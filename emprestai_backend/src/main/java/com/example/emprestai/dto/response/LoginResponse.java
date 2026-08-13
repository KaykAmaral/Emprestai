package com.example.emprestai.dto.response;

/** Corpo do login; o token permanece apenas no cookie HttpOnly. */
public record LoginResponse(UsuarioResponse usuario) { }
