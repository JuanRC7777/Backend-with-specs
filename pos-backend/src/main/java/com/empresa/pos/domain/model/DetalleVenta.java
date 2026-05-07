package com.empresa.pos.domain.model;

import java.math.BigDecimal;

public class DetalleVenta {
    private Long id;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnit;
    private BigDecimal subtotal;

    public DetalleVenta() {}

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnit = producto.getPrecio();
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() {
        return precioUnit.multiply(BigDecimal.valueOf(cantidad));
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnit() { return precioUnit; }
    public void setPrecioUnit(BigDecimal precioUnit) { this.precioUnit = precioUnit; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}