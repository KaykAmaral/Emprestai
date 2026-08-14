package com.example.emprestai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para cadastrar uma nova unidade escolar. */
public record UnidadeEscolarRequest(
        @NotBlank @Size(max = 255) String nome) { }
