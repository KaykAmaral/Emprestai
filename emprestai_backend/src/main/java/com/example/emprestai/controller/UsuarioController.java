package com.example.emprestai.controller;

import com.example.emprestai.dto.request.UsuarioRequest;
import com.example.emprestai.dto.response.UsuarioResponse;
import com.example.emprestai.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operações de cadastro de usuários da aplicação. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Criação de PROATI para unidade escolar, só é autorizada se o user for ADM_SUPREMO */
    @PostMapping("/adm-proati")
    @PreAuthorize("hasRole('ADM_SUPREMO')")
    public ResponseEntity<UsuarioResponse> criarAdmProati(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarAdmProati(request));
    }
}
