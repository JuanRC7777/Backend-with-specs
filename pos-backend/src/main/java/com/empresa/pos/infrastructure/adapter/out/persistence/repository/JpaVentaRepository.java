package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVentaRepository extends JpaRepository<VentaEntity, Long> {
}
