package com.empresa.pos.domain.model;

import java.math.BigDecimal;

/**
 * Entidad de dominio que representa la configuración global del sistema.
 * Almacena parámetros configurables como la tasa de impuesto.
 * 
 * @version 3.2.0
 * @since 3.1.0
 */
public class Configuracion {
    
    public static final String TASA_IMPUESTO_KEY = "tasa_impuesto";
    public static final String TASA_IMPUESTO_DEFAULT = "0.05";  // 5%
    
    private Long id;
    private String clave;
    private String valor;
    
    /**
     * Constructor por defecto requerido por frameworks.
     */
    public Configuracion() {
    }
    
    /**
     * Constructor para crear una nueva configuración.
     * 
     * @param clave Clave de configuración (ej: "tasa_impuesto")
     * @param valor Valor de la configuración
     */
    public Configuracion(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }
    
    /**
     * Constructor completo para reconstrucción desde persistencia.
     */
    public Configuracion(Long id, String clave, String valor) {
        this.id = id;
        this.clave = clave;
        this.valor = valor;
    }
    
    /**
     * Convierte el valor String a BigDecimal.
     * Útil para configuraciones numéricas como la tasa de impuesto.
     * 
     * @return Valor como BigDecimal
     * @throws NumberFormatException si el valor no es un número válido
     */
    public BigDecimal getValorComoDecimal() {
        return new BigDecimal(valor);
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getClave() {
        return clave;
    }
    
    public void setClave(String clave) {
        this.clave = clave;
    }
    
    public String getValor() {
        return valor;
    }
    
    public void setValor(String valor) {
        this.valor = valor;
    }
}
