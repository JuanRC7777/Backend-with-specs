package com.empresa.pos.application.service;

import com.empresa.pos.application.port.out.ConfiguracionRepositoryPort;
import com.empresa.pos.domain.model.Configuracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracionService - Tests Unitarios")
class ConfiguracionServiceTest {

    @Mock private ConfiguracionRepositoryPort configuracionRepository;

    @InjectMocks
    private ConfiguracionService configuracionService;

    @Test
    @DisplayName("5.9: obtenerTasaImpuesto debe retornar valor configurado cuando existe")
    void obtenerTasaImpuesto_debeRetornarValorConfigurado_cuandoExiste() {
        Configuracion config = new Configuracion(Configuracion.TASA_IMPUESTO_KEY, "0.12");
        given(configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY))
                .willReturn(Optional.of(config));

        BigDecimal tasa = configuracionService.obtenerTasaImpuesto();

        assertThat(tasa).isEqualByComparingTo("0.12");
    }

    @Test
    @DisplayName("5.9: obtenerTasaImpuesto debe retornar valor predefinido cuando no existe")
    void obtenerTasaImpuesto_debeRetornarValorPredefinido_cuandoNoExiste() {
        given(configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY))
                .willReturn(Optional.empty());

        BigDecimal tasa = configuracionService.obtenerTasaImpuesto();

        assertThat(tasa).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("5.9: actualizarTasaImpuesto debe actualizar valor cuando tasa es válida")
    void actualizarTasaImpuesto_debeActualizarValor_cuandoTasaValida() {
        Configuracion config = new Configuracion(Configuracion.TASA_IMPUESTO_KEY, "0.05");
        given(configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY))
                .willReturn(Optional.of(config));
        given(configuracionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        configuracionService.actualizar(new BigDecimal("0.15"));

        assertThat(config.getValor()).isEqualTo("0.15");
        then(configuracionRepository).should().save(config);
    }

    @Test
    @DisplayName("5.9: actualizarTasaImpuesto debe lanzar excepción cuando tasa está fuera de rango")
    void actualizarTasaImpuesto_debeLanzarValidationException_cuandoTasaFueraDeRango() {
        assertThatThrownBy(() -> configuracionService.actualizar(new BigDecimal("1.5")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 0.0 y 1.0");

        assertThatThrownBy(() -> configuracionService.actualizar(new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
