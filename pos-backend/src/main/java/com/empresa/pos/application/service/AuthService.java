package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.LoginCommand;
import com.empresa.pos.application.dto.response.LoginResponse;
import com.empresa.pos.application.port.in.auth.LoginUseCase;
import com.empresa.pos.application.port.out.JwtPort;
import com.empresa.pos.application.port.out.UsuarioRepositoryPort;
import com.empresa.pos.domain.model.Usuario;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPort jwtPort;

    public AuthService(UsuarioRepositoryPort usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtPort jwtPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
    }

    @Override
    public LoginResponse login(LoginCommand command) {
        Usuario usuario = usuarioRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(command.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtPort.generateToken(usuario.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .build();
    }
}
