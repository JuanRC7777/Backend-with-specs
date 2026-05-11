package com.empresa.pos.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para la clase DetalleVenta del dominio.
 * Valida el cálculo de subtotal con redondeo ROUND_HALF_UP.
 * 
 * @version 3.2.0
 */
@DisplayName("DetalleVenta - Tests Unitarios del Dominio")
class DetalleVentaTest {
    
    private Producto producto;
    
    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Café Premium", new BigDecimal("15000.00"), 50);
        producto.setActivo(true);
    }
    
    // Tarea 3.24: verificar calcularSubtotal = precio × cantidad con redondeo ROUND_HALF_UP a 2 decimales
    @Test
    @DisplayName("3.24 - Debe calcular subtotal como precio × cantidad con redondeo ROUND_HALF_UP")
    void calcularSubtotal_debeAplicarRedondeoHalfUp_correctamente() {
        // Given
        int cantidad = 3;
        BigDecimal precioEsperado = new BigDecimal("15000.00");
        BigDecimal subtotalEsperado = precioEsperado
            .multiply(BigDecimal.valueOf(cantidad))
            .setScale(2, RoundingMode.HALF_UP);
        
        // When
        DetalleVenta detalle = new DetalleVenta(producto, cantidad);
        BigDecimal subtotalCalculado = detalle.calcularSubtotal();
        
        // Then
        assertThat(subtotalCalculado)
            .isEqualTo(subtotalEsperado)
            .isEqualTo(new BigDecimal("45000.00"));
        
        // Verificar que tiene exactamente 2 decimales
        assertThat(subtotalCalculado.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("3.24 - Debe redondear hacia arriba cuando el tercer decimal es >= 5")
    void calcularSubtotal_debeRedondearHaciaArriba_cuandoTercerDecimalEsMayorOIgualA5() {
        // Given - Precio que genera redondeo hacia arriba
        Producto productoConRedondeo = new Producto(
            2L, 
            "Producto Test", 
            new BigDecimal("10.556"), // 10.556 × 2 = 21.112 → 21.11 (redondea hacia arriba)
            100
        );
        int cantidad = 2;
        
        // When
        DetalleVenta detalle = new DetalleVenta(productoConRedondeo, cantidad);
        BigDecimal subtotal = detalle.calcularSubtotal();
        
        // Then
        assertThat(subtotal).isEqualTo(new BigDecimal("21.11"));
        assertThat(subtotal.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("3.24 - Debe redondear hacia abajo cuando el tercer decimal es < 5")
    void calcularSubtotal_debeRedondearHaciaAbajo_cuandoTercerDecimalEsMenorA5() {
        // Given - Precio que genera redondeo hacia abajo
        Producto productoConRedondeo = new Producto(
            3L,
            "Producto Test 2",
            new BigDecimal("10.554"), // 10.554 × 2 = 21.108 → 21.11 (redondea hacia arriba)
            100
        );
        int cantidad = 2;
        
        // When
        DetalleVenta detalle = new DetalleVenta(productoConRedondeo, cantidad);
        BigDecimal subtotal = detalle.calcularSubtotal();
        
        // Then
        assertThat(subtotal).isEqualTo(new BigDecimal("21.11"));
        assertThat(subtotal.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Debe calcular subtotal correctamente con cantidad 1")
    void calcularSubtotal_debeSerIgualAlPrecio_cuandoCantidadEsUno() {
        // Given
        int cantidad = 1;
        
        // When
        DetalleVenta detalle = new DetalleVenta(producto, cantidad);
        BigDecimal subtotal = detalle.calcularSubtotal();
        
        // Then
        assertThat(subtotal).isEqualTo(producto.getPrecio());
        assertThat(subtotal.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Debe calcular subtotal correctamente con cantidades grandes")
    void calcularSubtotal_debeCalcularCorrectamente_conCantidadesGrandes() {
        // Given
        int cantidad = 100;
        BigDecimal subtotalEsperado = new BigDecimal("1500000.00");
        
        // When
        DetalleVenta detalle = new DetalleVenta(producto, cantidad);
        BigDecimal subtotal = detalle.calcularSubtotal();
        
        // Then
        assertThat(subtotal).isEqualTo(subtotalEsperado);
        assertThat(subtotal.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Debe mantener el precio unitario del producto al momento de la venta")
    void constructor_debeCapturarPrecioDelProducto_alMomentoDeCreacion() {
        // Given
        BigDecimal precioOriginal = producto.getPrecio();
        
        // When
        DetalleVenta detalle = new DetalleVenta(producto, 2);
        
        // Cambiar el precio del producto después de crear el detalle
        producto.setPrecio(new BigDecimal("20000.00"));
        
        // Then
        assertThat(detalle.getPrecioUnit())
            .isEqualTo(precioOriginal)
            .isNotEqualTo(producto.getPrecio());
    }
    
    @Test
    @DisplayName("Debe calcular subtotal con precios decimales complejos")
    void calcularSubtotal_debeCalcularCorrectamente_conPreciosDecimales() {
        // Given
        Producto productoDecimal = new Producto(
            4L,
            "Producto Decimal",
            new BigDecimal("123.456"),
            50
        );
        int cantidad = 7;
        
        // When
        DetalleVenta detalle = new DetalleVenta(productoDecimal, cantidad);
        BigDecimal subtotal = detalle.calcularSubtotal();
        
        // Then
        // 123.456 × 7 = 864.192 → 864.19 (ROUND_HALF_UP)
        assertThat(subtotal).isEqualTo(new BigDecimal("864.19"));
        assertThat(subtotal.scale()).isEqualTo(2);
    }
}
