package com.smartlibrary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de extremo a extremo (HTTP -> Controller -> Service -> Repository -> H2)
 * sobre los datos iniciales de data.sql:
 * estudiantes 1 y 2; libros 1 y 2 disponibles; libro 3 ya prestado (préstamo 1).
 * Cada prueba se revierte al terminar (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrestamoControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired PrestamoRepository prestamoRepository;
    @Autowired LibroRepository libroRepository;

    private static String cuerpo(Object estudianteId, Object libroId) {
        return "{\"estudianteId\": " + estudianteId + ", \"libroId\": " + libroId + "}";
    }

    @Test
    @DisplayName("R1-R3 · Libro disponible -> 201 Created y préstamo registrado")
    void libroDisponible_devuelve201_yRegistraPrestamo() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(1, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estudianteId").value(1))
                .andExpect(jsonPath("$.libroId").value(1))
                .andExpect(jsonPath("$.fechaPrestamo").exists());

        assertThat(prestamoRepository.count()).isEqualTo(antes + 1);
        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isFalse();
    }

    @Test
    @DisplayName("R1 · Libro ya prestado (seed) -> 409 Conflict y sin nuevo préstamo")
    void libroYaPrestado_devuelve409_sinNuevoPrestamo() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(1, 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("R5 · Prestar dos veces el mismo libro -> el segundo intento es 409 y solo hay un préstamo activo")
    void prestarDosVecesElMismoLibro_segundoEs409() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(1, 2)))
                .andExpect(status().isCreated());
        long trasPrimero = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(2, 2)))
                .andExpect(status().isConflict());

        assertThat(prestamoRepository.count()).isEqualTo(trasPrimero);
    }

    @Test
    @DisplayName("Estudiante inexistente -> 404 Not Found")
    void estudianteInexistente_devuelve404() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isTrue();
    }

    @Test
    @DisplayName("Libro inexistente -> 404 Not Found")
    void libroInexistente_devuelve404() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo(1, 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("R4 · Solicitud inválida (ids nulos) -> 400 Bad Request y sin préstamo")
    void solicitudInvalida_devuelve400_sinPrestamo() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Consulta de préstamos -> 200 OK con registros")
    void consultaDePrestamos_devuelve200_conRegistros() throws Exception {
        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].libroId").value(3))
                .andExpect(jsonPath("$[0].estudianteId").value(2));
    }
}
