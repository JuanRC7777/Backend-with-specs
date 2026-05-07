package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> findByUsername(String username);
}
