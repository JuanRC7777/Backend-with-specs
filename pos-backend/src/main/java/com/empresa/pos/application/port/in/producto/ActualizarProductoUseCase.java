package com.empresa.pos.application.port.in.producto;

import com.empresa.pos.application.dto.command.ActualizarProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;

public interface ActualizarProductoUseCase {

    ProductoResponse actualizar(Long id, ActualizarProductoCommand command);
}
