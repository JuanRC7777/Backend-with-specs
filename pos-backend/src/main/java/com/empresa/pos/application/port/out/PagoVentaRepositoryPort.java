package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Pago;
import java.util.List;

/**
 * Puerto de salida para persistencia de pagos de ventas.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface PagoVentaRepositoryPort {
    
    /**
     * Guarda múltiples pagos de una venta.
     * 
     * @param pagos Lista de pagos a persistir
     * @return Lista de pagos guardados con IDs asignados
     */
    List<Pago> saveAll(List<Pago> pagos);
    
    /**
     * Busca todos los pagos de una venta específica.
     * 
     * @param ventaId ID de la venta
     * @return Lista de pagos de la venta
     */
    List<Pago> findByVentaId(Long ventaId);
}
