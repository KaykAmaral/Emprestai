package com.example.emprestai.controller;

import java.util.List;
import com.example.emprestai.dto.request.UnidadeEscolarRequest;
import com.example.emprestai.model.UnidadeEscolar;
import com.example.emprestai.service.UnidadeEscolarService;
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
    private final UnidadeEscolarService unidadeEscolarService;

    public UnidadeEscolarController(UnidadeEscolarService unidadeEscolarService) {
        this.unidadeEscolarService = unidadeEscolarService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADM_SUPREMO', 'ADM_PROATI')")
    public List<UnidadeEscolar> listar() {
        return unidadeEscolarService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADM_SUPREMO')")
    public ResponseEntity<UnidadeEscolar> criar(@Valid @RequestBody UnidadeEscolarRequest request) {
        UnidadeEscolar unidade = unidadeEscolarService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(unidade);
    }
}
