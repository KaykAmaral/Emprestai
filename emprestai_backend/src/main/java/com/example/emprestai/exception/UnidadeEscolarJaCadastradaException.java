package com.example.emprestai.exception;

/** Sinaliza tentativa de cadastrar uma unidade escolar com nome já existente. */
public class UnidadeEscolarJaCadastradaException extends RuntimeException {
    public UnidadeEscolarJaCadastradaException() {
        super("Unidade escolar já cadastrada");
    }
}
