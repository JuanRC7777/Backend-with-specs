package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.ActualizarProductoCommand;
import com.empresa.pos.application.dto.command.CrearProductoCommand;
import com.empresa.pos.application.dto.response.ProductoResponse;
import com.empresa.pos.application.port.in.producto.*;
import com.empresa.pos.application.port.out.ProductoRepositoryPort;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
import com.empresa.pos.domain.model.Producto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements
        CrearProductoUseCase,
        ListarProductosUseCase,
        ObtenerProductoUseCase,
        ActualizarProductoUseCase,
        EliminarProductoUseCase {

    private final ProductoRepositoryPort productoRepository;

    public ProductoService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResponse crear(CrearProductoCommand command) {
        Producto producto = new Producto();
        producto.setNombre(command.getNombre());
        producto.setDescripcion(command.getDescripcion());
        producto.setPrecio(command.getPrecio());
        producto.setStock(command.getStock());
        producto.setActivo(true);
        Producto saved = productoRepository.save(producto);
        return toResponse(saved);
    }

    @Override
    public List<ProductoResponse> listar() {
        return productoRepository.findAllActivos()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductoResponse obtener(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        return toResponse(producto);
    }

    @Override
    public ProductoResponse actualizar(Long id, ActualizarProductoCommand command) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        producto.setNombre(command.getNombre());
        producto.setDescripcion(command.getDescripcion());
        producto.setPrecio(command.getPrecio());
        producto.setStock(command.getStock());
        Producto saved = productoRepository.save(producto);
        return toResponse(saved);
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .activo(p.isActivo())
                .build();
    }
}
