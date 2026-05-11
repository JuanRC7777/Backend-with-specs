package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.response.VentaResponse;

/**
 * Caso de uso para obtener una venta por su número de factura.
 * Incluye datos de reembolso si existe.
 * 
 * @version 3.2.0
 */
public interface ObtenerVentaPorFacturaUseCase {
    
    /**
     * Obtiene una venta por su número de factura.
     * 
     * @param numeroFactura Número de factura (formato: FAC-YYYYMMDD-NNNNNN)
     * @return Datos de la venta con detalles, pagos y reembolso (si existe)
     * @throws com.empresa.pos.domain.exception.RecursoNoEncontradoException si la venta no existe
     */
    VentaResponse obtenerPorNumeroFactura(String numeroFactura);
}
