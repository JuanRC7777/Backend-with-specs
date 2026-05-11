package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.Pago;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.PagoVentaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Pago (dominio) y PagoVentaEntity (JPA).
 */
@Component
public class PagoMapper {
    
    public Pago toDomain(PagoVentaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Pago(
            entity.getId(),
            entity.getVenta() != null ? entity.getVenta().getId() : null,
            entity.getMetodoPago(),
            entity.getMonto()
        );
    }
    
    public PagoVentaEntity toEntity(Pago pago) {
        if (pago == null) {
            return null;
        }
        
        return PagoVentaEntity.builder()
            .id(pago.getId())
            .metodoPago(pago.getMetodoPago())
            .monto(pago.getMonto())
            .build();
    }
}
