package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.CrearProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;
import com.empresa.pos.application.port.out.ProductoRepositoryPort;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
import com.empresa.pos.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoExistente;

    @BeforeEach
    void setUp() {
        productoExistente = new Producto(1L, "Café", new BigDecimal("10.00"), 20);
        productoExistente.setDescripcion("Café molido");
        productoExistente.setActivo(true);
    }

    @Test
    void crear_debeRetornarProductoCreado_cuandoDatosValidos() {
        CrearProductoCommand command = CrearProductoCommand.builder()
                .nombre("Café")
                .descripcion("Café molido")
                .precio(new BigDecimal("10.00"))
                .stock(20)
                .build();

        given(productoRepository.save(any(Producto.class))).willReturn(productoExistente);

        ProductoResponse response = productoService.crear(command);

        assertThat(response.getNombre()).isEqualTo("Café");
        assertThat(response.getPrecio()).isEqualByComparingTo("10.00");
    }

    @Test
    void obtener_debeLanzarExcepcion_cuandoProductoNoExiste() {
        given(productoRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void eliminar_debeMarcarInactivo_cuandoProductoExiste() {
        given(productoRepository.findById(1L)).willReturn(Optional.of(productoExistente));
        given(productoRepository.save(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));

        productoService.eliminar(1L);

        assertThat(productoExistente.isActivo()).isFalse();
    }
}
