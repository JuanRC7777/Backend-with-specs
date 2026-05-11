package com.empresa.pos.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Comando con filtros opcionales para listar ventas con paginación.
 *
 * @version 3.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroVentasCommand {

    /** Fecha de la venta (ISO-8601: YYYY-MM-DD). Opcional. */
    private LocalDate fecha;

    /** ID del cajero. Opcional. */
    private Long cajeroId;

    /** Cédula del cliente (10 dígitos). Opcional. */
    private String cedulaCliente;

    /** Método de pago (EFECTIVO, TARJETA, TRANSFERENCIA). Opcional. */
    private String metodoPago;

    /** Número de página (base 0). Default: 0. */
    @Builder.Default
    private int page = 0;

    /** Tamaño de página. Default: 20. */
    @Builder.Default
    private int size = 20;
}
