package com.smartlibrary.service;

public class LibroNoDisponibleException extends RuntimeException {
    public LibroNoDisponibleException(Long id) {
        super("El libro con id " + id + " no está disponible para préstamo");
    }
}
