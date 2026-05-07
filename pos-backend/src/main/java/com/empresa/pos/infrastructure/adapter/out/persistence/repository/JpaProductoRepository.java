package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findAllByActivoTrue();
}
