package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.PagoVentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPagoVentaRepository extends JpaRepository<PagoVentaEntity, Long> {
    
    List<PagoVentaEntity> findByVentaId(Long ventaId);
}
