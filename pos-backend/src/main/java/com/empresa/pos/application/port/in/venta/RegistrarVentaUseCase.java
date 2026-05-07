package com.empresa.pos.application.port.in.venta;

import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.VentaResponse;

public interface RegistrarVentaUseCase {

    VentaResponse registrar(RegistrarVentaCommand command);
}
