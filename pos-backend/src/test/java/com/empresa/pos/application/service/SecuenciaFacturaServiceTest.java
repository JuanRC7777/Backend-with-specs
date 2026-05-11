package com.empresa.pos.application.service;

import com.empresa.pos.application.port.out.SecuenciaFacturaRepositoryPort;
import com.empresa.pos.application.port.out.VentaRepositoryPort;
import com.empresa.pos.domain.exception.FacturaDuplicadaException;
import com.empresa.pos.domain.exception.LimiteFacturasDiarioExcedidoException;
import com.empresa.pos.domain.service.GeneradorNumeroFactura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecuenciaFacturaService - Tests Unitarios")
class SecuenciaFacturaServiceTest {

    @Mock private SecuenciaFacturaRepositoryPort secuenciaFacturaRepository;
    @Mock private VentaRepositoryPort ventaRepository;
    @Mock private GeneradorNumeroFactura generadorNumeroFactura;

    @InjectMocks
    private SecuenciaFacturaService secuenciaFacturaService;

    @Test
    @DisplayName("5.8: debe generar formato correcto con 6 dígitos")
    void obtenerSiguienteNumeroFactura_debeGenerarFormatoCorrecto_con6Digitos() {
        given(secuenciaFacturaRepository.obtenerSiguienteNumero(any())).willReturn(1);
        given(generadorNumeroFactura.generar(any(), eq(1))).willReturn("FAC-20260508-000001");
        given(ventaRepository.existsByNumeroFactura("FAC-20260508-000001")).willReturn(false);

        String resultado = secuenciaFacturaService.obtenerSiguienteNumeroFactura();

        assertThat(resultado).isEqualTo("FAC-20260508-000001");
    }

    @Test
    @DisplayName("5.8: debe incrementar secuencia en el mismo día")
    void obtenerSiguienteNumeroFactura_debeIncrementarSecuencia_enMismoDia() {
        given(secuenciaFacturaRepository.obtenerSiguienteNumero(any())).willReturn(5);
        given(generadorNumeroFactura.generar(any(), eq(5))).willReturn("FAC-20260508-000005");
        given(ventaRepository.existsByNumeroFactura(any())).willReturn(false);

        String resultado = secuenciaFacturaService.obtenerSiguienteNumeroFactura();

        assertThat(resultado).isEqualTo("FAC-20260508-000005");
        then(secuenciaFacturaRepository).should().actualizarSecuencia(any(LocalDate.class), eq(5));
    }

    @Test
    @DisplayName("5.8: debe lanzar FacturaDuplicadaException si número ya existe")
    void obtenerSiguienteNumeroFactura_debeLanzarExcepcion_siNumeroFacturaDuplicado() {
        given(secuenciaFacturaRepository.obtenerSiguienteNumero(any())).willReturn(1);
        given(generadorNumeroFactura.generar(any(), eq(1))).willReturn("FAC-20260508-000001");
        given(ventaRepository.existsByNumeroFactura("FAC-20260508-000001")).willReturn(true);

        assertThatThrownBy(() -> secuenciaFacturaService.obtenerSiguienteNumeroFactura())
                .isInstanceOf(FacturaDuplicadaException.class);
    }

    @Test
    @DisplayName("5.8: debe lanzar LimiteFacturasDiarioExcedidoException cuando supera límite")
    void obtenerSiguienteNumeroFactura_debeLanzarLimiteFacturasDiarioExcedidoException_cuandoSuperaLimite() {
        given(secuenciaFacturaRepository.obtenerSiguienteNumero(any())).willReturn(1000000);

        assertThatThrownBy(() -> secuenciaFacturaService.obtenerSiguienteNumeroFactura())
                .isInstanceOf(LimiteFacturasDiarioExcedidoException.class);
    }

    @Test
    @DisplayName("5.8: debe actualizar secuencia en transacción")
    void obtenerSiguienteNumeroFactura_debeActualizarSecuencia_enTransaccion() {
        given(secuenciaFacturaRepository.obtenerSiguienteNumero(any())).willReturn(42);
        given(generadorNumeroFactura.generar(any(), eq(42))).willReturn("FAC-20260508-000042");
        given(ventaRepository.existsByNumeroFactura(any())).willReturn(false);

        secuenciaFacturaService.obtenerSiguienteNumeroFactura();

        then(secuenciaFacturaRepository).should().actualizarSecuencia(any(LocalDate.class), eq(42));
    }
}
