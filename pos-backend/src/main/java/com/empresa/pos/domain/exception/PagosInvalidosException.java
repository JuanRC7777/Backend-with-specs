package com.empresa.pos.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando la suma de los pagos no coincide con el total de la venta.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class PagosInvalidosException extends RuntimeException {
    
    private final BigDecimal totalEsperado;
    private final BigDecimal totalPagos;
    
    /**
     * Constructor con mensaje simple.
     * 
     * @param mensaje Mensaje descriptivo del error
     */
    public PagosInvalidosException(String mensaje) {
        super(mensaje);
        this.totalEsperado = null;
        this.totalPagos = null;
    }
    
    /**
     * Constructor con detalles de los montos.
     * 
     * @param totalEsperado Total esperado de la venta
     * @param totalPagos Suma de todos los pagos
     */
    public PagosInvalidosException(BigDecimal totalEsperado, BigDecimal totalPagos) {
        super(String.format(
            "La suma de pagos (%.2f) no coincide con el total de la venta (%.2f). " +
            "Diferencia: %.2f",
            totalPagos, totalEsperado, totalPagos.subtract(totalEsperado).abs()
        ));
        this.totalEsperado = totalEsperado;
        this.totalPagos = totalPagos;
    }
    
    public BigDecimal getTotalEsperado() {
        return totalEsperado;
    }
    
    public BigDecimal getTotalPagos() {
        return totalPagos;
    }
}
