package com.example.emprestai.controller;

import com.example.emprestai.config.AuthProperties;
import com.example.emprestai.dto.request.LoginRequest;
import com.example.emprestai.dto.request.InitialAdminRequest;
import com.example.emprestai.dto.response.LoginResponse;
import com.example.emprestai.dto.response.UsuarioResponse;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.service.AuthService;
import com.example.emprestai.service.JwtService;
import com.example.emprestai.service.InitialSetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expõe as operações públicas de CSRF, instalação inicial, login e logout. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final InitialSetupService initialSetupService;

    public AuthController(AuthService authService, JwtService jwtService, AuthProperties authProperties,
            InitialSetupService initialSetupService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authProperties = authProperties;
        this.initialSetupService = initialSetupService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = authService.authenticate(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie(jwtService.generateToken(usuario), jwtService.expirationSeconds()).toString())
                .body(new LoginResponse(UsuarioResponse.from(usuario)));
    }

    @PostMapping("/setup/administrador")
    public ResponseEntity<Void> setupInitialAdmin(@Valid @RequestBody InitialAdminRequest request,
            @org.springframework.web.bind.annotation.RequestHeader("X-Setup-Key") String setupKey) {
        initialSetupService.createInitialAdmin(request, setupKey);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookie("", 0).toString())
                .build();
    }

    private ResponseCookie authCookie(String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(authProperties.cookie().name(), value)
                .httpOnly(true)
                .secure(authProperties.cookie().secure())
                .sameSite(authProperties.cookie().sameSite())
                .path("/")
                .maxAge(maxAgeSeconds);
        if (authProperties.cookie().domain() != null && !authProperties.cookie().domain().isBlank()) {
            builder.domain(authProperties.cookie().domain());
        }
        return builder.build();
    }
}
