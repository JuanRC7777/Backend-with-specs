package com.empresa.pos.application.port.in.producto;

import com.empresa.pos.application.dto.command.CrearProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;

public interface CrearProductoUseCase {

    ProductoResponse crear(CrearProductoCommand command);
}
