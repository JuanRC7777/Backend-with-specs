package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.ReembolsoRepositoryPort;
import com.empresa.pos.domain.model.Reembolso;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ReembolsoEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.mapper.ReembolsoMapper;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaReembolsoRepository;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaVentaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReembolsoRepositoryAdapter implements ReembolsoRepositoryPort {

    private final JpaReembolsoRepository jpaReembolsoRepository;
    private final JpaVentaRepository jpaVentaRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final ReembolsoMapper mapper;

    public ReembolsoRepositoryAdapter(JpaReembolsoRepository jpaReembolsoRepository,
                                       JpaVentaRepository jpaVentaRepository,
                                       JpaUsuarioRepository jpaUsuarioRepository,
                                       ReembolsoMapper mapper) {
        this.jpaReembolsoRepository = jpaReembolsoRepository;
        this.jpaVentaRepository = jpaVentaRepository;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.mapper = mapper;
    }

    @Override
    public Reembolso save(Reembolso reembolso) {
        ReembolsoEntity entity = mapper.toEntity(reembolso);
        
        if (reembolso.getVentaId() != null) {
            entity.setVenta(jpaVentaRepository.getReferenceById(reembolso.getVentaId()));
        }
        if (reembolso.getUsuarioId() != null) {
            entity.setUsuario(jpaUsuarioRepository.getReferenceById(reembolso.getUsuarioId()));
        }
        
        return mapper.toDomain(jpaReembolsoRepository.save(entity));
    }

    @Override
    public Optional<Reembolso> findByVentaId(Long ventaId) {
        return jpaReembolsoRepository.findByVentaId(ventaId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByVentaId(Long ventaId) {
        return jpaReembolsoRepository.existsByVentaId(ventaId);
    }
}
