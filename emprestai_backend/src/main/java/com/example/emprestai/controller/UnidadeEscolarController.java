package com.example.emprestai.controller;

import java.util.List;
import com.example.emprestai.dto.request.UnidadeEscolarRequest;
import com.example.emprestai.model.UnidadeEscolar;
import com.example.emprestai.repository.UnidadeEscolarRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro e consulta de unidades escolares. */
@RestController
@RequestMapping("/api/unidades")
public class UnidadeEscolarController {
    private final UnidadeEscolarRepository unidadeEscolarRepository;

    public UnidadeEscolarController(UnidadeEscolarRepository unidadeEscolarRepository) {
        this.unidadeEscolarRepository = unidadeEscolarRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADM_SUPREMO', 'ADM_PROATI')")
    public List<UnidadeEscolar> listar() {
        return unidadeEscolarRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADM_SUPREMO')")
    public ResponseEntity<UnidadeEscolar> criar(@Valid @RequestBody UnidadeEscolarRequest request) {
        UnidadeEscolar unidade = unidadeEscolarRepository.save(new UnidadeEscolar(request.nome().trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(unidade);
    }
}
