package com.example.emprestai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Centraliza a chave de assinatura e a validade dos tokens JWT. */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration expiration) { }
