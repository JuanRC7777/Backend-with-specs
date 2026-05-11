package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Configuracion;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de configuraciones del sistema.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface ConfiguracionRepositoryPort {
    
    /**
     * Busca una configuración por su clave.
     * 
     * @param clave Clave de configuración (ej: "tasa_impuesto")
     * @return Configuración si existe
     */
    Optional<Configuracion> findByClave(String clave);
    
    /**
     * Guarda o actualiza una configuración.
     * 
     * @param configuracion Configuración a persistir
     * @return Configuración guardada
     */
    Configuracion save(Configuracion configuracion);
}
