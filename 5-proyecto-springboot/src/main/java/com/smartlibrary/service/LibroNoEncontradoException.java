package com.smartlibrary.service;

public class LibroNoEncontradoException extends RuntimeException {
    public LibroNoEncontradoException(Long id) {
        super("No existe un libro con id " + id);
    }
}
