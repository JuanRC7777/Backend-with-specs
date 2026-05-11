package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Venta;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {

    Venta save(Venta venta);

    Optional<Venta> findById(Long id);
    
    /**
     * Busca una venta por su número de factura.
     * 
     * @param numeroFactura Número de factura (formato: FAC-YYYYMMDD-NNNNNN)
     * @return Venta si existe
     */
    Optional<Venta> findByNumeroFactura(String numeroFactura);
    
    /**
     * Verifica si existe una venta con el número de factura dado.
     * 
     * @param numeroFactura Número de factura a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNumeroFactura(String numeroFactura);

    List<Venta> findAll();
    
    /**
     * Lista ventas con paginación y filtros dinámicos.
     * 
     * @param filtro Objeto con criterios de filtrado (fecha, cajero, cédula, método de pago)
     * @return Página de ventas que cumplen los criterios
     */
    Page<Venta> findAll(FiltroVentas filtro);
    
    /**
     * Clase interna para encapsular los filtros de búsqueda de ventas.
     */
    class FiltroVentas {
        private java.time.LocalDate fecha;
        private Long cajeroId;
        private String cedulaCliente;
        private String metodoPago;
        private int page;
        private int size;
        
        public FiltroVentas() {
            this.page = 0;
            this.size = 20;
        }
        
        // Getters y Setters
        public java.time.LocalDate getFecha() { return fecha; }
        public void setFecha(java.time.LocalDate fecha) { this.fecha = fecha; }
        
        public Long getCajeroId() { return cajeroId; }
        public void setCajeroId(Long cajeroId) { this.cajeroId = cajeroId; }
        
        public String getCedulaCliente() { return cedulaCliente; }
        public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }
        
        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
        
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
