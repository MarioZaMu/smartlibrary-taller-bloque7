package com.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;

/** Objeto de entrada de POST /api/prestamos. Su validación cubre R4. */
public class PrestamoRequest {

    @NotNull(message = "estudianteId es obligatorio")
    private Long estudianteId;

    @NotNull(message = "libroId es obligatorio")
    private Long libroId;

    public PrestamoRequest() {
    }

    public PrestamoRequest(Long estudianteId, Long libroId) {
        this.estudianteId = estudianteId;
        this.libroId = libroId;
    }

    public Long getEstudianteId() { return estudianteId; }
    public Long getLibroId() { return libroId; }
}
