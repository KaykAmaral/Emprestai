package com.example.emprestai.exception;

/** Sinaliza a tentativa de usar uma chave de instalação inválida. */
public class InvalidSetupKeyException extends RuntimeException {
    public InvalidSetupKeyException() {
        super("Chave de instalação inválida");
    }
}
