package com.acc.connectauto.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.acc.connectauto.entity.Usuario;
import com.acc.connectauto.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Este projeto usa um unico usuario fixo (sem tela de cadastro): garante que
// ele exista no banco a cada subida, sem duplicar se ja estiver la.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${connectauto.security.admin-email}")
    private String adminEmail;

    @Value("${connectauto.security.admin-password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Usuario usuario = Usuario.builder()
                .email(adminEmail)
                .senha(passwordEncoder.encode(adminPassword))
                .build();
        usuarioRepository.save(usuario);
        log.info("Usuário administrador seedado: email={}", adminEmail);
    }
}
