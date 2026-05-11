package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.Configuracion;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ConfiguracionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Configuracion (dominio) y ConfiguracionEntity (JPA).
 */
@Component
public class ConfiguracionMapper {
    
    public Configuracion toDomain(ConfiguracionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Configuracion(
            entity.getId(),
            entity.getClave(),
            entity.getValor()
        );
    }
    
    public ConfiguracionEntity toEntity(Configuracion configuracion) {
        if (configuracion == null) {
            return null;
        }
        
        return ConfiguracionEntity.builder()
            .id(configuracion.getId())
            .clave(configuracion.getClave())
            .valor(configuracion.getValor())
            .build();
    }
}
