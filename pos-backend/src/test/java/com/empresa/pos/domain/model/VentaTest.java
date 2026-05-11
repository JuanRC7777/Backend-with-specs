package com.empresa.pos.domain.model;

import com.empresa.pos.domain.exception.PagosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para la clase Venta del dominio.
 * Verifica cálculos de subtotal, impuesto, total y validación de pagos.
 *
 * @version 3.2.0
 */
@DisplayName("Venta - Tests Unitarios del Dominio")
class VentaTest {

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto(1L, "Café", new BigDecimal("10.00"), 100);
        producto2 = new Producto(2L, "Leche", new BigDecimal("5.00"), 100);
    }

    // ─── 3.18 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.18: calcularSubtotal debe sumar correctamente todos los subtotales de detalles")
    void calcularSubtotal_debeSumarTodosLosSubtotales() {
        DetalleVenta d1 = new DetalleVenta(producto1, 2); // 20.00
        DetalleVenta d2 = new DetalleVenta(producto2, 3); // 15.00

        Venta venta = new Venta();
        venta.setDetalles(List.of(d1, d2));

        assertThat(venta.calcularSubtotal()).isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("3.18: calcularSubtotal debe retornar cero cuando no hay detalles")
    void calcularSubtotal_debeRetornarCero_sinDetalles() {
        Venta venta = new Venta();
        venta.setDetalles(List.of());
        assertThat(venta.calcularSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─── 3.19 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.19: calcularImpuesto debe retornar subtotal × tasaImpuesto con ROUND_HALF_UP")
    void calcularImpuesto_debeRetornarSubtotalPorTasa_conRedondeo() {
        Venta venta = new Venta();
        BigDecimal subtotal = new BigDecimal("35.00");
        BigDecimal tasa = new BigDecimal("0.05");

        BigDecimal impuesto = venta.calcularImpuesto(subtotal, tasa);

        // 35.00 × 0.05 = 1.75
        assertThat(impuesto).isEqualByComparingTo("1.75");
    }

    @Test
    @DisplayName("3.19: calcularImpuesto debe aplicar ROUND_HALF_UP cuando hay más de 2 decimales")
    void calcularImpuesto_debeAplicarRedondeoHalfUp() {
        Venta venta = new Venta();
        // 33.33 × 0.05 = 1.6665 → 1.67 (ROUND_HALF_UP)
        BigDecimal subtotal = new BigDecimal("33.33");
        BigDecimal tasa = new BigDecimal("0.05");

        BigDecimal impuesto = venta.calcularImpuesto(subtotal, tasa);

        assertThat(impuesto).isEqualByComparingTo("1.67");
    }

    // ─── 3.20 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.20: calcularTotal debe retornar subtotal + impuesto con ROUND_HALF_UP")
    void calcularTotal_debeRetornarSubtotalMasImpuesto_conRedondeo() {
        Venta venta = new Venta();
        BigDecimal subtotal = new BigDecimal("35.00");
        BigDecimal impuesto = new BigDecimal("1.75");

        BigDecimal total = venta.calcularTotal(subtotal, impuesto);

        assertThat(total).isEqualByComparingTo("36.75");
    }

    @Test
    @DisplayName("3.20: calcularTotal debe tener escala 2")
    void calcularTotal_debeTenerEscalaDos() {
        Venta venta = new Venta();
        BigDecimal total = venta.calcularTotal(new BigDecimal("10.00"), new BigDecimal("0.50"));
        assertThat(total.scale()).isEqualTo(2);
    }

    // ─── 3.21 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.21: calcularTotales debe actualizar correctamente subtotal, impuesto y total")
    void calcularTotales_debeActualizarTodosLosCampos() {
        DetalleVenta d1 = new DetalleVenta(producto1, 2); // 20.00
        DetalleVenta d2 = new DetalleVenta(producto2, 3); // 15.00

        Venta venta = new Venta();
        venta.setDetalles(List.of(d1, d2));
        venta.setTasaImpuesto(new BigDecimal("0.05"));

        venta.calcularTotales();

        // subtotal = 35.00, impuesto = 1.75, total = 36.75
        assertThat(venta.getSubtotal()).isEqualByComparingTo("35.00");
        assertThat(venta.getImpuesto()).isEqualByComparingTo("1.75");
        assertThat(venta.getTotal()).isEqualByComparingTo("36.75");
    }

    // ─── 3.22 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.22: validarPagos no debe lanzar excepción cuando suma de pagos = total")
    void validarPagos_noDebeLanzarExcepcion_cuandoSumaIgualAlTotal() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("36.75"));
        venta.setPagos(List.of(
            new Pago("EFECTIVO", new BigDecimal("20.00")),
            new Pago("TARJETA", new BigDecimal("16.75"))
        ));

        assertThatNoException().isThrownBy(venta::validarPagos);
    }

    @Test
    @DisplayName("3.22: validarPagos no debe lanzar excepción con un solo pago igual al total")
    void validarPagos_noDebeLanzarExcepcion_conUnSoloPago() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("50.00"));
        venta.setPagos(List.of(new Pago("EFECTIVO", new BigDecimal("50.00"))));

        assertThatNoException().isThrownBy(venta::validarPagos);
    }

    // ─── 3.23 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.23: validarPagos debe lanzar PagosInvalidosException cuando suma ≠ total")
    void validarPagos_debeLanzarPagosInvalidosException_cuandoSumaDiferenteAlTotal() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("36.75"));
        venta.setPagos(List.of(
            new Pago("EFECTIVO", new BigDecimal("20.00"))
            // Falta 16.75
        ));

        assertThatThrownBy(venta::validarPagos)
            .isInstanceOf(PagosInvalidosException.class);
    }

    @Test
    @DisplayName("3.23: validarPagos debe lanzar excepción cuando pagos están vacíos")
    void validarPagos_debeLanzarExcepcion_cuandoPagosVacios() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("36.75"));
        venta.setPagos(List.of());

        assertThatThrownBy(venta::validarPagos)
            .isInstanceOf(PagosInvalidosException.class);
    }

    @Test
    @DisplayName("3.23: validarPagos debe lanzar excepción cuando suma excede el total")
    void validarPagos_debeLanzarExcepcion_cuandoSumaExcedeTotal() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("36.75"));
        venta.setPagos(List.of(
            new Pago("EFECTIVO", new BigDecimal("50.00")) // Excede el total
        ));

        assertThatThrownBy(venta::validarPagos)
            .isInstanceOf(PagosInvalidosException.class);
    }

    // ─── Compatibilidad con constructor legacy ────────────────────────────────

    @Test
    @DisplayName("Constructor legacy debe calcular total simple")
    void constructorLegacy_debeCalcularTotalSimple() {
        DetalleVenta d1 = new DetalleVenta(producto1, 2); // 20.00
        DetalleVenta d2 = new DetalleVenta(producto2, 3); // 15.00

        Venta venta = new Venta(1L, List.of(d1, d2));

        // El constructor legacy calcula el total como suma de subtotales
        assertThat(venta.getTotal()).isEqualByComparingTo("35.00");
    }
}
