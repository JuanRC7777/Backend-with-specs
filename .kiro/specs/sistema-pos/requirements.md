# Especificación de Requerimientos de Software (SRS)
# Sistema POS - Point of Sale
**Versión:** 2.0.0  
**Fecha:** 2026-04-22  
**Estado:** Activo  

---

## 1. Introducción

### 1.1 Propósito
Este documento describe los requerimientos funcionales, no funcionales y restricciones de arquitectura del sistema Point of Sale (POS), desarrollado con Java y Spring Boot bajo arquitectura hexagonal y principios SOLID. Está dirigido a desarrolladores, arquitectos de software y stakeholders del proyecto.

### 1.2 Alcance del Sistema
El sistema POS permite a usuarios autenticados gestionar el inventario de productos y registrar ventas en tiempo real. Incluye:

- Autenticación y autorización de usuarios mediante JWT.
- Gestión completa de productos (CRUD).
- Interfaz de punto de venta para registrar transacciones.
- Cálculo automático de totales por venta.
- Validación de stock antes de confirmar una venta.

Queda fuera del alcance: módulos de contabilidad, facturación electrónica, gestión de proveedores y reportes avanzados.

### 1.3 Definiciones y Acrónimos
| Término | Descripción |
|---|---|
| POS | Point of Sale – Punto de Venta |
| JWT | JSON Web Token |
| CRUD | Create, Read, Update, Delete |
| SRS | Software Requirements Specification |
| API REST | Interfaz de programación basada en HTTP/JSON |
| Hexagonal | Arquitectura Ports & Adapters — aísla el dominio de la infraestructura |
| Port | Interfaz que define un contrato entre el dominio y el exterior |
| Adapter | Implementación concreta de un Port (REST, JPA, etc.) |
| Use Case | Caso de uso implementado como servicio de aplicación |
| SOLID | Conjunto de cinco principios de diseño orientado a objetos |
| PBT | Property-Based Testing — pruebas basadas en propiedades |

---

## 2. Requerimientos Funcionales

### RF-01: Autenticación de Usuario (Login)
- **RF-01.1** El sistema debe permitir el inicio de sesión mediante usuario y contraseña.
- **RF-01.2** El sistema debe generar un token JWT al autenticar correctamente al usuario.
- **RF-01.3** El token JWT debe tener un tiempo de expiración configurable.
- **RF-01.4** El sistema debe rechazar credenciales inválidas con un mensaje de error apropiado (HTTP 401).
- **RF-01.5** Todos los endpoints protegidos deben requerir un token JWT válido en el header `Authorization: Bearer <token>`.

### RF-02: Gestión de Productos
- **RF-02.1** El usuario autenticado puede crear un nuevo producto con los campos: nombre, descripción, precio unitario y cantidad en stock.
- **RF-02.2** El usuario autenticado puede editar los datos de un producto existente.
- **RF-02.3** El usuario autenticado puede eliminar un producto del sistema (eliminación lógica).
- **RF-02.4** El usuario autenticado puede listar todos los productos activos disponibles.
- **RF-02.5** El sistema debe validar que el precio sea mayor a cero y el stock no sea negativo.
- **RF-02.6** El sistema debe retornar HTTP 404 si se intenta acceder a un producto inexistente.

### RF-03: Proceso de Venta POS
- **RF-03.1** El usuario autenticado puede registrar una nueva venta enviando una lista de productos con sus cantidades.
- **RF-03.2** El sistema debe validar que la cantidad solicitada por producto no supere el stock disponible.
- **RF-03.3** Al confirmar la venta, el sistema debe descontar el stock de cada producto vendido de forma atómica.
- **RF-03.4** El sistema debe asociar la venta al usuario autenticado que la registra.
- **RF-03.5** Una venta confirmada no puede modificarse ni eliminarse.

### RF-04: Cálculo Automático de Totales
- **RF-04.1** El sistema debe calcular el subtotal por línea de detalle como `precio_unitario × cantidad`.
- **RF-04.2** El sistema debe calcular el total general de la venta como la suma de todos los subtotales.
- **RF-04.3** El precio unitario registrado en el detalle debe ser el precio del producto al momento de la venta.
- **RF-04.4** El total calculado debe persistirse junto con el registro de la venta.

---

## 3. Requerimientos No Funcionales

### RNF-01: Seguridad
- Toda comunicación debe realizarse sobre HTTPS en producción.
- Las contraseñas deben almacenarse con hash usando BCrypt.
- Los tokens JWT deben firmarse con una clave secreta externalizada como variable de entorno.
- Los endpoints deben estar protegidos; solo `/api/auth/login` es público.
- Se debe implementar protección contra ataques de fuerza bruta (rate limiting recomendado).

### RNF-02: Rendimiento
- El tiempo de respuesta de los endpoints no debe superar los 500ms bajo carga normal.
- El sistema debe soportar al menos 50 usuarios concurrentes sin degradación significativa.
- Las consultas a la base de datos deben estar optimizadas con índices apropiados.

### RNF-03: Usabilidad
- La API debe seguir convenciones REST estándar y retornar respuestas JSON en formato consistente.
- Los mensajes de error deben ser descriptivos e incluir el campo que falló cuando aplique.
- La documentación de la API debe estar disponible en Swagger UI (`/swagger-ui.html`).

### RNF-04: Escalabilidad
- La arquitectura hexagonal debe permitir reemplazar adaptadores (ej. cambiar MySQL por PostgreSQL) sin modificar el dominio.
- La configuración debe externalizarse para facilitar despliegues en múltiples entornos.
- El sistema debe poder contenedorizarse con Docker sin cambios en el código.

### RNF-05: Arquitectura Hexagonal (Ports & Adapters)
- **RNF-05.1** El dominio de negocio (`domain/model`) debe ser independiente de Spring, JPA y cualquier framework externo.
- **RNF-05.2** Los casos de uso deben definirse como interfaces (Input Ports) en `application/port/in`.
- **RNF-05.3** Los repositorios deben definirse como interfaces (Output Ports) en `application/port/out`.
- **RNF-05.4** Los controllers REST son Adapters Primarios y solo deben depender de Input Ports.
- **RNF-05.5** Los repositorios JPA son Adapters Secundarios y deben implementar los Output Ports.
- **RNF-05.6** Ninguna clase del dominio o de la capa de aplicación debe importar clases de `org.springframework` ni `javax.persistence`.

### RNF-06: Principios SOLID
- **RNF-06.1 (SRP)** Cada clase debe tener una única responsabilidad. Los servicios de aplicación solo orquestan casos de uso; la lógica de negocio reside en el dominio.
- **RNF-06.2 (OCP)** Los casos de uso deben depender de abstracciones (ports), permitiendo agregar nuevos adapters sin modificar el dominio.
- **RNF-06.3 (LSP)** Cualquier implementación de un Output Port debe poder sustituirse sin romper los casos de uso.
- **RNF-06.4 (ISP)** Los Input Ports deben estar segregados por operación. Cada controller inyecta solo los use cases que necesita.
- **RNF-06.5 (DIP)** Los servicios de aplicación deben depender de interfaces (ports), no de implementaciones concretas de JPA o Spring.

### RNF-07: Calidad y Testing
- **RNF-07.1** Los tests unitarios del dominio deben ejecutarse sin levantar contexto de Spring (`@SpringBootTest` prohibido en tests unitarios).
- **RNF-07.2** Los tests de servicios de aplicación deben usar mocks de los Output Ports (Mockito).
- **RNF-07.3** Los tests de controllers deben usar `@WebMvcTest` con mocks de los Input Ports.
- **RNF-07.4** La cobertura mínima de la capa `application/service` debe ser del 80%.
- **RNF-07.5** Cada regla de negocio del dominio debe tener al menos un test unitario que la valide.

---

## 4. Reglas de Negocio

| ID | Regla | Capa que la implementa |
|---|---|---|
| RN-01 | No se puede registrar una venta con cantidad de producto igual a cero. | Domain / Application |
| RN-02 | No se puede vender un producto cuyo stock sea insuficiente para la cantidad solicitada. | Domain (`Producto.descontarStock`) |
| RN-03 | El stock se descuenta únicamente al confirmar la venta, de forma atómica (`@Transactional`). | Application Service |
| RN-04 | Una venta confirmada no puede modificarse ni eliminarse. | Application Service |
| RN-05 | El precio de venta se toma del precio del producto al momento de confirmar la venta. | Application Service |
| RN-06 | Solo usuarios autenticados pueden acceder a cualquier funcionalidad del sistema. | Infrastructure (Security) |
| RN-07 | El total de la venta es la suma de `(precio_unitario × cantidad)` por cada línea de detalle. | Domain (`Venta.calcularTotal`) |
| RN-08 | La lógica de negocio del dominio no debe depender de ningún framework externo. | Domain |

---

## 5. Casos de Uso Principales

### CU-01: Iniciar Sesión
- **Actor:** Usuario
- **Precondición:** El usuario existe en el sistema.
- **Port involucrado:** `LoginUseCase`
- **Flujo principal:**
  1. El usuario envía credenciales (username, password) a `POST /api/auth/login`.
  2. `AuthController` delega a `LoginUseCase`.
  3. `AuthService` carga el usuario via `UsuarioRepositoryPort`.
  4. Valida la contraseña con BCrypt.
  5. Genera y retorna un token JWT.
- **Flujo alternativo:** Si las credenciales son inválidas, retorna HTTP 401.

### CU-02: Gestionar Productos
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido en el header `Authorization`.
- **Ports involucrados:** `CrearProductoUseCase`, `ListarProductosUseCase`, `ObtenerProductoUseCase`, `ActualizarProductoUseCase`, `EliminarProductoUseCase`
- **Flujo principal:**
  1. El usuario realiza una operación CRUD sobre productos.
  2. `ProductoController` delega al Use Case correspondiente (ISP).
  3. `ProductoService` aplica validaciones y persiste via `ProductoRepositoryPort`.
  4. Retorna la respuesta mapeada a `ProductoResponse`.
- **Flujo alternativo:** Datos inválidos → HTTP 400. Producto no encontrado → HTTP 404.

### CU-03: Registrar Venta POS
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido; productos con stock disponible.
- **Port involucrado:** `RegistrarVentaUseCase`
- **Flujo principal:**
  1. El usuario envía lista de `{ productoId, cantidad }` a `POST /api/ventas`.
  2. `VentaController` delega a `RegistrarVentaUseCase`.
  3. `VentaService` obtiene cada producto via `ProductoRepositoryPort`.
  4. Llama a `producto.descontarStock(cantidad)` — el dominio valida el stock.
  5. Calcula subtotales y total via `Venta.calcularTotal()`.
  6. Persiste la venta y los detalles via `VentaRepositoryPort` en una transacción atómica.
- **Flujo alternativo:** Si `descontarStock` lanza `StockInsuficienteException`, la transacción hace rollback y retorna HTTP 400.
