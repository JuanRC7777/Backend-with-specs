package com.empresa.pos.domain.exception;

import java.time.LocalDate;

/**
 * Excepción lanzada cuando se alcanza el límite de 999,999 facturas en un día.
 * Debe mapearse a HTTP 409 CONFLICT en el GlobalExceptionHandler.
 * 
 * @version 3.2.0
 * @since 3.2.0
 */
public class LimiteFacturasDiarioExcedidoException extends RuntimeException {
    
    public static final int LIMITE_DIARIO = 999999;
    public static final String MENSAJE_DEFAULT = 
        "Se alcanzó el límite de 999,999 facturas para el período actual. Contacte al administrador.";
    
    private final LocalDate fecha;
    private final int numeroActual;
    
    /**
     * Constructor con mensaje por defecto.
     */
    public LimiteFacturasDiarioExcedidoException() {
        super(MENSAJE_DEFAULT);
        this.fecha = LocalDate.now();
        this.numeroActual = LIMITE_DIARIO;
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param mensaje Mensaje personalizado
     */
    public LimiteFacturasDiarioExcedidoException(String mensaje) {
        super(mensaje);
        this.fecha = LocalDate.now();
        this.numeroActual = LIMITE_DIARIO;
    }
    
    /**
     * Constructor con detalles de la fecha y número actual.
     * 
     * @param fecha Fecha para la cual se alcanzó el límite
     * @param numeroActual Número de secuencia actual
     */
    public LimiteFacturasDiarioExcedidoException(LocalDate fecha, int numeroActual) {
        super(String.format(
            "Se alcanzó el límite de %d facturas para el día %s (número actual: %d). " +
            "Contacte al administrador.",
            LIMITE_DIARIO, fecha, numeroActual
        ));
        this.fecha = fecha;
        this.numeroActual = numeroActual;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public int getNumeroActual() {
        return numeroActual;
    }
    
    public int getLimiteDiario() {
        return LIMITE_DIARIO;
    }
}
