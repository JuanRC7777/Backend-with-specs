package com.empresa.pos.application.port.in.producto;

import com.empresa.pos.application.dto.response.ProductoResponse;

public interface ObtenerProductoUseCase {

    ProductoResponse obtener(Long id);
}
