package com.empresa.pos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entidad de dominio que representa un método de pago en una venta.
 * Una venta puede tener múltiples pagos (EFECTIVO, TARJETA, TRANSFERENCIA).
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class Pago {
    
    private Long id;
    private Long ventaId;
    private String metodoPago;  // EFECTIVO, TARJETA, TRANSFERENCIA
    private BigDecimal monto;
    
    /**
     * Constructor por defecto requerido por frameworks.
     */
    public Pago() {
    }
    
    /**
     * Constructor que crea un pago aplicando redondeo ROUND_HALF_UP a 2 decimales.
     * 
     * @param metodoPago Método de pago (EFECTIVO, TARJETA, TRANSFERENCIA)
     * @param monto Monto pagado con este método
     */
    public Pago(String metodoPago, BigDecimal monto) {
        this.metodoPago = metodoPago;
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Constructor completo para reconstrucción desde persistencia.
     */
    public Pago(Long id, Long ventaId, String metodoPago, BigDecimal monto) {
        this.id = id;
        this.ventaId = ventaId;
        this.metodoPago = metodoPago;
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVentaId() {
        return ventaId;
    }
    
    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
    }
}
