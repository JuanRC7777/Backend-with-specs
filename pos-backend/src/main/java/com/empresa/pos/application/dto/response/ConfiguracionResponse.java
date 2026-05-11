package com.empresa.pos.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta con datos de una configuración del sistema.
 *
 * @version 3.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionResponse {

    private String clave;
    private String valor;
    private BigDecimal valorDecimal;
}
