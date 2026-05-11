package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.command.FiltroVentasCommand;
import com.empresa.pos.application.dto.response.VentaResponse;
import org.springframework.data.domain.Page;

/**
 * Caso de uso para listar ventas con paginación y filtros opcionales.
 *
 * @version 3.2.0
 */
public interface ListarVentasUseCase {

    /**
     * Lista ventas con paginación y filtros opcionales.
     * Si la página solicitada no existe, retorna lista vacía (no lanza excepción).
     *
     * @param filtro Filtros opcionales: fecha, cajeroId, cedulaCliente, metodoPago, page, size
     * @return Página de ventas con metadata de paginación
     */
    Page<VentaResponse> listar(FiltroVentasCommand filtro);
}
