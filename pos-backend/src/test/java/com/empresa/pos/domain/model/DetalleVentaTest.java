package com.empresa.pos.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DetalleVentaTest {

    @Test
    void calcularSubtotal_esPrecioMultiplicadoPorCantidad() {
        Producto producto = new Producto(1L, "Café", new BigDecimal("12.50"), 10);
        DetalleVenta detalle = new DetalleVenta(producto, 4);

        assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void subtotal_seCalculaAlCrearDetalle() {
        Producto producto = new Producto(1L, "Té", new BigDecimal("3.00"), 5);
        DetalleVenta detalle = new DetalleVenta(producto, 3);

        assertThat(detalle.getSubtotal()).isEqualByComparingTo("9.00");
    }
}
