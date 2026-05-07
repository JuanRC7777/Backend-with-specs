package com.empresa.pos.application.port.in.producto;

import com.empresa.pos.application.dto.response.ProductoResponse;

import java.util.List;

public interface ListarProductosUseCase {

    List<ProductoResponse> listar();
}
