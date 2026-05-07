package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.LoginCommand;
import com.empresa.pos.application.dto.response.LoginResponse;
import com.empresa.pos.application.port.in.auth.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginCommand command) {
        return ResponseEntity.ok(loginUseCase.login(command));
    }
}
