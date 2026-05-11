package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Optional<Producto> findById(Long id);
    
    /**
     * Busca un producto por ID con bloqueo pesimista (SELECT FOR UPDATE).
     * Usado para garantizar consistencia en operaciones concurrentes.
     * 
     * @param id ID del producto
     * @return Producto bloqueado para actualización
     */
    Optional<Producto> findByIdForUpdate(Long id);

    List<Producto> findAllActivos();

    Producto save(Producto producto);

    void deleteById(Long id);
}
