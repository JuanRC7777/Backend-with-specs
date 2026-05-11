package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.ProductoRepositoryPort;
import com.empresa.pos.domain.model.Producto;
import com.empresa.pos.infrastructure.adapter.out.persistence.mapper.ProductoMapper;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaProductoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final JpaProductoRepository jpaRepository;
    private final ProductoMapper mapper;

    public ProductoRepositoryAdapter(JpaProductoRepository jpaRepository, ProductoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Producto> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public List<Producto> findAllActivos() {
        return jpaRepository.findAllByActivoTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Producto save(Producto producto) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(producto)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
