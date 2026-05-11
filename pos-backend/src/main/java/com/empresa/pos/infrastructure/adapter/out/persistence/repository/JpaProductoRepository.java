package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findAllByActivoTrue();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductoEntity p WHERE p.id = :id")
    Optional<ProductoEntity> findByIdForUpdate(@Param("id") Long id);
}
