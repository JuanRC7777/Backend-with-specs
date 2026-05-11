package com.empresa.pos.application.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Comando para registrar una nueva venta.
 * El usuarioId se establece automáticamente desde el JWT.
 * La tasa de impuesto se obtiene de la configuración global.
 *
 * @version 3.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarVentaCommand {

    /** Se establece automáticamente desde el token JWT. */
    private Long usuarioId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del cliente debe tener entre 3 y 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
        message = "El nombre del cliente solo puede contener letras, espacios y tildes"
    )
    private String nombreCliente;

    @NotBlank(message = "La cédula del cliente es obligatoria")
    @Pattern(
        regexp = "^\\d{10}$",
        message = "La cédula del cliente debe tener exactamente 10 dígitos"
    )
    private String cedulaCliente;

    @NotEmpty(message = "La venta debe tener al menos un item")
    @Valid
    private List<ItemVentaCommand> items;

    @NotEmpty(message = "La venta debe tener al menos un pago")
    @Valid
    private List<PagoCommand> pagos;
}
