package com.smartlibrary.dto;

import com.smartlibrary.model.Prestamo;
import java.time.LocalDate;

/** Representación de un préstamo hacia el cliente (no se exponen las entidades). */
public record PrestamoResponse(Long id, Long estudianteId, Long libroId, LocalDate fechaPrestamo) {

    public static PrestamoResponse desde(Prestamo p) {
        return new PrestamoResponse(p.getId(), p.getEstudiante().getId(), p.getLibro().getId(), p.getFechaPrestamo());
    }
}
