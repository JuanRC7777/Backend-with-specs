package com.empresa.pos.application.port.in.configuracion;

import java.math.BigDecimal;

/**
 * Caso de uso para actualizar la tasa de impuesto global del sistema.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface ActualizarTasaImpuestoUseCase {
    
    /**
     * Actualiza la tasa de impuesto del sistema.
     * La tasa debe estar entre 0.0 y 1.0 (0% a 100%).
     * 
     * @param nuevaTasa Nueva tasa de impuesto (ej: 0.05 para 5%)
     * @throws IllegalArgumentException si la tasa está fuera del rango válido
     */
    void actualizar(BigDecimal nuevaTasa);
}
