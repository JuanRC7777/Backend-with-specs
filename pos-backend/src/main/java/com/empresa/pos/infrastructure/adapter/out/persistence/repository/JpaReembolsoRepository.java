package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ReembolsoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaReembolsoRepository extends JpaRepository<ReembolsoEntity, Long> {
    
    Optional<ReembolsoEntity> findByVentaId(Long ventaId);
    
    boolean existsByVentaId(Long ventaId);
}
