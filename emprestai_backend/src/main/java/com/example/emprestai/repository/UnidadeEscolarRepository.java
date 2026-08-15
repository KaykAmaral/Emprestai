package com.example.emprestai.repository;

import java.util.List;
import java.util.Optional;

import com.example.emprestai.model.UnidadeEscolar;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Consultas de unidades escolares. */
public interface UnidadeEscolarRepository extends JpaRepository<UnidadeEscolar, Long> {
    boolean existsByNome(String nome);

    /** Retorna apenas unidades ativas, respeitando o soft delete (RN03). */
    List<UnidadeEscolar> findAllByAtivoTrue();

    /** Trava a linha da unidade (lock pessimista) para serializar operações dependentes, ex.: RN01 (limite de ADM_PROATI). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UnidadeEscolar u where u.id = :id")
    Optional<UnidadeEscolar> findWithLockingById(@Param("id") Long id);
}
