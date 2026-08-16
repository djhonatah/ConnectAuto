package com.acc.connectauto.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.acc.connectauto.dto.request.LoginRequestDTO;
import com.acc.connectauto.dto.response.LoginResponseDTO;
import com.acc.connectauto.entity.Usuario;
import com.acc.connectauto.exception.CredenciaisInvalidasException;
import com.acc.connectauto.repository.UsuarioRepository;
import com.acc.connectauto.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.email())
                .filter(candidato -> passwordEncoder.matches(loginRequestDTO.senha(), candidato.getSenha()))
                .orElseThrow(() -> {
                    log.warn("Tentativa de login inválida para email={}", loginRequestDTO.email());
                    return new CredenciaisInvalidasException("E-mail ou senha inválidos.");
                });

        String token = jwtService.gerarToken(usuario.getEmail());
        log.info("Login bem-sucedido: email={}", usuario.getEmail());
        return new LoginResponseDTO(token, usuario.getEmail());
    }
}
