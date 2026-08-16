package com.acc.connectauto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acc.connectauto.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
}
