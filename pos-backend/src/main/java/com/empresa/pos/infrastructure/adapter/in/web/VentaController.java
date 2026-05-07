package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.ListarVentasUseCase;
import com.empresa.pos.application.port.in.venta.RegistrarVentaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final ListarVentasUseCase listarVentasUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase,
                            ListarVentasUseCase listarVentasUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.listarVentasUseCase = listarVentasUseCase;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody RegistrarVentaCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarVentaUseCase.registrar(command));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar() {
        return ResponseEntity.ok(listarVentasUseCase.listar());
    }
}
