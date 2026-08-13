package com.example.emprestai.exception;

/** Mantém a resposta de login genérica para não revelar qual credencial falhou. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos");
    }
}
