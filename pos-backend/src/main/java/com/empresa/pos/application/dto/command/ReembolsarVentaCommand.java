package com.empresa.pos.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comando para reembolsar una venta.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReembolsarVentaCommand {
    
    @NotNull(message = "El ID de la venta es obligatorio")
    private Long ventaId;
    
    @NotBlank(message = "El motivo del reembolso es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
    
    // El usuarioId se obtiene del JWT en el servicio
    private Long usuarioId;
}
