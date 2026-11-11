package com.smartlibrary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.model.Estudiante;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestamo;
import com.smartlibrary.repository.EstudianteRepository;
import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias del servicio con repositorios simulados (la regla vive en el servicio). */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock EstudianteRepository estudianteRepository;
    @Mock LibroRepository libroRepository;
    @Mock PrestamoRepository prestamoRepository;
    @InjectMocks PrestamoService servicio;

    @Test
    @DisplayName("Libro disponible: lo marca prestado y guarda libro y préstamo")
    void libroDisponible_marcaPrestadoYGuarda() {
        Estudiante estudiante = new Estudiante("Ana", "ana@u.edu");
        Libro libro = new Libro("Clean Code", true);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        Prestamo prestamo = servicio.registrarPrestamo(new PrestamoRequest(1L, 2L));

        assertThat(libro.isDisponible()).isFalse();
        assertThat(prestamo.getLibro()).isSameAs(libro);
        assertThat(prestamo.getEstudiante()).isSameAs(estudiante);
        assertThat(prestamo.getFechaPrestamo()).isNotNull();
        verify(libroRepository).save(libro);
        verify(prestamoRepository).save(prestamo);
    }

    @Test
    @DisplayName("Libro no disponible: lanza LibroNoDisponibleException y no guarda nada")
    void libroNoDisponible_lanzaExcepcion_yNoGuarda() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(new Estudiante("Ana", "ana@u.edu")));
        when(libroRepository.findById(3L)).thenReturn(Optional.of(new Libro("Refactoring", false)));

        assertThatThrownBy(() -> servicio.registrarPrestamo(new PrestamoRequest(1L, 3L)))
                .isInstanceOf(LibroNoDisponibleException.class);

        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Estudiante inexistente: lanza EstudianteNoEncontradoException")
    void estudianteInexistente_lanzaExcepcion() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrarPrestamo(new PrestamoRequest(99L, 1L)))
                .isInstanceOf(EstudianteNoEncontradoException.class);

        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Libro inexistente: lanza LibroNoEncontradoException")
    void libroInexistente_lanzaExcepcion() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(new Estudiante("Ana", "ana@u.edu")));
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrarPrestamo(new PrestamoRequest(1L, 99L)))
                .isInstanceOf(LibroNoEncontradoException.class);

        verify(prestamoRepository, never()).save(any());
    }
}
