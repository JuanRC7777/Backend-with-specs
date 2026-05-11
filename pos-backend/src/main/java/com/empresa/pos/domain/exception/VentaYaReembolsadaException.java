package com.empresa.pos.domain.exception;

/**
 * Excepción lanzada cuando se intenta reembolsar una venta que ya fue reembolsada previamente.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class VentaYaReembolsadaException extends RuntimeException {
    
    private final Long ventaId;
    private final String numeroFactura;
    
    /**
     * Constructor con ID de venta.
     * 
     * @param ventaId ID de la venta que ya fue reembolsada
     */
    public VentaYaReembolsadaException(Long ventaId) {
        super(String.format("La venta con ID %d ya fue reembolsada previamente", ventaId));
        this.ventaId = ventaId;
        this.numeroFactura = null;
    }
    
    /**
     * Constructor con ID y número de factura.
     * 
     * @param ventaId ID de la venta
     * @param numeroFactura Número de factura de la venta
     */
    public VentaYaReembolsadaException(Long ventaId, String numeroFactura) {
        super(String.format(
            "La venta con ID %d (Factura: %s) ya fue reembolsada previamente. " +
            "Una venta solo puede reembolsarse una vez.",
            ventaId, numeroFactura
        ));
        this.ventaId = ventaId;
        this.numeroFactura = numeroFactura;
    }
    
    public Long getVentaId() {
        return ventaId;
    }
    
    public String getNumeroFactura() {
        return numeroFactura;
    }
}
