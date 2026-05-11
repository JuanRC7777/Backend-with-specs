package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.FiltroVentasCommand;
import com.empresa.pos.application.dto.command.ReembolsarVentaCommand;
import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.ReembolsoResponse;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller REST para gestión de ventas.
 * Endpoints: registro, consulta, listado con filtros y reembolso.
 *
 * @version 3.2.0
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final ListarVentasUseCase listarVentasUseCase;
    private final ObtenerVentaUseCase obtenerVentaUseCase;
    private final ObtenerVentaPorFacturaUseCase obtenerVentaPorFacturaUseCase;
    private final ReembolsarVentaUseCase reembolsarVentaUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase,
                           ListarVentasUseCase listarVentasUseCase,
                           ObtenerVentaUseCase obtenerVentaUseCase,
                           ObtenerVentaPorFacturaUseCase obtenerVentaPorFacturaUseCase,
                           ReembolsarVentaUseCase reembolsarVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.listarVentasUseCase = listarVentasUseCase;
        this.obtenerVentaUseCase = obtenerVentaUseCase;
        this.obtenerVentaPorFacturaUseCase = obtenerVentaPorFacturaUseCase;
        this.reembolsarVentaUseCase = reembolsarVentaUseCase;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody RegistrarVentaCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarVentaUseCase.registrar(command));
    }

    @GetMapping
    public ResponseEntity<Page<VentaResponse>> listar(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Long cajeroId,
            @RequestParam(required = false) String cedulaCliente,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FiltroVentasCommand filtro = FiltroVentasCommand.builder()
                .fecha(fecha != null ? LocalDate.parse(fecha) : null)
                .cajeroId(cajeroId)
                .cedulaCliente(cedulaCliente)
                .metodoPago(metodoPago)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(listarVentasUseCase.listar(filtro));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(obtenerVentaUseCase.obtenerPorId(id));
    }

    @GetMapping("/factura/{numeroFactura}")
    public ResponseEntity<VentaResponse> obtenerPorNumeroFactura(@PathVariable String numeroFactura) {
        return ResponseEntity.ok(obtenerVentaPorFacturaUseCase.obtenerPorNumeroFactura(numeroFactura));
    }

    @PostMapping("/{id}/reembolso")
    public ResponseEntity<ReembolsoResponse> reembolsar(@PathVariable Long id,
                                                         @Valid @RequestBody ReembolsarVentaCommand command) {
        command.setVentaId(id);
        return ResponseEntity.ok(reembolsarVentaUseCase.reembolsar(command));
    }
}
