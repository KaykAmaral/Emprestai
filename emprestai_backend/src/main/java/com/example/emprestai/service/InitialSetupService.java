package com.example.emprestai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import com.example.emprestai.config.InitialSetupProperties;
import com.example.emprestai.dto.request.InitialAdminRequest;
import com.example.emprestai.exception.InitialSetupUnavailableException;
import com.example.emprestai.exception.InvalidSetupKeyException;
import com.example.emprestai.model.PapelUsuario;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Controla o cadastro único e protegido do primeiro ADM_SUPREMO. */
@Service
public class InitialSetupService {
    private final InitialSetupProperties properties;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialSetupService(InitialSetupProperties properties, UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void validateConfiguration() {
        if (properties.key() == null || properties.key().isBlank()) {
            throw new IllegalStateException("INITIAL_SETUP_KEY deve ser definido.");
        }
    }

    @Transactional
    public void createInitialAdmin(InitialAdminRequest request, String setupKey) {
        validateSetupKey(setupKey);
        if (usuarioRepository.existsByPapelAndAtivoTrue(PapelUsuario.ADM_SUPREMO)) {
            throw new InitialSetupUnavailableException();
        }
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new InitialSetupUnavailableException();
        }
        usuarioRepository.save(new Usuario(request.nome().trim(), email,
                passwordEncoder.encode(request.senha()), PapelUsuario.ADM_SUPREMO));
    }

    private void validateSetupKey(String setupKey) {
        if (properties.key() == null || properties.key().isBlank() || setupKey == null
                || !MessageDigest.isEqual(properties.key().getBytes(StandardCharsets.UTF_8),
                        setupKey.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidSetupKeyException();
        }
    }
}
