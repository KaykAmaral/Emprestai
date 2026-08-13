package com.example.emprestai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados necessários para criar o único ADM_SUPREMO durante a instalação inicial. */
public record InitialAdminRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 255) String senha) { }
