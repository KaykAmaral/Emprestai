package com.example.emprestai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.emprestai.dto.request.InitialAdminRequest;
import com.example.emprestai.dto.request.LoginRequest;
import com.example.emprestai.dto.request.UnidadeEscolarRequest;
import com.example.emprestai.dto.request.UsuarioRequest;
import com.example.emprestai.dto.response.UsuarioResponse;
import com.example.emprestai.exception.InitialSetupUnavailableException;
import com.example.emprestai.exception.InvalidSetupKeyException;
import com.example.emprestai.exception.InvalidCredentialsException;
import com.example.emprestai.exception.LimiteAdmProatiAtingidoException;
import com.example.emprestai.exception.UnidadeEscolarJaCadastradaException;
import com.example.emprestai.model.UnidadeEscolar;
import com.example.emprestai.model.Usuario;
import com.example.emprestai.repository.UnidadeEscolarRepository;
import com.example.emprestai.repository.UsuarioRepository;
import com.example.emprestai.service.AuthService;
import com.example.emprestai.service.InitialSetupService;
import com.example.emprestai.service.JwtService;
import com.example.emprestai.service.UnidadeEscolarService;
import com.example.emprestai.service.UsuarioService;
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
    private UsuarioService usuarioService;

    @Autowired
    private UnidadeEscolarService unidadeEscolarService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UnidadeEscolarRepository unidadeEscolarRepository;

    @BeforeEach
    void createInitialAdmin() {
        usuarioRepository.deleteAll();
        unidadeEscolarRepository.deleteAll();
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

    @Test
    void allowsUpToTwoActiveAdmProatiPerUnidade() {
        UnidadeEscolar unidade = unidadeEscolarRepository.save(new UnidadeEscolar("Escola Teste"));
        Long unidadeId = unidade.getId();

        UsuarioResponse primeiro = usuarioService.criarAdmProati(proatiRequest("proati1@test", unidadeId));
        UsuarioResponse segundo = usuarioService.criarAdmProati(proatiRequest("proati2@test", unidadeId));

        assertThat(primeiro.papel()).isEqualTo("ADM_PROATI");
        assertThat(segundo.papel()).isEqualTo("ADM_PROATI");
        assertThatThrownBy(() -> usuarioService.criarAdmProati(proatiRequest("proati3@test", unidadeId)))
                .isInstanceOf(LimiteAdmProatiAtingidoException.class);
    }

    @Test
    void admProatiLimitIsPerUnidade() {
        UnidadeEscolar unidadeA = unidadeEscolarRepository.save(new UnidadeEscolar("Escola A"));
        UnidadeEscolar unidadeB = unidadeEscolarRepository.save(new UnidadeEscolar("Escola B"));

        usuarioService.criarAdmProati(proatiRequest("proatiA1@test", unidadeA.getId()));
        usuarioService.criarAdmProati(proatiRequest("proatiB1@test", unidadeB.getId()));

        assertThat(usuarioService.criarAdmProati(proatiRequest("proatiA2@test", unidadeA.getId())).papel())
                .isEqualTo("ADM_PROATI");
        assertThat(usuarioService.criarAdmProati(proatiRequest("proatiB2@test", unidadeB.getId())).papel())
                .isEqualTo("ADM_PROATI");
    }

    @Test
    void createsUnidadeEscolar() {
        UnidadeEscolar unidade = unidadeEscolarService.criar(new UnidadeEscolarRequest("Escola Nova"));

        assertThat(unidade.getId()).isNotNull();
        assertThat(unidade.getNome()).isEqualTo("Escola Nova");
        assertThat(unidade.isAtivo()).isTrue();
        assertThat(unidade.getCriadoEm()).isNotNull();
    }

    @Test
    void rejectsDuplicateUnidadeEscolarName() {
        unidadeEscolarService.criar(new UnidadeEscolarRequest("Escola Duplicada"));

        assertThatThrownBy(() -> unidadeEscolarService.criar(new UnidadeEscolarRequest("Escola Duplicada")))
                .isInstanceOf(UnidadeEscolarJaCadastradaException.class);
    }

    @Test
    void listsOnlyActiveUnidades() {
        unidadeEscolarService.criar(new UnidadeEscolarRequest("Escola Ativa"));
        UnidadeEscolar inativa = unidadeEscolarRepository.save(new UnidadeEscolar("Escola Inativa"));
        inativa.inativar();
        unidadeEscolarRepository.save(inativa);

        assertThat(unidadeEscolarService.listar())
                .extracting(UnidadeEscolar::getNome)
                .contains("Escola Ativa")
                .doesNotContain("Escola Inativa");
    }

    private UsuarioRequest proatiRequest(String email, Long unidadeEscolarId) {
        return new UsuarioRequest("Proati", email, "senha-segura", unidadeEscolarId);
    }

    private InitialAdminRequest adminRequest() {
        return new InitialAdminRequest("Administrador Inicial", "admin@emprestai.test", "senha-segura");
    }
}
