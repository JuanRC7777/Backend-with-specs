package com.empresa.pos.application.dto.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Comando que representa un pago individual dentro de una venta.
 * Una venta puede tener múltiples pagos con distintos métodos.
 *
 * @version 3.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoCommand {

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
        regexp = "^(EFECTIVO|TARJETA|TRANSFERENCIA)$",
        message = "El método de pago debe ser EFECTIVO, TARJETA o TRANSFERENCIA"
    )
    private String metodoPago;

    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto del pago debe ser mayor a 0")
    private BigDecimal monto;
}
