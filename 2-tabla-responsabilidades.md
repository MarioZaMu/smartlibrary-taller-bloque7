# 2. Tabla de responsabilidades — «Registrar préstamo» (POST /api/prestamos)

**Integrantes:** Mario Alberto Zambrano Muñoz · Karen Valentina Arteaga Realpe

Reparto entre **Controller** (entrada/salida HTTP), **Service** (coordinación del caso de uso) y **Repository** (persistencia). `Estudiante`, `Libro` y `Prestamo` son el modelo del dominio, no una cuarta capa.

| Operación o decisión | Clase responsable | Justificación |
|---|---|---|
| Recibir `POST /api/prestamos` | `PrestamoController` (`registrar(request)`) | Es el único punto que conoce HTTP: mapea la ruta, deserializa el JSON a `PrestamoRequest` y dispara la validación (`@Valid`, R4). No contiene reglas de negocio, lo que mantiene alta su cohesión y bajo su acoplamiento con el dominio. |
| Buscar estudiante por identificador | `EstudianteRepository` (`findById`), invocado por `PrestamoService` | Acceder a datos es responsabilidad de la capa de persistencia. El servicio decide qué hacer con el resultado: si no hay estudiante lanza `EstudianteNoEncontradoException` (→ 404). |
| Buscar libro por identificador | `LibroRepository` (`findById`), invocado por `PrestamoService` | Misma razón: el servicio no conoce SQL ni JPA; si el libro no existe lanza `LibroNoEncontradoException` (→ 404). |
| Validar disponibilidad (R1) | `PrestamoService` decide; `Libro.isDisponible()` aporta el dato | El estado `disponible` pertenece a `Libro` (Experto en información), pero rechazar el caso de uso es una decisión de coordinación, que corresponde al servicio. Si no está disponible lanza `LibroNoDisponibleException` (→ 409). |
| Crear el préstamo (R2) | `PrestamoService` (`new Prestamo(estudiante, libro, fecha)`) | Creador (GRASP): el servicio dispone de los datos de inicialización (estudiante, libro y fecha) y registra el préstamo. R2: el préstamo guarda estudiante, libro y fecha. |
| Cambiar disponibilidad (R3) | `Libro` (`marcarPrestado()`) | El objeto que posee el estado es quien lo modifica (Experto); el servicio solo invoca la operación. |
| Guardar cambios | `LibroRepository.save` y `PrestamoRepository.save`, orquestados por `PrestamoService` dentro de `@Transactional` | Persistir es de los repositorios; que el cambio de disponibilidad y la creación del préstamo ocurran **juntos o ninguno** es responsabilidad de quien coordina el caso de uso (el servicio), que abre la transacción. |
| Devolver respuesta HTTP | `PrestamoController` (201 Created, 200 OK) y `ManejadorExcepciones` (404, 409, 400) | Traducir resultado y errores a códigos HTTP es presentación. El manejo explícito de excepciones evita que el servicio dependa de HTTP. |

**Regla de dependencias:** `PrestamoController → PrestamoService → Repository`. El controlador nunca llama a un repositorio.
