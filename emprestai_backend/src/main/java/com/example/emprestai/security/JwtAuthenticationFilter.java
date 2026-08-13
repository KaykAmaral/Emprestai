package com.example.emprestai.security;

import java.io.IOException;
import java.util.Arrays;
import com.example.emprestai.config.AuthProperties;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UsuarioRepository;
import com.example.emprestai.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lê o JWT do cookie, valida-o e registra o usuário autenticado no contexto do Spring Security. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final AuthProperties authProperties;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository, AuthProperties authProperties) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.authProperties = authProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractCookie(request);
        if (token != null) {
            try {
                Claims claims = jwtService.parse(token);
                Usuario usuario = usuarioRepository.findByEmailAndAtivoTrue(claims.getSubject())
                        .orElseThrow(() -> new JwtException("Usuário inválido"));
                String papel = claims.get("papel", String.class);
                if (!usuario.getPapel().name().equals(papel)) {
                    throw new JwtException("Papel inválido");
                }
                var authentication = new UsernamePasswordAuthenticationToken(
                        usuario.getEmail(), null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
                if (request.getRequestURI().startsWith("/api/auth/")) {
                    filterChain.doFilter(request, response);
                    return;
                }
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                response.getWriter().write("{\"status\":401,\"message\":\"Token inválido ou expirado\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> authProperties.cookie().name().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
