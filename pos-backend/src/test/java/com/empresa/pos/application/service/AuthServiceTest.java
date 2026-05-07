package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.LoginCommand;
import com.empresa.pos.application.dto.response.LoginResponse;
import com.empresa.pos.application.port.out.JwtPort;
import com.empresa.pos.application.port.out.UsuarioRepositoryPort;
import com.empresa.pos.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtPort jwtPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_debeRetornarToken_cuandoCredencialesValidas() {
        Usuario usuario = new Usuario(1L, "admin", "$2a$10$hash", "Administrador", "ADMIN", true);

        given(usuarioRepository.findByUsername("admin")).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("admin123", "$2a$10$hash")).willReturn(true);
        given(jwtPort.generateToken("admin")).willReturn("jwt-token-generado");

        LoginResponse response = authService.login(new LoginCommand("admin", "admin123"));

        assertThat(response.getToken()).isEqualTo("jwt-token-generado");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getNombre()).isEqualTo("Administrador");
    }

    @Test
    void login_debeLanzarExcepcion_cuandoPasswordIncorrecto() {
        Usuario usuario = new Usuario(1L, "admin", "$2a$10$hash", "Administrador", "ADMIN", true);

        given(usuarioRepository.findByUsername("admin")).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("wrongpass", "$2a$10$hash")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("admin", "wrongpass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_debeLanzarExcepcion_cuandoUsuarioNoExiste() {
        given(usuarioRepository.findByUsername("noexiste")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("noexiste", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
