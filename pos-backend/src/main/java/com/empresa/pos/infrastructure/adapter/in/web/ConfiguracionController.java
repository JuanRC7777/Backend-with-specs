package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.ActualizarTasaImpuestoCommand;
import com.empresa.pos.application.dto.response.ConfiguracionResponse;
import com.empresa.pos.application.port.in.configuracion.ActualizarTasaImpuestoUseCase;
import com.empresa.pos.application.port.in.configuracion.ObtenerTasaImpuestoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller REST para gestión de configuración del sistema.
 * Permite consultar y actualizar la tasa de impuesto global.
 *
 * @version 3.2.0
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ObtenerTasaImpuestoUseCase obtenerTasaImpuestoUseCase;
    private final ActualizarTasaImpuestoUseCase actualizarTasaImpuestoUseCase;

    public ConfiguracionController(ObtenerTasaImpuestoUseCase obtenerTasaImpuestoUseCase,
                                    ActualizarTasaImpuestoUseCase actualizarTasaImpuestoUseCase) {
        this.obtenerTasaImpuestoUseCase = obtenerTasaImpuestoUseCase;
        this.actualizarTasaImpuestoUseCase = actualizarTasaImpuestoUseCase;
    }

    @GetMapping("/tasa-impuesto")
    public ResponseEntity<ConfiguracionResponse> obtenerTasaImpuesto() {
        BigDecimal tasa = obtenerTasaImpuestoUseCase.obtenerTasaImpuesto();
        ConfiguracionResponse response = ConfiguracionResponse.builder()
                .clave("tasa_impuesto")
                .valor(tasa.toPlainString())
                .valorDecimal(tasa)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasa-impuesto")
    public ResponseEntity<ConfiguracionResponse> actualizarTasaImpuesto(
            @Valid @RequestBody ActualizarTasaImpuestoCommand command) {
        actualizarTasaImpuestoUseCase.actualizar(command.getTasaImpuesto());
        BigDecimal tasaActualizada = obtenerTasaImpuestoUseCase.obtenerTasaImpuesto();
        ConfiguracionResponse response = ConfiguracionResponse.builder()
                .clave("tasa_impuesto")
                .valor(tasaActualizada.toPlainString())
                .valorDecimal(tasaActualizada)
                .build();
        return ResponseEntity.ok(response);
    }
}
