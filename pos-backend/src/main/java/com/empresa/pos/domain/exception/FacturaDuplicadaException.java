package com.empresa.pos.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear una venta con un número de factura duplicado.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class FacturaDuplicadaException extends RuntimeException {
    
    private final String numeroFactura;
    
    /**
     * Constructor con número de factura duplicado.
     * 
     * @param numeroFactura Número de factura que ya existe
     */
    public FacturaDuplicadaException(String numeroFactura) {
        super(String.format("El número de factura '%s' ya existe en el sistema", numeroFactura));
        this.numeroFactura = numeroFactura;
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param numeroFactura Número de factura duplicado
     * @param mensaje Mensaje personalizado
     */
    public FacturaDuplicadaException(String numeroFactura, String mensaje) {
        super(mensaje);
        this.numeroFactura = numeroFactura;
    }
    
    public String getNumeroFactura() {
        return numeroFactura;
    }
}
