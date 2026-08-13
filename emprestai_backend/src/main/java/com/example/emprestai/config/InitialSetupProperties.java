package com.example.emprestai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Armazena a chave de uso único necessária para cadastrar o primeiro administrador. */
@ConfigurationProperties(prefix = "initial-setup")
public record InitialSetupProperties(String key) { }
