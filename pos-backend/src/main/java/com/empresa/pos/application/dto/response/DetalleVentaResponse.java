package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVentaResponse {

    private Long productoId;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnit;
    private BigDecimal subtotal;
}
