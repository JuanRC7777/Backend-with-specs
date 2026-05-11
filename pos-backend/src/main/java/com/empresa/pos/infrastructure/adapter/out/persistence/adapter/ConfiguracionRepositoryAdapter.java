package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.ConfiguracionRepositoryPort;
import com.empresa.pos.domain.model.Configuracion;
import com.empresa.pos.infrastructure.adapter.out.persistence.mapper.ConfiguracionMapper;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaConfiguracionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfiguracionRepositoryAdapter implements ConfiguracionRepositoryPort {

    private final JpaConfiguracionRepository jpaConfiguracionRepository;
    private final ConfiguracionMapper mapper;

    public ConfiguracionRepositoryAdapter(JpaConfiguracionRepository jpaConfiguracionRepository,
                                           ConfiguracionMapper mapper) {
        this.jpaConfiguracionRepository = jpaConfiguracionRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Configuracion> findByClave(String clave) {
        return jpaConfiguracionRepository.findByClave(clave).map(mapper::toDomain);
    }

    @Override
    public Configuracion save(Configuracion configuracion) {
        return mapper.toDomain(jpaConfiguracionRepository.save(mapper.toEntity(configuracion)));
    }
}
