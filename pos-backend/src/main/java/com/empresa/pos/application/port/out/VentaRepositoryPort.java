package com.empresa.pos.application.port.out;

import com.empresa.pos.domain.model.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {

    Venta save(Venta venta);

    Optional<Venta> findById(Long id);

    List<Venta> findAll();
}
