package com.example.emprestai.dto.response;

import com.example.emprestai.model.Usuario;

/** Dados públicos de usuário retornados pela API, sem senha ou hash. */
public record UsuarioResponse(Long id, String nome, String email, String papel, UnidadeResumoResponse unidadeEscolar) {
    public static UsuarioResponse from(Usuario usuario) {
        UnidadeResumoResponse unidade = usuario.getUnidadeEscolar() == null ? null
                : new UnidadeResumoResponse(usuario.getUnidadeEscolar().getId(), usuario.getUnidadeEscolar().getNome());
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel().name(), unidade);
    }
}
