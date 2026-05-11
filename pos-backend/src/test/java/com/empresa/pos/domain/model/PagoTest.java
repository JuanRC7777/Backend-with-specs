package com.empresa.pos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la clase Pago.
 * Verifica el redondeo ROUND_HALF_UP a 2 decimales en el monto.
 * 
 * @version 3.2.0
 */
@DisplayName("Pago - Tests Unitarios")
class PagoTest {

    @Test
    @DisplayName("3.25: Constructor debe aplicar redondeo ROUND_HALF_UP a 2 decimales en el monto")
    void constructor_debeAplicarRedondeoHalfUp_enMonto() {
        // Given
        String metodoPago = "EFECTIVO";
        BigDecimal montoSinRedondear = new BigDecimal("10.125");  // 3 decimales
        
        // When
        Pago pago = new Pago(metodoPago, montoSinRedondear);
        
        // Then
        BigDecimal montoEsperado = new BigDecimal("10.13");  // Redondeado HALF_UP
        assertThat(pago.getMonto()).isEqualByComparingTo(montoEsperado);
        assertThat(pago.getMonto().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Constructor debe redondear hacia arriba cuando decimal es .125")
    void constructor_debeRedondearHaciaArriba_cuando125() {
        // Given
        BigDecimal monto = new BigDecimal("50.125");
        
        // When
        Pago pago = new Pago("TARJETA", monto);
        
        // Then
        assertThat(pago.getMonto()).isEqualByComparingTo(new BigDecimal("50.13"));
    }

    @Test
    @DisplayName("Constructor debe redondear hacia abajo cuando decimal es .124")
    void constructor_debeRedondearHaciaAbajo_cuando124() {
        // Given
        BigDecimal monto = new BigDecimal("50.124");
        
        // When
        Pago pago = new Pago("TRANSFERENCIA", monto);
        
        // Then
        assertThat(pago.getMonto()).isEqualByComparingTo(new BigDecimal("50.12"));
    }

    @Test
    @DisplayName("Constructor debe mantener 2 decimales cuando ya están redondeados")
    void constructor_debeManternerDosDecimales_cuandoYaRedondeado() {
        // Given
        BigDecimal monto = new BigDecimal("100.50");
        
        // When
        Pago pago = new Pago("EFECTIVO", monto);
        
        // Then
        assertThat(pago.getMonto())
            .isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(pago.getMonto().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Setter de monto debe aplicar redondeo ROUND_HALF_UP")
    void setMonto_debeAplicarRedondeoHalfUp() {
        // Given
        Pago pago = new Pago("EFECTIVO", BigDecimal.TEN);
        BigDecimal nuevoMonto = new BigDecimal("25.999");
        
        // When
        pago.setMonto(nuevoMonto);
        
        // Then
        assertThat(pago.getMonto()).isEqualByComparingTo(new BigDecimal("26.00"));
    }

    @Test
    @DisplayName("Constructor completo debe aplicar redondeo")
    void constructorCompleto_debeAplicarRedondeo() {
        // Given
        Long id = 1L;
        Long ventaId = 100L;
        String metodoPago = "TARJETA";
        BigDecimal monto = new BigDecimal("75.555");
        
        // When
        Pago pago = new Pago(id, ventaId, metodoPago, monto);
        
        // Then
        assertThat(pago.getId()).isEqualTo(id);
        assertThat(pago.getVentaId()).isEqualTo(ventaId);
        assertThat(pago.getMetodoPago()).isEqualTo(metodoPago);
        assertThat(pago.getMonto()).isEqualByComparingTo(new BigDecimal("75.56"));
    }
}
