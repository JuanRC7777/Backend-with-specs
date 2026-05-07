package com.empresa.pos.infrastructure.adapter.in.web;

import com.empresa.pos.application.dto.command.CrearProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;
import com.empresa.pos.application.port.in.producto.*;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

@WebMvcTest(value = ProductoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@TestPropertySource(properties = {
        "jwt.secret=testSecretKeyForJWTTestingMustBe32Chars",
        "jwt.expiration=86400000"
})
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrearProductoUseCase crearProductoUseCase;

    @MockBean
    private ListarProductosUseCase listarProductosUseCase;

    @MockBean
    private ObtenerProductoUseCase obtenerProductoUseCase;

    @MockBean
    private ActualizarProductoUseCase actualizarProductoUseCase;

    @MockBean
    private EliminarProductoUseCase eliminarProductoUseCase;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    void listar_debeRetornar200_conListaDeProductos() throws Exception {
        ProductoResponse p = ProductoResponse.builder()
                .id(1L).nombre("Café").precio(new BigDecimal("10.00")).stock(20).activo(true).build();

        given(listarProductosUseCase.listar()).willReturn(List.of(p));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Café"));
    }

    @Test
    @WithMockUser
    void crear_debeRetornar201_cuandoDatosValidos() throws Exception {
        CrearProductoCommand command = CrearProductoCommand.builder()
                .nombre("Café").precio(new BigDecimal("10.00")).stock(20).build();

        ProductoResponse response = ProductoResponse.builder()
                .id(1L).nombre("Café").precio(new BigDecimal("10.00")).stock(20).activo(true).build();

        given(crearProductoUseCase.crear(any())).willReturn(response);

        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void crear_debeRetornar400_cuandoDatosInvalidos() throws Exception {
        CrearProductoCommand command = CrearProductoCommand.builder()
                .nombre("").precio(new BigDecimal("-1.00")).stock(-5).build();

        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void obtener_debeRetornar404_cuandoProductoNoExiste() throws Exception {
        given(obtenerProductoUseCase.obtener(99L))
                .willThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}
