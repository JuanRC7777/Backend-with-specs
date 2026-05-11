package com.empresa.pos.application.port.in.configuracion;

import java.math.BigDecimal;

/**
 * Caso de uso para obtener la tasa de impuesto global del sistema.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public interface ObtenerTasaImpuestoUseCase {
    
    /**
     * Obtiene la tasa de impuesto configurada en el sistema.
     * Si no existe configuración, retorna el valor predefinido (0.05 = 5%).
     * 
     * @return Tasa de impuesto como BigDecimal (ej: 0.05 para 5%)
     */
    BigDecimal obtenerTasaImpuesto();
}
