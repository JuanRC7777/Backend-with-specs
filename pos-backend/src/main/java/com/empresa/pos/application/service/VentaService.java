package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.DetalleVentaResponse;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.ListarVentasUseCase;
import com.empresa.pos.application.port.in.venta.RegistrarVentaUseCase;
import com.empresa.pos.application.port.out.ProductoRepositoryPort;
import com.empresa.pos.application.port.out.VentaRepositoryPort;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
import com.empresa.pos.domain.model.DetalleVenta;
import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.domain.model.Venta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaService implements RegistrarVentaUseCase, ListarVentasUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final VentaRepositoryPort ventaRepository;

    public VentaService(ProductoRepositoryPort productoRepository, VentaRepositoryPort ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional
    public VentaResponse registrar(RegistrarVentaCommand command) {
        List<DetalleVenta> detalles = command.getItems().stream().map(item -> {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));
            producto.descontarStock(item.getCantidad());
            productoRepository.save(producto);
            return new DetalleVenta(producto, item.getCantidad());
        }).toList();

        Venta venta = new Venta(command.getUsuarioId(), detalles);
        Venta saved = ventaRepository.save(venta);
        return toResponse(saved);
    }

    @Override
    public List<VentaResponse> listar() {
        return ventaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private VentaResponse toResponse(Venta v) {
        List<DetalleVentaResponse> detallesResponse = v.getDetalles().stream()
                .map(d -> DetalleVentaResponse.builder()
                        .productoId(d.getProducto().getId())
                        .nombreProducto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnit(d.getPrecioUnit())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        return VentaResponse.builder()
                .id(v.getId())
                .usuarioId(v.getUsuarioId())
                .detalles(detallesResponse)
                .total(v.getTotal())
                .fecha(v.getFecha())
                .build();
    }
}
