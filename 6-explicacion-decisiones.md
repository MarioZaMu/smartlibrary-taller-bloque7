# 6. Explicación de las decisiones de diseño

**Integrantes:** Mario Alberto Zambrano Muñoz · Karen Valentina Arteaga Realpe

**1. Responsabilidades que cambiaron.** `GestorPrestamos` era una clase mixta: guardaba listas en memoria (acceso a datos), coordinaba `registrarPrestamo` (negocio) y no tenía forma de recibir HTTP. Desapareció y su rol se repartió: `PrestamoController` recibe y responde HTTP, `PrestamoService` coordina el caso de uso y la transacción, y los tres `Repository` buscan y guardan. `Estudiante`, `Libro` y `Prestamo` conservan los datos y el comportamiento propio (`Libro.marcarPrestado()`). Se añadieron `PrestamoRequest`/`PrestamoResponse` para no exponer entidades y `ManejadorExcepciones` para traducir errores.

**2. GRASP.** La coordinación del caso de uso corresponde al patrón **Controlador (GRASP)**: un objeto no visual que recibe el evento del sistema y lo delega, aquí `PrestamoService`. Un controlador GRASP **no equivale** a `@RestController`: este es solo un adaptador de HTTP (capa de presentación) que traduce la petición; el controlador GRASP es la clase que coordina la operación del sistema, independiente del protocolo. En este diseño `PrestamoController` hace de fachada HTTP y `PrestamoService` asume el rol de controlador de caso de uso. También aplican *Experto* (`Libro` cambia su disponibilidad), *Creador* (el servicio crea el `Prestamo`), *Bajo acoplamiento* y *Alta cohesión*.

**3. Regla de disponibilidad (R1).** Se decide en `PrestamoService`, que consulta `Libro.isDisponible()`. El estado es del dominio; la decisión de rechazar el caso de uso es de coordinación. No va en el controlador (mezclaría negocio con HTTP) ni en el repositorio (solo persiste). Como el cambio de disponibilidad y la creación del préstamo ocurren en una única transacción, R3 y R4 se cumplen: si algo falla, no queda un préstamo a medias. R5 se cumple porque `disponible = false` mientras el préstamo siga activo, así que solo puede haber uno activo por libro. La concurrencia (dos solicitudes simultáneas) no se cubre; requeriría bloqueo optimista o pesimista.

**4. Relaciones UML.** *Conservadas:* las asociaciones `Prestamo`→`Estudiante` (0..* a 1) y `Prestamo`→`Libro` (0..* a 1), sin agregación ni composición, porque libro y estudiante tienen ciclo de vida independiente. *Modificadas:* se eliminaron las cinco asociaciones de `GestorPrestamos` (1 a 0..*); aparecieron dependencias `Controller → Service → Repository` (el contenedor inyecta los colaboradores; no son parte del dominio), generalizaciones de los repositorios hacia `JpaRepository` y la realización de `JpaRepository` por la clase que genera Spring Data.

**5. Trazabilidad requisito → clase → operación → prueba.**

| Requisito | Clase | Operación | Prueba |
|---|---|---|---|
| R1 solo un libro disponible | `PrestamoService` / `Libro` | `registrarPrestamo` / `isDisponible` | `libroYaPrestado_devuelve409_sinNuevoPrestamo`, `libroNoDisponible_lanzaExcepcion_yNoGuarda` |
| R2 registra estudiante, libro y fecha | `PrestamoService` / `Prestamo` | `registrarPrestamo` / constructor | `libroDisponible_devuelve201_yRegistraPrestamo` |
| R3 el libro deja de estar disponible | `Libro` | `marcarPrestado` | `libroDisponible_devuelve201_yRegistraPrestamo` (comprueba `disponible=false`) |
| R4 solicitud inválida no crea préstamo | `PrestamoRequest` / `ManejadorExcepciones` | `@NotNull` / `manejarValidacion` | `solicitudInvalida_devuelve400_sinPrestamo`, `estudianteInexistente…404`, `libroInexistente…404` |
| R5 varios históricos, uno activo | `Prestamo`, `Libro` | asociaciones 0..*; `registrarPrestamo` | `prestarDosVecesElMismoLibro_segundoEs409` |
| Consulta de préstamos | `PrestamoController` / `PrestamoService` | `listar` / `consultarPrestamos` | `consultaDePrestamos_devuelve200_conRegistros` |

Cada mensaje del diagrama de secuencia es una operación del diagrama de clases, y cada operación se ejecuta en alguna de las 11 pruebas (`evidencias-pruebas/mvn-test.txt`).
