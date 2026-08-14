package com.example.emprestai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidade mínima da unidade escolar, usada no vínculo dos usuários. */
@Entity
@Table(name = "unidades_escolares")
public class UnidadeEscolar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    protected UnidadeEscolar() { }

    public UnidadeEscolar(String nome) {
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
}
