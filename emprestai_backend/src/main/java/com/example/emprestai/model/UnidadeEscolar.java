package com.example.emprestai.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Entidade da unidade escolar, usada no vínculo dos usuários e na gestão de equipamentos. */
@Entity
@Table(name = "unidades_escolares")
public class UnidadeEscolar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    /** Soft delete (RN03): ao inativar, preserva o histórico vinculado. */
    @Column(nullable = false)
    private boolean ativo = true;

    /** Auditoria (RNF07): data de criação, preenchida automaticamente. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Auditoria (RNF07): data da última alteração, preenchida automaticamente. */
    private LocalDateTime atualizadoEm;

    protected UnidadeEscolar() { }

    public UnidadeEscolar(String nome) {
        this.nome = nome;
    }

    @PrePersist
    void prePersist() { criadoEm = atualizadoEm = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { atualizadoEm = LocalDateTime.now(); }

    /** Inativa a unidade (soft delete — RN03). */
    public void inativar() { this.ativo = false; }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
