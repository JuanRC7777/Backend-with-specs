package com.empresa.pos.infrastructure.adapter.out.persistence.mapper;

import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoEntity toEntity(Producto producto) {
        return ProductoEntity.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .activo(producto.isActivo())
                .build();
    }

    public Producto toDomain(ProductoEntity entity) {
        Producto producto = new Producto();
        producto.setId(entity.getId());
        producto.setNombre(entity.getNombre());
        producto.setDescripcion(entity.getDescripcion());
        producto.setPrecio(entity.getPrecio());
        producto.setStock(entity.getStock());
        producto.setActivo(entity.isActivo());
        return producto;
    }
}
