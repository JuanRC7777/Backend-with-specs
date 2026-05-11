package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.PagoCommand;
import com.empresa.pos.application.dto.command.ItemVentaCommand;
import com.empresa.pos.application.dto.command.ReembolsarVentaCommand;
import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.PagoResponse;
import com.empresa.pos.application.dto.response.ReembolsoResponse;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.*;
import com.empresa.pos.domain.exception.*;
import com.empresa.pos.infrastructure.config.JwtUtil;
import com.empresa.pos.infrastructure.config.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = VentaController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@TestPropertySource(properties = {
        "jwt.secret=testSecretKeyForJWTTestingMustBe32Chars",
        "jwt.expiration=86400000"
})
@DisplayName("VentaController - Tests")
class VentaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegistrarVentaUseCase registrarVentaUseCase;
    @MockBean private ListarVentasUseCase listarVentasUseCase;
    @MockBean private ObtenerVentaUseCase obtenerVentaUseCase;
    @MockBean private ObtenerVentaPorFacturaUseCase obtenerVentaPorFacturaUseCase;
    @MockBean private ReembolsarVentaUseCase reembolsarVentaUseCase;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsServiceImpl userDetailsService;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private RegistrarVentaCommand buildValidCommand() {
        return RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", new BigDecimal("21.00"))))
                .build();
    }

    private VentaResponse buildVentaResponse() {
        return VentaResponse.builder()
                .id(1L)
                .numeroFactura("FAC-20260508-000001")
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .subtotal(new BigDecimal("20.00"))
                .tasaImpuesto(new BigDecimal("0.05"))
                .impuesto(new BigDecimal("1.00"))
                .total(new BigDecimal("21.00"))
                .pagos(List.of(new PagoResponse(1L, "EFECTIVO", new BigDecimal("21.00"))))
                .fecha(LocalDateTime.now())
                .reembolsada(false)
                .build();
    }

    // ─── POST /api/ventas ─────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 201 con todos los campos de facturación")
    void registrar_debeRetornar201_cuandoVentaExitosa_conTodosLosCamposDeFacturacion() throws Exception {
        given(registrarVentaUseCase.registrar(any())).willReturn(buildVentaResponse());

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCommand())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("FAC-20260508-000001"))
                .andExpect(jsonPath("$.total").value(21.00));
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando stock insuficiente")
    void registrar_debeRetornar400_cuandoStockInsuficiente() throws Exception {
        given(registrarVentaUseCase.registrar(any()))
                .willThrow(new StockInsuficienteException(1L, 5, 2));

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCommand())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando falta nombre cliente")
    void registrar_debeRetornar400_cuandoFaltaNombreCliente() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", new BigDecimal("21.00"))))
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando falta cédula cliente")
    void registrar_debeRetornar400_cuandoFaltaCedulaCliente() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", new BigDecimal("21.00"))))
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando cédula no tiene 10 dígitos")
    void registrar_debeRetornar400_cuandoCedulaNoTiene10Digitos() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("12345") // Solo 5 dígitos
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", new BigDecimal("21.00"))))
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando nombre contiene caracteres inválidos")
    void registrar_debeRetornar400_cuandoNombreContieneCaracteresInvalidos() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan123 Pérez") // Contiene números
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", new BigDecimal("21.00"))))
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando método de pago es inválido")
    void registrar_debeRetornar400_cuandoMetodoPagoInvalido() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("BITCOIN", new BigDecimal("21.00"))))
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando suma de pagos no coincide con total")
    void registrar_debeRetornar400_cuandoSumaDePagosNoCoincideConTotal() throws Exception {
        given(registrarVentaUseCase.registrar(any()))
                .willThrow(new PagosInvalidosException(new BigDecimal("21.00"), new BigDecimal("15.00")));

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCommand())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: registrar debe retornar 400 cuando lista de pagos está vacía")
    void registrar_debeRetornar400_cuandoListaDePagosVacia() throws Exception {
        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of()) // Lista vacía
                .build();

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8.7: registrar debe retornar 401 sin token")
    void registrar_debeRetornar401_sinToken() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCommand())))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/ventas/{id} ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("8.7: obtenerPorId debe retornar 200 cuando venta existe")
    void obtenerPorId_debeRetornar200_cuandoVentaExiste() throws Exception {
        given(obtenerVentaUseCase.obtenerPorId(1L)).willReturn(buildVentaResponse());

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: obtenerPorId debe retornar 404 cuando venta no existe")
    void obtenerPorId_debeRetornar404_cuandoVentaNoExiste() throws Exception {
        given(obtenerVentaUseCase.obtenerPorId(99L))
                .willThrow(new RecursoNoEncontradoException("Venta", 99L));

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/ventas/factura/{numeroFactura} ──────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("8.7: obtenerPorNumeroFactura debe retornar 200 cuando factura existe")
    void obtenerPorNumeroFactura_debeRetornar200_cuandoFacturaExiste() throws Exception {
        given(obtenerVentaPorFacturaUseCase.obtenerPorNumeroFactura("FAC-20260508-000001"))
                .willReturn(buildVentaResponse());

        mockMvc.perform(get("/api/ventas/factura/FAC-20260508-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroFactura").value("FAC-20260508-000001"));
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: obtenerPorNumeroFactura debe retornar 404 cuando factura no existe")
    void obtenerPorNumeroFactura_debeRetornar404_cuandoFacturaNoExiste() throws Exception {
        given(obtenerVentaPorFacturaUseCase.obtenerPorNumeroFactura("FAC-INVALIDA"))
                .willThrow(new RecursoNoEncontradoException("Venta", "FAC-INVALIDA"));

        mockMvc.perform(get("/api/ventas/factura/FAC-INVALIDA"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/ventas ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("8.7: listar debe retornar 200 con ventas paginadas")
    void listar_debeRetornar200_conVentasPaginadas() throws Exception {
        Page<VentaResponse> page = new PageImpl<>(List.of(buildVentaResponse()));
        given(listarVentasUseCase.listar(any())).willReturn(page);

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroFactura").value("FAC-20260508-000001"));
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: listar debe retornar 200 con ventas filtradas por fecha")
    void listar_debeRetornar200_conVentasFiltradasPorFecha() throws Exception {
        Page<VentaResponse> page = new PageImpl<>(List.of(buildVentaResponse()));
        given(listarVentasUseCase.listar(any())).willReturn(page);

        mockMvc.perform(get("/api/ventas").param("fecha", "2026-05-08"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: listar debe retornar 200 con ventas filtradas por cédula cliente")
    void listar_debeRetornar200_conVentasFiltradasPorCedulaCliente() throws Exception {
        Page<VentaResponse> page = new PageImpl<>(List.of(buildVentaResponse()));
        given(listarVentasUseCase.listar(any())).willReturn(page);

        mockMvc.perform(get("/api/ventas").param("cedulaCliente", "1234567890"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: listar debe retornar 200 con ventas filtradas por método de pago")
    void listar_debeRetornar200_conVentasFiltradasPorMetodoPago() throws Exception {
        Page<VentaResponse> page = new PageImpl<>(List.of(buildVentaResponse()));
        given(listarVentasUseCase.listar(any())).willReturn(page);

        mockMvc.perform(get("/api/ventas").param("metodoPago", "EFECTIVO"))
                .andExpect(status().isOk());
    }

    // ─── POST /api/ventas/{id}/reembolso ─────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("8.7: reembolsar debe retornar 200 cuando reembolso es exitoso")
    void reembolsar_debeRetornar200_cuandoReembolsoExitoso() throws Exception {
        ReembolsoResponse response = ReembolsoResponse.builder()
                .id(1L).ventaId(1L).motivo("Producto defectuoso recibido").build();
        given(reembolsarVentaUseCase.reembolsar(any())).willReturn(response);

        ReembolsarVentaCommand command = new ReembolsarVentaCommand(1L, "Producto defectuoso recibido", null);

        mockMvc.perform(post("/api/ventas/1/reembolso")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventaId").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: reembolsar debe retornar 400 cuando venta ya fue reembolsada")
    void reembolsar_debeRetornar400_cuandoVentaYaReembolsada() throws Exception {
        given(reembolsarVentaUseCase.reembolsar(any()))
                .willThrow(new VentaYaReembolsadaException(1L));

        ReembolsarVentaCommand command = new ReembolsarVentaCommand(1L, "Motivo de prueba largo", null);

        mockMvc.perform(post("/api/ventas/1/reembolso")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: reembolsar debe retornar 400 cuando motivo es muy corto")
    void reembolsar_debeRetornar400_cuandoMotivoMuyCorto() throws Exception {
        ReembolsarVentaCommand command = new ReembolsarVentaCommand(1L, "Corto", null); // < 10 chars

        mockMvc.perform(post("/api/ventas/1/reembolso")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("8.7: reembolsar debe retornar 404 cuando venta no existe")
    void reembolsar_debeRetornar404_cuandoVentaNoExiste() throws Exception {
        given(reembolsarVentaUseCase.reembolsar(any()))
                .willThrow(new RecursoNoEncontradoException("Venta", 99L));

        ReembolsarVentaCommand command = new ReembolsarVentaCommand(99L, "Motivo de prueba largo", null);

        mockMvc.perform(post("/api/ventas/99/reembolso")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
    }
}
