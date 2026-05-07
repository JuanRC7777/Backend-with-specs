package com.empresa.pos.domain.model;

import com.empresa.pos.domain.exception.StockInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ProductoTest {

    @Test
    void debeDescontarStockCorrectamente() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 10);
        producto.descontarStock(3);
        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void debeLanzarExcepcionCuandoStockEsInsuficiente() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 2);
        assertThatThrownBy(() -> producto.descontarStock(5))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void tieneStockSuficiente_retornaFalse_cuandoStockMenorQueCantidad() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 3);
        assertThat(producto.tieneStockSuficiente(5)).isFalse();
    }

    @Test
    void tieneStockSuficiente_retornaTrue_cuandoStockIgualACantidad() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 5);
        assertThat(producto.tieneStockSuficiente(5)).isTrue();
    }
}
