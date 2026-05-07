package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.ItemVentaCommand;
import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.out.ProductoRepositoryPort;
import com.empresa.pos.application.port.out.VentaRepositoryPort;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
import com.empresa.pos.domain.exception.StockInsuficienteException;
import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.domain.model.Venta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private VentaRepositoryPort ventaRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void registrar_debeCalcularTotalCorrectamente_conMultiplesProductos() {
        Producto p1 = new Producto(1L, "Café", new BigDecimal("10.00"), 10);
        Producto p2 = new Producto(2L, "Leche", new BigDecimal("5.00"), 10);

        given(productoRepository.findById(1L)).willReturn(Optional.of(p1));
        given(productoRepository.findById(2L)).willReturn(Optional.of(p2));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(ventaRepository.save(any(Venta.class))).willAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(
                        new ItemVentaCommand(1L, 2),  // 20.00
                        new ItemVentaCommand(2L, 3)   // 15.00
                ))
                .build();

        VentaResponse response = ventaService.registrar(command);

        assertThat(response.getTotal()).isEqualByComparingTo("35.00");
    }

    @Test
    void registrar_debeDescontarStock_alConfirmarVenta() {
        Producto producto = new Producto(1L, "Café", new BigDecimal("10.00"), 10);

        given(productoRepository.findById(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(ventaRepository.save(any(Venta.class))).willAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(1L, 3)))
                .build();

        ventaService.registrar(command);

        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void registrar_debeLanzarStockInsuficienteException_cuandoStockEsInsuficiente() {
        Producto producto = new Producto(1L, "Café", new BigDecimal("10.00"), 2);

        given(productoRepository.findById(1L)).willReturn(Optional.of(producto));

        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(1L, 5)))
                .build();

        assertThatThrownBy(() -> ventaService.registrar(command))
                .isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoProductoNoExiste() {
        given(productoRepository.findById(99L)).willReturn(Optional.empty());

        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .usuarioId(1L)
                .items(List.of(new ItemVentaCommand(99L, 1)))
                .build();

        assertThatThrownBy(() -> ventaService.registrar(command))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
