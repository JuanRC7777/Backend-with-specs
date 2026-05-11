package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.command.ReembolsarVentaCommand;
import com.empresa.pos.application.dto.response.ReembolsoResponse;

/**
 * Caso de uso para reembolsar una venta.
 * Devuelve el stock de los productos y marca la venta como reembolsada.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface ReembolsarVentaUseCase {
    
    /**
     * Reembolsa una venta completa.
     * - Valida que la venta no esté ya reembolsada
     * - Devuelve el stock de todos los productos
     * - Crea un registro de reembolso
     * - Marca la venta como reembolsada
     * 
     * @param command Comando con ventaId, motivo y usuarioId
     * @return Datos del reembolso creado
     * @throws com.empresa.pos.domain.exception.RecursoNoEncontradoException si la venta no existe
     * @throws com.empresa.pos.domain.exception.VentaYaReembolsadaException si la venta ya fue reembolsada
     */
    ReembolsoResponse reembolsar(ReembolsarVentaCommand command);
}
