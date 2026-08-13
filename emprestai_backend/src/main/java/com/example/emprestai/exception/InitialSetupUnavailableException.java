package com.example.emprestai.exception;

/** Sinaliza que a instalação inicial não pode mais criar outro administrador. */
public class InitialSetupUnavailableException extends RuntimeException {
    public InitialSetupUnavailableException() {
        super("O administrador inicial já foi cadastrado");
    }
}
