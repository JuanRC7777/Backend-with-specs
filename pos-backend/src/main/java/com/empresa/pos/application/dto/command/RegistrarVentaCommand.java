package com.empresa.pos.application.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarVentaCommand {

    private Long usuarioId; // Se establece automáticamente desde el token JWT

    @NotEmpty(message = "La venta debe tener al menos un item")
    @Valid
    private List<ItemVentaCommand> items;
}
