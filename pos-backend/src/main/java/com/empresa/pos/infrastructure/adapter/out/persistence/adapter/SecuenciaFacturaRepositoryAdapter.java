package com.empresa.pos.infrastructure.adapter.out.persistence.adapter;

import com.empresa.pos.application.port.out.SecuenciaFacturaRepositoryPort;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.SecuenciaFacturaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.repository.JpaSecuenciaFacturaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class SecuenciaFacturaRepositoryAdapter implements SecuenciaFacturaRepositoryPort {

    private final JpaSecuenciaFacturaRepository jpaSecuenciaFacturaRepository;

    public SecuenciaFacturaRepositoryAdapter(JpaSecuenciaFacturaRepository jpaSecuenciaFacturaRepository) {
        this.jpaSecuenciaFacturaRepository = jpaSecuenciaFacturaRepository;
    }

    @Override
    @Transactional
    public int obtenerSiguienteNumero(LocalDate fecha) {
        Optional<SecuenciaFacturaEntity> secuenciaOpt = jpaSecuenciaFacturaRepository.findByFechaForUpdate(fecha);
        
        if (secuenciaOpt.isPresent()) {
            SecuenciaFacturaEntity secuencia = secuenciaOpt.get();
            return secuencia.getUltimoNumero() + 1;
        } else {
            // Primera factura del día
            SecuenciaFacturaEntity nuevaSecuencia = SecuenciaFacturaEntity.builder()
                .fecha(fecha)
                .ultimoNumero(0)
                .build();
            jpaSecuenciaFacturaRepository.save(nuevaSecuencia);
            return 1;
        }
    }

    @Override
    @Transactional
    public void actualizarSecuencia(LocalDate fecha, int numero) {
        Optional<SecuenciaFacturaEntity> secuenciaOpt = jpaSecuenciaFacturaRepository.findByFechaForUpdate(fecha);
        
        if (secuenciaOpt.isPresent()) {
            SecuenciaFacturaEntity secuencia = secuenciaOpt.get();
            secuencia.setUltimoNumero(numero);
            jpaSecuenciaFacturaRepository.save(secuencia);
        } else {
            SecuenciaFacturaEntity nuevaSecuencia = SecuenciaFacturaEntity.builder()
                .fecha(fecha)
                .ultimoNumero(numero)
                .build();
            jpaSecuenciaFacturaRepository.save(nuevaSecuencia);
        }
    }
}
