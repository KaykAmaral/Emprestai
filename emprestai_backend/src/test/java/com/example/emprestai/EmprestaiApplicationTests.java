package com.example.emprestai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.emprestai.dto.request.InitialAdminRequest;
import com.example.emprestai.dto.request.LoginRequest;
import com.example.emprestai.exception.InitialSetupUnavailableException;
import com.example.emprestai.exception.InvalidSetupKeyException;
import com.example.emprestai.exception.InvalidCredentialsException;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UsuarioRepository;
import com.example.emprestai.service.AuthService;
import com.example.emprestai.service.InitialSetupService;
import com.example.emprestai.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Verifica autenticação, criação inicial e conteúdo essencial do JWT em um banco H2 isolado. */
@SpringBootTest
class EmprestaiApplicationTests {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private InitialSetupService initialSetupService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void createInitialAdmin() {
        usuarioRepository.deleteAll();
        initialSetupService.createInitialAdmin(adminRequest(), "chave-de-instalacao-de-teste");
    }

    @Test
    void contextLoads() { }

    @Test
    void authenticatesBootstrapUserAndGeneratesJwtWithRole() {
        Usuario usuario = authService.authenticate(new LoginRequest("admin@emprestai.test", "senha-segura"));
        String token = jwtService.generateToken(usuario);

        assertThat(usuario.getSenha()).isNotEqualTo("senha-segura");
        assertThat(jwtService.parse(token).getSubject()).isEqualTo("admin@emprestai.test");
        assertThat(jwtService.parse(token).get("papel", String.class)).isEqualTo("ADM_SUPREMO");
    }

    @Test
    void rejectsInvalidCredentials() {
        assertThatThrownBy(() -> authService.authenticate(new LoginRequest("admin@emprestai.test", "incorreta")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void acceptsOnlyTheInstallationKeyAndOnlyOneInitialAdmin() {
        assertThatThrownBy(() -> initialSetupService.createInitialAdmin(adminRequest(), "chave-invalida"))
                .isInstanceOf(InvalidSetupKeyException.class);
        assertThatThrownBy(() -> initialSetupService.createInitialAdmin(
                new InitialAdminRequest("Outro Admin", "outro@emprestai.test", "senha-segura"),
                "chave-de-instalacao-de-teste"))
                .isInstanceOf(InitialSetupUnavailableException.class);
    }

    private InitialAdminRequest adminRequest() {
        return new InitialAdminRequest("Administrador Inicial", "admin@emprestai.test", "senha-segura");
    }
}
