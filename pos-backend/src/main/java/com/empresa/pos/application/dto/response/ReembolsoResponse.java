package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta con datos de un reembolso.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReembolsoResponse {
    
    private Long id;
    private Long ventaId;
    private String motivo;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String nombreUsuario;
}
