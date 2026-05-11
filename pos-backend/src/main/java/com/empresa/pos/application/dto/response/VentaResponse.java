package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con datos completos de una venta.
 *
 * @version 3.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponse {

    private Long id;
    private String numeroFactura;
    private Long usuarioId;
    private String nombreCajero;
    private String nombreCliente;
    private String cedulaCliente;
    private List<DetalleVentaResponse> detalles;
    private List<PagoResponse> pagos;
    private BigDecimal subtotal;
    private BigDecimal tasaImpuesto;
    private BigDecimal impuesto;
    private BigDecimal total;
    private LocalDateTime fecha;
    private boolean reembolsada;

    /** Datos del reembolso si la venta fue reembolsada. Null si no aplica. */
    private ReembolsoResponse reembolso;
}
