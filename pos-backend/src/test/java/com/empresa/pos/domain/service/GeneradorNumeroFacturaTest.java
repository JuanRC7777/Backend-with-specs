package com.empresa.pos.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para GeneradorNumeroFactura.
 * Verifica formato FAC-YYYYMMDD-NNNNNN (6 dígitos).
 */
@DisplayName("GeneradorNumeroFactura - Tests Unitarios")
class GeneradorNumeroFacturaTest {

    private GeneradorNumeroFactura generador;

    @BeforeEach
    void setUp() {
        generador = new GeneradorNumeroFactura();
    }

    @Test
    @DisplayName("3.27: generar debe retornar formato correcto FAC-YYYYMMDD-NNNNNN")
    void generar_debeRetornarFormatoCorrecto_con6Digitos() {
        LocalDate fecha = LocalDate.of(2026, 5, 8);
        String resultado = generador.generar(fecha, 1);
        assertThat(resultado).isEqualTo("FAC-20260508-000001");
    }

    @Test
    @DisplayName("3.28: validarFormato debe retornar true para formatos válidos con 6 dígitos")
    void validarFormato_debeRetornarTrue_paraFormatosValidos() {
        assertThat(generador.validarFormato("FAC-20260508-000001")).isTrue();
        assertThat(generador.validarFormato("FAC-20260508-999999")).isTrue();
        assertThat(generador.validarFormato("FAC-20260101-000001")).isTrue();
    }

    @Test
    @DisplayName("3.29: validarFormato debe retornar false para formatos inválidos")
    void validarFormato_debeRetornarFalse_paraFormatosInvalidos() {
        // 4 dígitos en secuencia
        assertThat(generador.validarFormato("FAC-20260508-0001")).isFalse();
        // 5 dígitos en secuencia
        assertThat(generador.validarFormato("FAC-20260508-00001")).isFalse();
        // 7 dígitos en secuencia
        assertThat(generador.validarFormato("FAC-20260508-0000001")).isFalse();
        // Sin prefijo
        assertThat(generador.validarFormato("20260508-000001")).isFalse();
        // Null
        assertThat(generador.validarFormato(null)).isFalse();
        // Vacío
        assertThat(generador.validarFormato("")).isFalse();
    }

    @Test
    @DisplayName("3.30: generar debe formatear secuencia con ceros a la izquierda")
    void generar_debeFormatearConCerosALaIzquierda() {
        LocalDate fecha = LocalDate.of(2026, 5, 8);
        assertThat(generador.generar(fecha, 1)).isEqualTo("FAC-20260508-000001");
        assertThat(generador.generar(fecha, 99)).isEqualTo("FAC-20260508-000099");
        assertThat(generador.generar(fecha, 999)).isEqualTo("FAC-20260508-000999");
        assertThat(generador.generar(fecha, 99999)).isEqualTo("FAC-20260508-099999");
        assertThat(generador.generar(fecha, 999999)).isEqualTo("FAC-20260508-999999");
    }

    @Test
    @DisplayName("generar debe lanzar excepción si secuencia es menor a 1")
    void generar_debeLanzarExcepcion_siSecuenciaMenorA1() {
        LocalDate fecha = LocalDate.of(2026, 5, 8);
        assertThatThrownBy(() -> generador.generar(fecha, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generar debe lanzar excepción si secuencia supera 999999")
    void generar_debeLanzarExcepcion_siSecuenciaSuperaLimite() {
        LocalDate fecha = LocalDate.of(2026, 5, 8);
        assertThatThrownBy(() -> generador.generar(fecha, 1000000))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
