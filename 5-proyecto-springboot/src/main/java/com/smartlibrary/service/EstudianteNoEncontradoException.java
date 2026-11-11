package com.smartlibrary.service;

public class EstudianteNoEncontradoException extends RuntimeException {
    public EstudianteNoEncontradoException(Long id) {
        super("No existe un estudiante con id " + id);
    }
}
