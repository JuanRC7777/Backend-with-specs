package com.empresa.pos.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un reembolso de una venta.
 * Un reembolso devuelve el stock de todos los productos y marca la venta como reembolsada.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class Reembolso {
    
    private Long id;
    private Long ventaId;
    private String motivo;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String nombreUsuario;
    
    /**
     * Constructor por defecto requerido por frameworks.
     */
    public Reembolso() {
    }
    
    /**
     * Constructor que crea un reembolso asignando automáticamente la fecha actual.
     * 
     * @param ventaId ID de la venta que se reembolsa
     * @param motivo Motivo del reembolso (10-500 caracteres)
     * @param usuarioId ID del usuario que autoriza el reembolso
     * @param nombreUsuario Username del usuario que autoriza
     */
    public Reembolso(Long ventaId, String motivo, Long usuarioId, String nombreUsuario) {
        this.ventaId = ventaId;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
    }
    
    /**
     * Constructor completo para reconstrucción desde persistencia.
     */
    public Reembolso(Long id, Long ventaId, String motivo, LocalDateTime fecha, 
                     Long usuarioId, String nombreUsuario) {
        this.id = id;
        this.ventaId = ventaId;
        this.motivo = motivo;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
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
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
