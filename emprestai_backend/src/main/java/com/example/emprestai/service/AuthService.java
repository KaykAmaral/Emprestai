package com.example.emprestai.service;

import com.example.emprestai.dto.request.LoginRequest;
import com.example.emprestai.exception.InvalidCredentialsException;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Valida credenciais contra usuários ativos, sem expor a causa da falha ao cliente. */
@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Usuario authenticate(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailAndAtivoTrue(request.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }
        return usuario;
    }
}
