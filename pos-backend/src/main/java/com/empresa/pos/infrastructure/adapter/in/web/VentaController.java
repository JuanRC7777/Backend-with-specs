package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.ListarVentasUseCase;
import com.empresa.pos.application.port.in.venta.RegistrarVentaUseCase;
import com.empresa.pos.application.port.out.UsuarioRepositoryPort;
import com.empresa.pos.domain.model.Usuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final ListarVentasUseCase listarVentasUseCase;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final Validator validator;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase,
                            ListarVentasUseCase listarVentasUseCase,
                            UsuarioRepositoryPort usuarioRepositoryPort,
                            Validator validator) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.listarVentasUseCase = listarVentasUseCase;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@RequestBody RegistrarVentaCommand command) {
        // Obtener el usuario autenticado del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepositoryPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado: " + username));
        
        // Establecer el ID del usuario en el command
        command.setUsuarioId(usuario.getId());
        
        // Validar manualmente el command completo después de establecer el usuarioId
        Set<ConstraintViolation<RegistrarVentaCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<RegistrarVentaCommand> violation : violations) {
                errorMessage.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarVentaUseCase.registrar(command));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar() {
        return ResponseEntity.ok(listarVentasUseCase.listar());
    }
}
