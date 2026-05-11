package com.empresa.pos.application.service;

import com.empresa.pos.application.port.in.configuracion.ActualizarTasaImpuestoUseCase;
import com.empresa.pos.application.port.in.configuracion.ObtenerTasaImpuestoUseCase;
import com.empresa.pos.application.port.out.ConfiguracionRepositoryPort;
import com.empresa.pos.domain.model.Configuracion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio de aplicación para gestión de configuraciones del sistema.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
@Service
public class ConfiguracionService implements ObtenerTasaImpuestoUseCase, ActualizarTasaImpuestoUseCase {

    private final ConfiguracionRepositoryPort configuracionRepository;

    public ConfiguracionService(ConfiguracionRepositoryPort configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    public BigDecimal obtenerTasaImpuesto() {
        return configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY)
            .map(Configuracion::getValorComoDecimal)
            .orElse(new BigDecimal(Configuracion.TASA_IMPUESTO_DEFAULT));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void actualizar(BigDecimal nuevaTasa) {
        if (nuevaTasa.compareTo(BigDecimal.ZERO) < 0 || nuevaTasa.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("La tasa de impuesto debe estar entre 0.0 y 1.0");
        }

        Configuracion configuracion = configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY)
            .orElse(new Configuracion(Configuracion.TASA_IMPUESTO_KEY, nuevaTasa.toString()));
        
        configuracion.setValor(nuevaTasa.toString());
        configuracionRepository.save(configuracion);
    }
}
