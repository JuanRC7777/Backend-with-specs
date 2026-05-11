package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.PagoVentaRepositoryPort;
import com.empresa.pos.domain.model.Pago;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.PagoVentaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.mapper.PagoMapper;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaPagoVentaRepository;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaVentaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PagoVentaRepositoryAdapter implements PagoVentaRepositoryPort {

    private final JpaPagoVentaRepository jpaPagoVentaRepository;
    private final JpaVentaRepository jpaVentaRepository;
    private final PagoMapper mapper;

    public PagoVentaRepositoryAdapter(JpaPagoVentaRepository jpaPagoVentaRepository,
                                       JpaVentaRepository jpaVentaRepository,
                                       PagoMapper mapper) {
        this.jpaPagoVentaRepository = jpaPagoVentaRepository;
        this.jpaVentaRepository = jpaVentaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Pago> saveAll(List<Pago> pagos) {
        List<PagoVentaEntity> entities = pagos.stream()
            .map(pago -> {
                PagoVentaEntity entity = mapper.toEntity(pago);
                if (pago.getVentaId() != null) {
                    entity.setVenta(jpaVentaRepository.getReferenceById(pago.getVentaId()));
                }
                return entity;
            })
            .toList();
        
        return jpaPagoVentaRepository.saveAll(entities).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Pago> findByVentaId(Long ventaId) {
        return jpaPagoVentaRepository.findByVentaId(ventaId).stream()
            .map(mapper::toDomain)
            .toList();
    }
}
