package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.response.VentaResponse;
import java.util.List;

public interface ListarVentasUseCase {

    List<VentaResponse> listar();
}
