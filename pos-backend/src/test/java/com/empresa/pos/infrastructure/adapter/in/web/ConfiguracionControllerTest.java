package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.ActualizarTasaImpuestoCommand;
import com.empresa.pos.application.port.in.configuracion.ActualizarTasaImpuestoUseCase;
import com.empresa.pos.application.port.in.configuracion.ObtenerTasaImpuestoUseCase;
import com.empresa.pos.infrastructure.config.JwtUtil;
import com.empresa.pos.infrastructure.config.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ConfiguracionController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@TestPropertySource(properties = {
        "jwt.secret=testSecretKeyForJWTTestingMustBe32Chars",
        "jwt.expiration=86400000"
})
@DisplayName("ConfiguracionController - Tests")
class ConfiguracionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ObtenerTasaImpuestoUseCase obtenerTasaImpuestoUseCase;
    @MockBean private ActualizarTasaImpuestoUseCase actualizarTasaImpuestoUseCase;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    @DisplayName("8.8: obtenerTasaImpuesto debe retornar 200 con tasa actual")
    void obtenerTasaImpuesto_debeRetornar200_conTasaActual() throws Exception {
        given(obtenerTasaImpuestoUseCase.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));

        mockMvc.perform(get("/api/configuracion/tasa-impuesto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value("tasa_impuesto"))
                .andExpect(jsonPath("$.valorDecimal").value(0.05));
    }

    @Test
    @WithMockUser
    @DisplayName("8.8: actualizarTasaImpuesto debe retornar 200 cuando tasa es válida")
    void actualizarTasaImpuesto_debeRetornar200_cuandoTasaValida() throws Exception {
        ActualizarTasaImpuestoCommand command = new ActualizarTasaImpuestoCommand(new BigDecimal("0.12"));
        given(obtenerTasaImpuestoUseCase.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.12"));

        mockMvc.perform(put("/api/configuracion/tasa-impuesto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorDecimal").value(0.12));
    }

    @Test
    @WithMockUser
    @DisplayName("8.8: actualizarTasaImpuesto debe retornar 400 cuando tasa es negativa")
    void actualizarTasaImpuesto_debeRetornar400_cuandoTasaNegativa() throws Exception {
        ActualizarTasaImpuestoCommand command = new ActualizarTasaImpuestoCommand(new BigDecimal("-0.05"));

        mockMvc.perform(put("/api/configuracion/tasa-impuesto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.8: actualizarTasaImpuesto debe retornar 400 cuando tasa mayor a 1")
    void actualizarTasaImpuesto_debeRetornar400_cuandoTasaMayorA1() throws Exception {
        ActualizarTasaImpuestoCommand command = new ActualizarTasaImpuestoCommand(new BigDecimal("1.5"));

        mockMvc.perform(put("/api/configuracion/tasa-impuesto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
