package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Optional<Producto> findById(Long id);

    List<Producto> findAllActivos();

    Producto save(Producto producto);

    void deleteById(Long id);
}
