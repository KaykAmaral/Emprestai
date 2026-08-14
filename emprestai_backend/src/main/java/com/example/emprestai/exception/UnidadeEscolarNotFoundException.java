package com.example.emprestai.exception;

/** Sinaliza referência a uma unidade escolar inexistente. */
public class UnidadeEscolarNotFoundException extends RuntimeException {
    public UnidadeEscolarNotFoundException() {
        super("Unidade escolar não encontrada");
    }
}
