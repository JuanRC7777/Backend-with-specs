package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponse {

    private Long id;
    private Long usuarioId;
    private List<DetalleVentaResponse> detalles;
    private BigDecimal total;
    private LocalDateTime fecha;
}
