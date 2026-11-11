package com.smartlibrary.service;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.model.Estudiante;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestamo;
import com.smartlibrary.repository.EstudianteRepository;
import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordina el caso de uso «Registrar préstamo» y la consulta de préstamos. */
@Service
public class PrestamoService {

    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamoService(EstudianteRepository estudianteRepository,
                           LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    /**
     * Cambio de disponibilidad + creación del préstamo en una sola transacción:
     * si algo falla, ninguno de los dos cambios queda guardado.
     */
    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequest request) {
        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new EstudianteNoEncontradoException(request.getEstudianteId()));
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new LibroNoEncontradoException(request.getLibroId()));

        if (!libro.isDisponible()) {                       // R1 (decide el servicio; el estado lo conoce Libro)
            throw new LibroNoDisponibleException(libro.getId());
        }

        Prestamo prestamo = new Prestamo(estudiante, libro, LocalDate.now());   // R2
        libro.marcarPrestado();                                                 // R3
        libroRepository.save(libro);
        return prestamoRepository.save(prestamo);
    }

    @Transactional(readOnly = true)
    public List<Prestamo> consultarPrestamos() {
        return prestamoRepository.findAll();
    }
}
