package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.DetalleVenta;
import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.domain.model.Venta;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.DetalleVentaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VentaMapper {

    private final ProductoMapper productoMapper;

    public VentaMapper(ProductoMapper productoMapper) {
        this.productoMapper = productoMapper;
    }

    public VentaEntity toEntity(Venta venta) {
        VentaEntity ventaEntity = VentaEntity.builder()
                .id(venta.getId())
                .total(venta.getTotal())
                .fecha(venta.getFecha())
                .build();

        List<DetalleVentaEntity> detalles = venta.getDetalles().stream()
                .map(d -> DetalleVentaEntity.builder()
                        .venta(ventaEntity)
                        .producto(productoMapper.toEntity(d.getProducto()))
                        .cantidad(d.getCantidad())
                        .precioUnit(d.getPrecioUnit())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        ventaEntity.setDetalles(detalles);
        return ventaEntity;
    }

    public Venta toDomain(VentaEntity entity) {
        List<DetalleVenta> detalles = entity.getDetalles().stream()
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

        Venta venta = new Venta();
        venta.setId(entity.getId());
        venta.setUsuarioId(entity.getUsuario().getId());
        venta.setDetalles(detalles);
        venta.setTotal(entity.getTotal());
        venta.setFecha(entity.getFecha());
        return venta;
    }
}
