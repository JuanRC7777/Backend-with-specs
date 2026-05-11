package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Usuario si existe
     */
    Optional<Usuario> findById(Long id);
}
