package com.empresa.pos.domain.model;

import com.empresa.pos.domain.exception.PagosInvalidosException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa una venta con facturación completa.
 * Incluye cálculos de subtotal, impuesto y total con redondeo ROUND_HALF_UP.
 * Soporta múltiples métodos de pago y reembolsos.
 * 
 * @version 3.2.0
 */
public class Venta {
    private Long id;
    private String numeroFactura;
    private Long usuarioId;
    private String nombreCajero;
    private String nombreCliente;
    private String cedulaCliente;
    private List<DetalleVenta> detalles;
    private List<Pago> pagos;
    private BigDecimal subtotal;
    private BigDecimal tasaImpuesto;
    private BigDecimal impuesto;
    private BigDecimal total;
    private LocalDateTime fecha;
    private boolean reembolsada;

    public Venta() {
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();
        this.reembolsada = false;
    }

    /**
     * Constructor simplificado para compatibilidad con código existente.
     * @deprecated Usar constructor completo con todos los campos
     */
    @Deprecated
    public Venta(Long usuarioId, List<DetalleVenta> detalles) {
        this.usuarioId = usuarioId;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.pagos = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.reembolsada = false;
        // Calcular total simple para compatibilidad
        this.total = detalles != null ? detalles.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
    }

    public Venta(Long usuarioId, String nombreCajero, String nombreCliente, String cedulaCliente, 
                 List<DetalleVenta> detalles, List<Pago> pagos, BigDecimal tasaImpuesto) {
        this.usuarioId = usuarioId;
        this.nombreCajero = nombreCajero;
        this.nombreCliente = nombreCliente;
        this.cedulaCliente = cedulaCliente;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.pagos = pagos != null ? pagos : new ArrayList<>();
        this.tasaImpuesto = tasaImpuesto;
        this.fecha = LocalDateTime.now();
        this.reembolsada = false;
        calcularTotales();
    }

    /**
     * Calcula el subtotal como la suma de todos los subtotales de detalles ya redondeados.
     * Según RF-04.1 y RF-04.2.
     * 
     * @return Subtotal de la venta
     */
    public BigDecimal calcularSubtotal() {
        if (detalles == null || detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return detalles.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el impuesto como subtotal × tasaImpuesto con redondeo ROUND_HALF_UP a 2 decimales.
     * Según RF-04.3 y RF-04.7.
     * 
     * @param subtotal Subtotal de la venta
     * @param tasaImpuesto Tasa de impuesto (ej: 0.05 para 5%)
     * @return Impuesto redondeado a 2 decimales
     */
    public BigDecimal calcularImpuesto(BigDecimal subtotal, BigDecimal tasaImpuesto) {
        if (subtotal == null || tasaImpuesto == null) {
            return BigDecimal.ZERO;
        }
        return subtotal
            .multiply(tasaImpuesto)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total como subtotal + impuesto con redondeo ROUND_HALF_UP a 2 decimales.
     * Según RF-04.4 y RF-04.7.
     * 
     * @param subtotal Subtotal de la venta
     * @param impuesto Impuesto calculado
     * @return Total redondeado a 2 decimales
     */
    public BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal impuesto) {
        if (subtotal == null || impuesto == null) {
            return BigDecimal.ZERO;
        }
        return subtotal
            .add(impuesto)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Orquesta el cálculo completo de subtotal, impuesto y total.
     * Actualiza los campos de la venta con los valores calculados.
     * Según RF-04.1, RF-04.2, RF-04.3, RF-04.4.
     */
    public void calcularTotales() {
        this.subtotal = calcularSubtotal();
        this.impuesto = calcularImpuesto(this.subtotal, this.tasaImpuesto);
        this.total = calcularTotal(this.subtotal, this.impuesto);
    }

    /**
     * Valida que la suma de todos los pagos sea exactamente igual al total de la venta.
     * Lanza PagosInvalidosException si no coinciden.
     * Según RF-03.5.
     * 
     * @throws PagosInvalidosException si la suma de pagos no coincide con el total
     */
    public void validarPagos() {
        if (pagos == null || pagos.isEmpty()) {
            throw new PagosInvalidosException(total, BigDecimal.ZERO);
        }

        BigDecimal sumaPagos = pagos.stream()
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Redondear ambos a 2 decimales con ROUND_HALF_UP antes de comparar (RF-03.5.1, RF-03.5.2)
        BigDecimal totalRedondeado = total.setScale(2, RoundingMode.HALF_UP);
        BigDecimal sumaPagosRedondeada = sumaPagos.setScale(2, RoundingMode.HALF_UP);

        if (totalRedondeado.compareTo(sumaPagosRedondeada) != 0) {
            throw new PagosInvalidosException(totalRedondeado, sumaPagosRedondeada);
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreCajero() { return nombreCajero; }
    public void setNombreCajero(String nombreCajero) { this.nombreCajero = nombreCajero; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTasaImpuesto() { return tasaImpuesto; }
    public void setTasaImpuesto(BigDecimal tasaImpuesto) { this.tasaImpuesto = tasaImpuesto; }

    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isReembolsada() { return reembolsada; }
    public void setReembolsada(boolean reembolsada) { this.reembolsada = reembolsada; }
}