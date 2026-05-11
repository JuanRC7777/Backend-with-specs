package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Reembolso;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de reembolsos.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface ReembolsoRepositoryPort {
    
    /**
     * Guarda un reembolso.
     * 
     * @param reembolso Reembolso a persistir
     * @return Reembolso guardado con ID asignado
     */
    Reembolso save(Reembolso reembolso);
    
    /**
     * Busca un reembolso por el ID de la venta.
     * 
     * @param ventaId ID de la venta reembolsada
     * @return Reembolso si existe
     */
    Optional<Reembolso> findByVentaId(Long ventaId);
    
    /**
     * Verifica si existe un reembolso para una venta.
     * 
     * @param ventaId ID de la venta
     * @return true si existe un reembolso, false en caso contrario
     */
    boolean existsByVentaId(Long ventaId);
}
