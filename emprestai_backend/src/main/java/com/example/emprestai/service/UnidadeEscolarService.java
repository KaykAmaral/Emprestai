package com.example.emprestai.service;

import java.util.List;

import com.example.emprestai.dto.request.UnidadeEscolarRequest;
import com.example.emprestai.exception.UnidadeEscolarJaCadastradaException;
import com.example.emprestai.model.UnidadeEscolar;
import com.example.emprestai.repository.UnidadeEscolarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negócio do cadastro e da consulta de unidades escolares. */
@Service
public class UnidadeEscolarService {
    private final UnidadeEscolarRepository unidadeEscolarRepository;

    public UnidadeEscolarService(UnidadeEscolarRepository unidadeEscolarRepository) {
        this.unidadeEscolarRepository = unidadeEscolarRepository;
    }

    /** Lista apenas as unidades ativas, respeitando o soft delete (RN03). */
    @Transactional(readOnly = true)
    public List<UnidadeEscolar> listar() {
        return unidadeEscolarRepository.findAllByAtivoTrue();
    }

    /** Cadastra uma nova unidade, garantindo nome único no sistema (RNF08). */
    @Transactional
    public UnidadeEscolar criar(UnidadeEscolarRequest request) {
        String nome = request.nome().trim();
        if (unidadeEscolarRepository.existsByNome(nome)) {
            throw new UnidadeEscolarJaCadastradaException();
        }
        return unidadeEscolarRepository.save(new UnidadeEscolar(nome));
    }
}
