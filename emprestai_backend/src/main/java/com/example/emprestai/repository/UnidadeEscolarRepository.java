package com.example.emprestai.repository;

import com.example.emprestai.model.UnidadeEscolar;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de unidades escolares. */
public interface UnidadeEscolarRepository extends JpaRepository<UnidadeEscolar, Long> {
    boolean existsByNome(String nome);
}
