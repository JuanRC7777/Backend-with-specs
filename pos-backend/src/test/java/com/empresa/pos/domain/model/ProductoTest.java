package com.empresa.pos.domain.model;

import com.empresa.pos.domain.exception.StockInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para la clase Producto del dominio.
 * Valida las reglas de negocio relacionadas con el stock.
 * 
 * @version 3.2.0
 */
@DisplayName("Producto - Tests Unitarios del Dominio")
class ProductoTest {
    
    private Producto producto;
    
    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Café Premium", new BigDecimal("15000.00"), 50);
        producto.setDescripcion("Café colombiano de alta calidad");
        producto.setActivo(true);
    }
    
    // Tarea 3.14: verificar descontarStock con stock suficiente
    @Test
    @DisplayName("3.14 - Debe descontar stock cuando hay cantidad suficiente")
    void descontarStock_debeDescontarCorrectamente_cuandoHayStockSuficiente() {
        // Given
        int stockInicial = producto.getStock();
        int cantidadADescontar = 10;
        
        // When
        producto.descontarStock(cantidadADescontar);
        
        // Then
        assertThat(producto.getStock())
            .isEqualTo(stockInicial - cantidadADescontar)
            .isEqualTo(40);
    }
    
    // Tarea 3.15: verificar que lanza StockInsuficienteException con stock insuficiente
    @Test
    @DisplayName("3.15 - Debe lanzar StockInsuficienteException cuando no hay stock suficiente")
    void descontarStock_debeLanzarExcepcion_cuandoStockInsuficiente() {
        // Given
        int cantidadADescontar = 100; // Mayor que el stock disponible (50)
        
        // When & Then
        assertThatThrownBy(() -> producto.descontarStock(cantidadADescontar))
            .isInstanceOf(StockInsuficienteException.class)
            .hasMessageContaining("Stock insuficiente");
    }
    
    // Tarea 3.16: verificar tieneStockSuficiente retorna false cuando stock < cantidad
    @Test
    @DisplayName("3.16 - tieneStockSuficiente debe retornar false cuando stock es menor que cantidad")
    void tieneStockSuficiente_debeRetornarFalse_cuandoStockEsMenor() {
        // Given
        int cantidadRequerida = 100; // Mayor que el stock disponible (50)
        
        // When
        boolean resultado = producto.tieneStockSuficiente(cantidadRequerida);
        
        // Then
        assertThat(resultado).isFalse();
    }
    
    @Test
    @DisplayName("3.16 - tieneStockSuficiente debe retornar true cuando stock es igual a cantidad")
    void tieneStockSuficiente_debeRetornarTrue_cuandoStockEsIgual() {
        // Given
        int cantidadRequerida = 50; // Igual al stock disponible
        
        // When
        boolean resultado = producto.tieneStockSuficiente(cantidadRequerida);
        
        // Then
        assertThat(resultado).isTrue();
    }
    
    @Test
    @DisplayName("3.16 - tieneStockSuficiente debe retornar true cuando stock es mayor que cantidad")
    void tieneStockSuficiente_debeRetornarTrue_cuandoStockEsMayor() {
        // Given
        int cantidadRequerida = 30; // Menor que el stock disponible (50)
        
        // When
        boolean resultado = producto.tieneStockSuficiente(cantidadRequerida);
        
        // Then
        assertThat(resultado).isTrue();
    }
    
    // Tarea 3.17: verificar incrementarStock aumenta el stock correctamente (para reembolsos)
    @Test
    @DisplayName("3.17 - Debe incrementar stock correctamente para reembolsos")
    void incrementarStock_debeAumentarStock_correctamente() {
        // Given
        int stockInicial = producto.getStock();
        int cantidadAIncrementar = 20;
        
        // When
        producto.incrementarStock(cantidadAIncrementar);
        
        // Then
        assertThat(producto.getStock())
            .isEqualTo(stockInicial + cantidadAIncrementar)
            .isEqualTo(70);
    }
    
    @Test
    @DisplayName("3.17 - Debe lanzar excepción cuando se intenta incrementar con cantidad negativa")
    void incrementarStock_debeLanzarExcepcion_cuandoCantidadNegativa() {
        // Given
        int cantidadNegativa = -10;
        
        // When & Then
        assertThatThrownBy(() -> producto.incrementarStock(cantidadNegativa))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativa");
    }
    
    @Test
    @DisplayName("Debe permitir incrementar stock en producto inactivo (para reembolsos)")
    void incrementarStock_debePermitirIncrementar_enProductoInactivo() {
        // Given
        producto.setActivo(false);
        int stockInicial = producto.getStock();
        int cantidadAIncrementar = 15;
        
        // When
        producto.incrementarStock(cantidadAIncrementar);
        
        // Then
        assertThat(producto.getStock()).isEqualTo(stockInicial + cantidadAIncrementar);
        assertThat(producto.isActivo()).isFalse(); // Sigue inactivo
    }
    
    @Test
    @DisplayName("Debe descontar stock hasta llegar a cero")
    void descontarStock_debePermitirLlegarACero() {
        // Given
        int stockTotal = producto.getStock();
        
        // When
        producto.descontarStock(stockTotal);
        
        // Then
        assertThat(producto.getStock()).isZero();
    }
}
