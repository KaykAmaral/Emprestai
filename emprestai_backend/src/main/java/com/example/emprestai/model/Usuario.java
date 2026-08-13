package com.example.emprestai.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Entidade persistida de autenticação e autorização dos usuários do sistema. */
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PapelUsuario papel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_escolar_id")
    private UnidadeEscolar unidadeEscolar;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    protected Usuario() { }

    public Usuario(String nome, String email, String senha, PapelUsuario papel) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    @PrePersist
    void prePersist() { criadoEm = atualizadoEm = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { atualizadoEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public PapelUsuario getPapel() { return papel; }
    public UnidadeEscolar getUnidadeEscolar() { return unidadeEscolar; }
    public boolean isAtivo() { return ativo; }
}
