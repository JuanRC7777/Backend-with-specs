package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta con datos de un pago individual de una venta.
 *
 * @version 3.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    private Long id;
    private String metodoPago;
    private BigDecimal monto;
}
