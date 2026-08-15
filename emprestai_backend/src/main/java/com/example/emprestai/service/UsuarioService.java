package com.example.emprestai.service;

import com.example.emprestai.dto.request.UsuarioRequest;
import com.example.emprestai.dto.response.UsuarioResponse;
import com.example.emprestai.exception.EmailJaCadastradoException;
import com.example.emprestai.exception.LimiteAdmProatiAtingidoException;
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
    /** RN01: máximo de administradores PROATI ativos por unidade. */
    private static final int MAXIMO_ADM_PROATI_POR_UNIDADE = 2;

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
    public UsuarioResponse criarAdmProati(UsuarioRequest request) {
        // RN01: trava a linha da unidade (lock pessimista) antes de validar o limite,
        // serializando cadastros simultâneos de ADM_PROATI na mesma unidade.
        UnidadeEscolar unidade = unidadeEscolarRepository.findWithLockingById(request.unidadeEscolarId())
                .orElseThrow(UnidadeEscolarNotFoundException::new);
        validarLimiteAdmProatiPorUnidade(unidade);
        return UsuarioResponse.from(criarUsuario(request, PapelUsuario.ADM_PROATI, unidade));
    }

    /** RN01: impede que a unidade ultrapasse o máximo de ADM_PROATI ativos. */
    private void validarLimiteAdmProatiPorUnidade(UnidadeEscolar unidade) {
        long ativos = usuarioRepository.countByPapelAndAtivoTrueAndUnidadeEscolar_Id(
                PapelUsuario.ADM_PROATI, unidade.getId());
        if (ativos >= MAXIMO_ADM_PROATI_POR_UNIDADE) {
            throw new LimiteAdmProatiAtingidoException();
        }
    }

    private Usuario criarUsuario(UsuarioRequest request, PapelUsuario papel, UnidadeEscolar unidade) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(request.nome().trim(), email,
                passwordEncoder.encode(request.senha()), papel);
        usuario.setUnidadeEscolar(unidade);
        return usuarioRepository.save(usuario);
    }
}
