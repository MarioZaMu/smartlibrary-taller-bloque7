package com.smartlibrary.controller;

import com.smartlibrary.dto.ErrorRespuesta;
import com.smartlibrary.service.EstudianteNoEncontradoException;
import com.smartlibrary.service.LibroNoDisponibleException;
import com.smartlibrary.service.LibroNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce excepciones del negocio a códigos HTTP de forma explícita. */
@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler({EstudianteNoEncontradoException.class, LibroNoEncontradoException.class})
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RuntimeException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LibroNoDisponibleException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoDisponible(LibroNoDisponibleException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .sorted()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Solicitud inválida");
        return construir(HttpStatus.BAD_REQUEST, detalle);
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(estado.value(), mensaje));
    }
}
