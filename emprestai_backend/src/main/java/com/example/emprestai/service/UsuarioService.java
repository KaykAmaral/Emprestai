package com.example.emprestai.service;

import com.example.emprestai.dto.request.UsuarioRequest;
import com.example.emprestai.exception.EmailJaCadastradoException;
import com.example.emprestai.exception.UnidadeEscolarNotFoundException;
import com.example.emprestai.model.PapelUsuario;
import com.example.emprestai.model.UnidadeEscolar;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UnidadeEscolarRepository;
import com.example.emprestai.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro de usuários comuns, vinculados a uma unidade escolar. */
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final UnidadeEscolarRepository unidadeEscolarRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UnidadeEscolarRepository unidadeEscolarRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.unidadeEscolarRepository = unidadeEscolarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criarAdmProati(UsuarioRequest request) {
        return criarUsuario(request, PapelUsuario.ADM_PROATI);
    }

    private Usuario criarUsuario(UsuarioRequest request, PapelUsuario papel) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        UnidadeEscolar unidade = unidadeEscolarRepository.findById(request.unidadeEscolarId())
                .orElseThrow(UnidadeEscolarNotFoundException::new);
        Usuario usuario = new Usuario(request.nome().trim(), email,
                passwordEncoder.encode(request.senha()), papel);
        usuario.setUnidadeEscolar(unidade);
        return usuarioRepository.save(usuario);
    }
}
