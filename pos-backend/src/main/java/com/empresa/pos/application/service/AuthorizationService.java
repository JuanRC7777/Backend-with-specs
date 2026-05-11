package com.empresa.pos.application.service;

import com.empresa.pos.domain.model.Usuario;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado de autorización basado en roles.
 * Actualmente soporta el rol ADMIN. Diseñado con interfaces
 * para facilitar la adición de roles futuros.
 *
 * @version 3.2.0
 */
@Service
public class AuthorizationService {

    private static final String ROL_ADMIN = "ADMIN";

    /**
     * Verifica que el usuario tenga rol ADMIN.
     *
     * @param usuario Usuario a verificar
     * @throws org.springframework.security.access.AccessDeniedException si no tiene permiso
     */
    public void verificarPermisoAdmin(Usuario usuario) {
        if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Acceso denegado: se requiere rol ADMIN");
        }
    }

    /**
     * Verifica si el usuario tiene rol ADMIN.
     *
     * @param usuario Usuario a verificar
     * @return true si tiene rol ADMIN
     */
    public boolean esAdmin(Usuario usuario) {
        return usuario != null && ROL_ADMIN.equalsIgnoreCase(usuario.getRol());
    }
}
