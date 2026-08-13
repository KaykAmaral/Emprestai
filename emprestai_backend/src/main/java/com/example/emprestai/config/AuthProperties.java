package com.example.emprestai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configura atributos dos cookies de autenticação e as origens permitidas pelo CORS. */
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(Cookie cookie, Cors cors) {
    public record Cookie(String name, boolean secure, String sameSite, String domain) { }
    public record Cors(String allowedOrigins) { }
}
