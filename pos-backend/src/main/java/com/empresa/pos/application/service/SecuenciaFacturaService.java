package com.empresa.pos.application.service;

import com.empresa.pos.application.port.out.SecuenciaFacturaRepositoryPort;
import com.empresa.pos.application.port.out.VentaRepositoryPort;
import com.empresa.pos.domain.exception.FacturaDuplicadaException;
import com.empresa.pos.domain.exception.LimiteFacturasDiarioExcedidoException;
import com.empresa.pos.domain.service.GeneradorNumeroFactura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio de aplicación para gestión de secuencias de números de factura.
 * Garantiza unicidad y secuencialidad de números de factura por día.
 *
 * @version 3.2.0
 */
@Service
public class SecuenciaFacturaService {

    private static final Logger log = LoggerFactory.getLogger(SecuenciaFacturaService.class);
    private static final int LIMITE_FACTURAS_DIARIO = 999999;

    private final SecuenciaFacturaRepositoryPort secuenciaFacturaRepository;
    private final VentaRepositoryPort ventaRepository;
    private final GeneradorNumeroFactura generadorNumeroFactura;

    public SecuenciaFacturaService(SecuenciaFacturaRepositoryPort secuenciaFacturaRepository,
                                    VentaRepositoryPort ventaRepository,
                                    GeneradorNumeroFactura generadorNumeroFactura) {
        this.secuenciaFacturaRepository = secuenciaFacturaRepository;
        this.ventaRepository = ventaRepository;
        this.generadorNumeroFactura = generadorNumeroFactura;
    }

    /**
     * Obtiene el siguiente número de factura único para el día actual.
     * Formato: FAC-YYYYMMDD-NNNNNN (6 dígitos).
     * 
     * @return Número de factura único
     * @throws LimiteFacturasDiarioExcedidoException si se supera el límite de 999,999 facturas por día
     * @throws FacturaDuplicadaException si el número generado ya existe (caso extremadamente raro)
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String obtenerSiguienteNumeroFactura() {
        LocalDate fechaActual = LocalDate.now();
        
        // Obtener siguiente número de secuencia con SELECT FOR UPDATE
        int siguienteNumero = secuenciaFacturaRepository.obtenerSiguienteNumero(fechaActual);
        
        // Validar límite diario
        if (siguienteNumero > LIMITE_FACTURAS_DIARIO) {
            throw new LimiteFacturasDiarioExcedidoException(fechaActual, LIMITE_FACTURAS_DIARIO);
        }

        // Logging al 90% del límite (RF-05.9)
        if (siguienteNumero >= 899999) {
            log.warn("ALERTA: Se ha alcanzado el 90% del límite de facturas diarias. " +
                     "Número actual: {}, Límite: {}, Fecha: {}", siguienteNumero, LIMITE_FACTURAS_DIARIO, fechaActual);
        }
        
        // Generar número de factura
        String numeroFactura = generadorNumeroFactura.generar(fechaActual, siguienteNumero);
        
        // Verificar unicidad (doble verificación por seguridad)
        if (ventaRepository.existsByNumeroFactura(numeroFactura)) {
            throw new FacturaDuplicadaException(numeroFactura);
        }
        
        // Actualizar secuencia
        secuenciaFacturaRepository.actualizarSecuencia(fechaActual, siguienteNumero);
        
        return numeroFactura;
    }
}
