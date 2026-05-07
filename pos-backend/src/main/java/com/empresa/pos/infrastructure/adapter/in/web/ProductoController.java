package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.ActualizarProductoCommand;
import com.empresa.pos.application.dto.command.CrearProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;
import com.empresa.pos.application.port.in.producto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final CrearProductoUseCase crearProductoUseCase;
    private final ListarProductosUseCase listarProductosUseCase;
    private final ObtenerProductoUseCase obtenerProductoUseCase;
    private final ActualizarProductoUseCase actualizarProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                               ListarProductosUseCase listarProductosUseCase,
                               ObtenerProductoUseCase obtenerProductoUseCase,
                               ActualizarProductoUseCase actualizarProductoUseCase,
                               EliminarProductoUseCase eliminarProductoUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.listarProductosUseCase = listarProductosUseCase;
        this.obtenerProductoUseCase = obtenerProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
        this.eliminarProductoUseCase = eliminarProductoUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(listarProductosUseCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(obtenerProductoUseCase.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crearProductoUseCase.crear(command));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ActualizarProductoCommand command) {
        return ResponseEntity.ok(actualizarProductoUseCase.actualizar(id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarProductoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
