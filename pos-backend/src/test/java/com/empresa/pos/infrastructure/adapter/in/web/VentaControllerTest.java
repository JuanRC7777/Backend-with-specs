package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.ItemVentaCommand;
import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.ListarVentasUseCase;
import com.empresa.pos.application.port.in.venta.RegistrarVentaUseCase;
import com.empresa.pos.domain.exception.StockInsuficienteException;
import com.empresa.pos.infrastructure.config.JwtUtil;
import com.empresa.pos.infrastructure.config.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

@WebMvcTest(value = VentaController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@TestPropertySource(properties = {
        "jwt.secret=testSecretKeyForJWTTestingMustBe32Chars",
        "jwt.expiration=86400000"
})
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrarVentaUseCase registrarVentaUseCase;

    @MockBean
    private ListarVentasUseCase listarVentasUseCase;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    void registrar_debeRetornar201_cuandoVentaExitosa() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .build();

        VentaResponse response = VentaResponse.builder()
                .id(1L)
                .usuarioId(1L)
                .total(new BigDecimal("20.00"))
                .fecha(LocalDateTime.now())
                .detalles(List.of())
                .build();

        given(registrarVentaUseCase.registrar(any())).willReturn(response);

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    @WithMockUser
    void registrar_debeRetornar400_cuandoStockInsuficiente() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(1L, 100)))
                .build();

        given(registrarVentaUseCase.registrar(any()))
                .willThrow(new StockInsuficienteException(1L, 100, 5));

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_debeRetornar401_sinToken() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(1L, 1)))
                .build();

        // Sin autenticación Spring Security devuelve 401 (Unauthorized)
        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }
}
