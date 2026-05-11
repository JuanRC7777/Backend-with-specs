package com.empresa.pos.domain.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Servicio de dominio para generar y validar números de factura.
 * Formato: FAC-YYYYMMDD-NNNNNN (6 dígitos para la secuencia).
 * 
 * Ejemplos:
 * - FAC-20260507-000001
 * - FAC-20260507-000099
 * - FAC-20260507-999999
 * 
 * @version 3.2.0
 * @since 3.2.0
 */
public class GeneradorNumeroFactura {
    
    private static final String PREFIJO = "FAC-";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern PATRON_FORMATO = Pattern.compile("^FAC-\\d{8}-\\d{6}$");
    private static final int LONGITUD_SECUENCIA = 6;
    
    /**
     * Genera un número de factura con el formato FAC-YYYYMMDD-NNNNNN.
     * La secuencia se formatea con ceros a la izquierda para completar 6 dígitos.
     * 
     * Según RF-05.2 y RF-05.5.
     * 
     * @param fecha Fecha de la factura
     * @param secuencia Número de secuencia (1-999999)
     * @return Número de factura formateado
     * @throws IllegalArgumentException si la secuencia está fuera del rango válido
     */
    public String generar(LocalDate fecha, int secuencia) {
        if (secuencia < 1 || secuencia > 999999) {
            throw new IllegalArgumentException(
                "La secuencia debe estar entre 1 y 999999, recibido: " + secuencia
            );
        }
        
        String fechaFormateada = fecha.format(FORMATO_FECHA);
        String secuenciaFormateada = String.format("%0" + LONGITUD_SECUENCIA + "d", secuencia);
        
        return PREFIJO + fechaFormateada + "-" + secuenciaFormateada;
    }
    
    /**
     * Valida que un número de factura cumpla con el formato esperado.
     * Formato válido: FAC-YYYYMMDD-NNNNNN (exactamente 6 dígitos en la secuencia).
     * 
     * Según RF-05.5.
     * 
     * @param numeroFactura Número de factura a validar
     * @return true si el formato es válido, false en caso contrario
     */
    public boolean validarFormato(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isBlank()) {
            return false;
        }
        return PATRON_FORMATO.matcher(numeroFactura).matches();
    }
}
