package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.Reembolso;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ReembolsoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Reembolso (dominio) y ReembolsoEntity (JPA).
 */
@Component
public class ReembolsoMapper {
    
    public Reembolso toDomain(ReembolsoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Reembolso(
            entity.getId(),
            entity.getVenta() != null ? entity.getVenta().getId() : null,
            entity.getMotivo(),
            entity.getFecha(),
            entity.getUsuario() != null ? entity.getUsuario().getId() : null,
            entity.getNombreUsuario()
        );
    }
    
    public ReembolsoEntity toEntity(Reembolso reembolso) {
        if (reembolso == null) {
            return null;
        }
        
        return ReembolsoEntity.builder()
            .id(reembolso.getId())
            .motivo(reembolso.getMotivo())
            .fecha(reembolso.getFecha())
            .nombreUsuario(reembolso.getNombreUsuario())
            .build();
    }
}
