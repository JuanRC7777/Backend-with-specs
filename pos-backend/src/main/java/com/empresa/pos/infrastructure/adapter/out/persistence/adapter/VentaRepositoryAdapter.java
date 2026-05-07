package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.VentaRepositoryPort;
import com.empresa.pos.domain.model.Venta;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.mapper.VentaMapper;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaVentaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final JpaVentaRepository jpaVentaRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final VentaMapper mapper;

    public VentaRepositoryAdapter(JpaVentaRepository jpaVentaRepository,
                                   JpaUsuarioRepository jpaUsuarioRepository,
                                   VentaMapper mapper) {
        this.jpaVentaRepository = jpaVentaRepository;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.mapper = mapper;
    }

    @Override
    public Venta save(Venta venta) {
        VentaEntity entity = mapper.toEntity(venta);
        // Reemplaza el UsuarioEntity parcial por un proxy JPA válido
        entity.setUsuario(jpaUsuarioRepository.getReferenceById(venta.getUsuarioId()));
        return mapper.toDomain(jpaVentaRepository.save(entity));
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return jpaVentaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Venta> findAll() {
        return jpaVentaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
