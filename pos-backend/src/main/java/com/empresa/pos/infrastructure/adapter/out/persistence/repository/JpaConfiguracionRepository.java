package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ConfiguracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaConfiguracionRepository extends JpaRepository<ConfiguracionEntity, Long> {
    
    Optional<ConfiguracionEntity> findByClave(String clave);
}
