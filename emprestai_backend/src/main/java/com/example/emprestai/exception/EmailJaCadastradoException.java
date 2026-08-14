package com.example.emprestai.exception;

/** Sinaliza tentativa de cadastrar um e-mail que já está em uso. */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException() {
        super("E-mail já cadastrado");
    }
}
