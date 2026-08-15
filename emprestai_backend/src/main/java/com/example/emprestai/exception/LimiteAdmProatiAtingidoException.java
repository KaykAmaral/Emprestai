package com.example.emprestai.exception;

/** Indica que a unidade já atingiu o número máximo de administradores PROATI ativos. */
public class LimiteAdmProatiAtingidoException extends RuntimeException {
    public LimiteAdmProatiAtingidoException() {
        super("Unidade já possui o número máximo de administradores PROATI ativos");
    }
}
