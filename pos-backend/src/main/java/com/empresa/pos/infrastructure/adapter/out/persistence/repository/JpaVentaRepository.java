package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaVentaRepository extends JpaRepository<VentaEntity, Long>, JpaSpecificationExecutor<VentaEntity> {
    
    Optional<VentaEntity> findByNumeroFactura(String numeroFactura);
    
    boolean existsByNumeroFactura(String numeroFactura);
    
    Page<VentaEntity> findAll(Specification<VentaEntity> spec, Pageable pageable);
}
