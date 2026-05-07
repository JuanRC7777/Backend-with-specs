package com.empresa.pos.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VentaTest {

    @Test
    void calcularTotal_sumaTodosLosSubtotales() {
        Producto p1 = new Producto(1L, "Café", new BigDecimal("10.00"), 10);
        Producto p2 = new Producto(2L, "Leche", new BigDecimal("5.00"), 10);

        DetalleVenta d1 = new DetalleVenta(p1, 2); // 20.00
        DetalleVenta d2 = new DetalleVenta(p2, 3); // 15.00

        Venta venta = new Venta(1L, List.of(d1, d2));

        assertThat(venta.calcularTotal()).isEqualByComparingTo("35.00");
    }

    @Test
    void calcularTotal_retornaCero_cuandoNoHayDetalles() {
        Venta venta = new Venta(1L, List.of());
        assertThat(venta.calcularTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
