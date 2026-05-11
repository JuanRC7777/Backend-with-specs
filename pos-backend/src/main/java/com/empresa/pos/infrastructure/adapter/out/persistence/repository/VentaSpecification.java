package com.empresa.pos.infrastructure.adapter.out.persistence.repository;

import com.empresa.pos.infrastructure.adapter.out.persistence.entity.PagoVentaEntity;
import com.empresa.pos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VentaSpecification {

    public static Specification<VentaEntity> porFecha(LocalDate fecha) {
        return (root, query, criteriaBuilder) -> {
            if (fecha == null) {
                return criteriaBuilder.conjunction();
            }
            LocalDateTime startOfDay = fecha.atStartOfDay();
            LocalDateTime endOfDay = fecha.plusDays(1).atStartOfDay();
            return criteriaBuilder.between(root.get("fecha"), startOfDay, endOfDay);
        };
    }

    public static Specification<VentaEntity> porCajero(Long cajeroId) {
        return (root, query, criteriaBuilder) -> {
            if (cajeroId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("usuario").get("id"), cajeroId);
        };
    }

    public static Specification<VentaEntity> porCedulaCliente(String cedulaCliente) {
        return (root, query, criteriaBuilder) -> {
            if (cedulaCliente == null || cedulaCliente.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("cedulaCliente"), cedulaCliente);
        };
    }

    public static Specification<VentaEntity> porMetodoPago(String metodoPago) {
        return (root, query, criteriaBuilder) -> {
            if (metodoPago == null || metodoPago.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<VentaEntity, PagoVentaEntity> pagosJoin = root.join("pagos", JoinType.INNER);
            return criteriaBuilder.equal(pagosJoin.get("metodoPago"), metodoPago);
        };
    }
}
