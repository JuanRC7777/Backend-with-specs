package com.empresa.pos.application.dto.command;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Comando para actualizar la tasa de impuesto global del sistema.
 *
 * @version 3.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarTasaImpuestoCommand {

    @NotNull(message = "La tasa de impuesto es obligatoria")
    @DecimalMin(value = "0.0", message = "La tasa de impuesto no puede ser negativa")
    @DecimalMax(value = "1.0", message = "La tasa de impuesto no puede ser mayor a 1.0 (100%)")
    private BigDecimal tasaImpuesto;
}
