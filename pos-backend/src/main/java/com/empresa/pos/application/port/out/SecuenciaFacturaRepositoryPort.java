package com.empresa.pos.application.port.out;

import java.time.LocalDate;

/**
 * Puerto de salida para gestión de secuencias de números de factura.
 * Garantiza unicidad y secuencialidad de números de factura por día.
 * 
 * @version 3.2.0
 */
public interface SecuenciaFacturaRepositoryPort {
    
    /**
     * Obtiene el siguiente número de secuencia para una fecha.
     * Si no existe registro para la fecha, crea uno nuevo con secuencia 1.
     * Si existe, retorna ultimoNumero + 1.
     * 
     * Debe ejecutarse con SELECT FOR UPDATE para evitar condiciones de carrera.
     * 
     * @param fecha Fecha para la cual se genera la secuencia
     * @return Siguiente número de secuencia (1-999999)
     */
    int obtenerSiguienteNumero(LocalDate fecha);
    
    /**
     * Actualiza el último número de secuencia usado para una fecha.
     * 
     * @param fecha Fecha de la secuencia
     * @param numero Último número usado
     */
    void actualizarSecuencia(LocalDate fecha, int numero);
}
