-- Datos iniciales de prueba (estudiantes y libros ya existen; el libro 3 ya está prestado)
INSERT INTO estudiante (nombre, correo) VALUES ('Ana Torres', 'ana.torres@universidad.edu');
INSERT INTO estudiante (nombre, correo) VALUES ('Luis Pérez', 'luis.perez@universidad.edu');

INSERT INTO libro (titulo, disponible) VALUES ('Clean Code', TRUE);
INSERT INTO libro (titulo, disponible) VALUES ('Domain-Driven Design', TRUE);
INSERT INTO libro (titulo, disponible) VALUES ('Refactoring', FALSE);

INSERT INTO prestamo (fecha_prestamo, fecha_devolucion, estudiante_id, libro_id) VALUES (DATE '2026-11-01', NULL, 2, 3);
