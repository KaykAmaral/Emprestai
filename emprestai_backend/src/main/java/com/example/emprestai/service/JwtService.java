package com.example.emprestai.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import com.example.emprestai.config.JwtProperties;
import com.example.emprestai.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/** Gera e valida JWTs assinados com a chave configurada para o ambiente atual. */
@Service
public class JwtService {
    private final JwtProperties properties;
    private SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initialize() {
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("JWT_SECRET deve ser definido.");
        }
        byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 bytes.");
        }
        if (properties.expiration() == null || properties.expiration().isNegative() || properties.expiration().isZero()) {
            throw new IllegalStateException("jwt.expiration deve ser uma duração positiva.");
        }
        key = Keys.hmacShaKeyFor(secret);
    }

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.expiration().toMillis());
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("papel", usuario.getPapel().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long expirationSeconds() {
        return properties.expiration().toSeconds();
    }
}
