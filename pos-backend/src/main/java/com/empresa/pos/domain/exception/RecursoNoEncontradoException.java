package com.empresa.pos.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " no encontrado con ID: " + id);
    }

    public RecursoNoEncontradoException(String recurso, String identificador) {
        super(recurso + " no encontrado: " + identificador);
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
