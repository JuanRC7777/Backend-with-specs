package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.response.VentaResponse;

/**
 * Caso de uso para obtener una venta por su ID.
 * Incluye datos de reembolso si existe.
 * 
 * @version 3.2.0
 */
public interface ObtenerVentaUseCase {
    
    /**
     * Obtiene una venta por su ID.
     * 
     * @param id ID de la venta
     * @return Datos de la venta con detalles, pagos y reembolso (si existe)
     * @throws com.empresa.pos.domain.exception.RecursoNoEncontradoException si la venta no existe
     */
    VentaResponse obtenerPorId(Long id);
}
