package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.DetalleVenta;
import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.domain.model.Venta;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.DetalleVentaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre Venta (dominio) y VentaEntity (JPA).
 * Incluye todos los campos de facturación: numeroFactura, cajero, cliente,
 * subtotal, tasaImpuesto, impuesto, total, reembolsada.
 *
 * @version 3.2.0
 */
@Component
public class VentaMapper {

    private final ProductoMapper productoMapper;

    public VentaMapper(ProductoMapper productoMapper) {
        this.productoMapper = productoMapper;
    }

    public VentaEntity toEntity(Venta venta) {
        VentaEntity ventaEntity = VentaEntity.builder()
                .id(venta.getId())
                .numeroFactura(venta.getNumeroFactura())
                .nombreCajero(venta.getNombreCajero())
                .nombreCliente(venta.getNombreCliente())
                .cedulaCliente(venta.getCedulaCliente())
                .subtotal(venta.getSubtotal())
                .tasaImpuesto(venta.getTasaImpuesto())
                .impuesto(venta.getImpuesto())
                .total(venta.getTotal())
                .fecha(venta.getFecha())
                .reembolsada(venta.isReembolsada())
                .build();

        if (venta.getDetalles() != null) {
            List<DetalleVentaEntity> detalles = venta.getDetalles().stream()
                    .map(d -> DetalleVentaEntity.builder()
                            .id(d.getId())
                            .venta(ventaEntity)
                            .producto(productoMapper.toEntity(d.getProducto()))
                            .cantidad(d.getCantidad())
                            .precioUnit(d.getPrecioUnit())
                            .subtotal(d.getSubtotal())
                            .build())
                    .toList();
            ventaEntity.setDetalles(detalles);
        }

        return ventaEntity;
    }

    public Venta toDomain(VentaEntity entity) {
        List<DetalleVenta> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            detalles = entity.getDetalles().stream()
                    .map(d -> {
                        ProductoEntity pe = d.getProducto();
                        Producto producto = productoMapper.toDomain(pe);
                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setId(d.getId());
                        detalle.setProducto(producto);
                        detalle.setCantidad(d.getCantidad());
                        detalle.setPrecioUnit(d.getPrecioUnit());
                        detalle.setSubtotal(d.getSubtotal());
                        return detalle;
                    })
                    .toList();
        }

        Venta venta = new Venta();
        venta.setId(entity.getId());
        venta.setNumeroFactura(entity.getNumeroFactura());
        venta.setUsuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null);
        venta.setNombreCajero(entity.getNombreCajero());
        venta.setNombreCliente(entity.getNombreCliente());
        venta.setCedulaCliente(entity.getCedulaCliente());
        venta.setDetalles(detalles);
        venta.setSubtotal(entity.getSubtotal());
        venta.setTasaImpuesto(entity.getTasaImpuesto());
        venta.setImpuesto(entity.getImpuesto());
        venta.setTotal(entity.getTotal());
        venta.setFecha(entity.getFecha());
        venta.setReembolsada(entity.isReembolsada());
        return venta;
    }
}
