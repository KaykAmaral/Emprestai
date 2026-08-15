package com.example.emprestai.repository;

import java.util.Optional;
import com.example.emprestai.model.PapelUsuario;
import com.example.emprestai.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de usuários necessárias para autenticação e instalação inicial. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndAtivoTrue(String email);
    boolean existsByEmail(String email);
    boolean existsByPapelAndAtivoTrue(PapelUsuario papel);
    long countByPapelAndAtivoTrueAndUnidadeEscolar_Id(PapelUsuario papel, Long unidadeEscolarId);
}
