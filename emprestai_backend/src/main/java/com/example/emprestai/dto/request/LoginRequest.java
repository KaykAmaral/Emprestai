package com.example.emprestai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciais recebidas pelo endpoint de login. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String senha) { }
