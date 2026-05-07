package com.empresa.pos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Venta {
    private Long id;
    private Long usuarioId;
    private List<DetalleVenta> detalles;
    private BigDecimal total;
    private LocalDateTime fecha;

    public Venta() {}

    public Venta(Long usuarioId, List<DetalleVenta> detalles) {
        this.usuarioId = usuarioId;
        this.detalles = detalles;
        this.total = calcularTotal();
        this.fecha = LocalDateTime.now();
    }

    public BigDecimal calcularTotal() {
        if (detalles == null || detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return detalles.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}