package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.UsuarioRepositoryPort;
import com.empresa.pos.domain.model.Usuario;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final JpaUsuarioRepository jpaRepository;

    public UsuarioRepositoryAdapter(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(entity -> {
            Usuario usuario = new Usuario();
            usuario.setId(entity.getId());
            usuario.setUsername(entity.getUsername());
            usuario.setPassword(entity.getPassword());
            usuario.setNombre(entity.getNombre());
            usuario.setRol(entity.getRol());
            usuario.setActivo(entity.isActivo());
            return usuario;
        });
    }
}
