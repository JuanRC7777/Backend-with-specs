package com.empresa.pos.domain.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(Long productoId, int cantidadSolicitada, int stockDisponible) {
        super("Stock insuficiente para el producto ID " + productoId +
              ". Solicitado: " + cantidadSolicitada +
              ", Disponible: " + stockDisponible);
    }
}