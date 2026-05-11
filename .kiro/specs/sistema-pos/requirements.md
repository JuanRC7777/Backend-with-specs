# Especificación de Requerimientos de Software (SRS)
# Sistema POS - Point of Sale con Facturación
**Versión:** 3.2.0  
**Fecha:** 2026-05-08  
**Estado:** Activo  

---

## 1. Introducción

### 1.1 Propósito
Este documento describe los requerimientos funcionales, no funcionales y restricciones de arquitectura del sistema Point of Sale (POS), desarrollado con Java y Spring Boot bajo arquitectura hexagonal y principios SOLID. Está dirigido a desarrolladores, arquitectos de software y stakeholders del proyecto.

### 1.2 Alcance del Sistema
El sistema POS permite a usuarios autenticados gestionar el inventario de productos y registrar ventas con facturación completa en tiempo real. Incluye:

- Autenticación y autorización de usuarios mediante JWT.
- Gestión completa de productos (CRUD).
- Interfaz de punto de venta para registrar transacciones con facturación.
- Generación automática de números de factura únicos (formato FAC-YYYYMMDD-NNNNNN con 6 dígitos).
- Captura de datos del cliente (nombre y cédula obligatorios con validaciones específicas).
- **NUEVO:** Múltiples métodos de pago por venta (efectivo, tarjeta, transferencia).
- **NUEVO:** Sistema de reembolsos con devolución automática de productos al inventario.
- **NUEVO:** Tasa de impuesto global configurable (predefinida en 5%).
- Cálculo automático de subtotales, impuestos y totales por venta con redondeo HALF_UP.
- Consulta de ventas por ID o número de factura con paginación y filtros.
- Validación de stock antes de confirmar una venta.
- Identificación automática del cajero desde el token JWT.

Queda fuera del alcance: facturación electrónica con DIAN, módulos de contabilidad avanzada, gestión de proveedores y reportes analíticos complejos.

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
| Factura | Documento comercial que registra una transacción de venta |
| Número de Factura | Identificador único de una factura con formato FAC-YYYYMMDD-NNNNNN (6 dígitos) |
| Cajero | Usuario autenticado que registra una venta en el sistema (identificado por username del JWT) |
| Cliente | Persona que realiza la compra y cuyos datos se registran en la factura |
| Cédula | Documento de identidad del cliente (exactamente 10 dígitos numéricos) |
| Subtotal | Suma de los precios de productos sin aplicar impuesto |
| Impuesto | Monto calculado aplicando la tasa de impuesto al subtotal |
| Tasa de Impuesto | Porcentaje de impuesto aplicable configurado globalmente (ej: 0.05 = 5%) |
| Total | Monto final de la venta (subtotal + impuesto) |
| Método de Pago | Forma de pago utilizada: EFECTIVO, TARJETA o TRANSFERENCIA |
| Pago | Registro de un método de pago con su monto asociado en una venta |
| Reembolso | Devolución de una venta que restaura el inventario y marca la venta como reembolsada |
| ROUND_HALF_UP | Método de redondeo comercial estándar (5 o más redondea hacia arriba) |
| Configuración Global | Parámetros del sistema almacenados en base de datos (ej: tasa de impuesto) |
| RBAC | Role-Based Access Control — Control de acceso basado en roles |
| ADMIN | Rol de usuario con acceso total al sistema (único rol en MVP) |
| MVP | Minimum Viable Product — Producto Mínimo Viable |
| BigDecimal | Tipo de dato Java para cálculos monetarios con precisión arbitraria |
| ROUND_HALF_UP | Método de redondeo comercial estándar (5 o más redondea hacia arriba) |
| ISO-8601 | Estándar internacional para representación de fechas (YYYY-MM-DD) |
| SERIALIZABLE | Nivel de aislamiento transaccional más estricto en bases de datos |

---

## 1.4 Registro de Cambios

### Versión 3.2.0 (2026-05-08)
**Clarificaciones incorporadas:**

1. **Permisos y Roles de Usuario**: Definido rol único ADMIN para MVP con diseño RBAC centralizado
2. **Filtro por Método de Pago**: Especificado comportamiento ANY match para ventas con múltiples pagos
3. **Validación de Nombre del Cliente**: Normalización de espacios múltiples y validación post-trim
4. **Precisión de Suma de Pagos**: Uso obligatorio de BigDecimal con redondeo ROUND_HALF_UP
5. **Límite de 999,999 Facturas**: Código de error HTTP 409 CONFLICT y mensaje específico
6. **Reembolso Parcial vs Total**: Solo reembolsos totales permitidos, modelo preparado para parciales
7. **Concurrencia en Actualización de Tasa de Impuesto**: Bloqueo explícito o aislamiento SERIALIZABLE
8. **Formato de Fecha en Filtros**: ISO-8601 obligatorio con normalización de zona horaria
9. **Paginación sin Resultados**: Lista vacía con metadata en lugar de HTTP 404
10. **Validación de Stock en Reembolso**: Permitir reembolso de productos inactivos si existen históricamente

---

## 2. Requerimientos Funcionales

### RF-01: Autenticación y Autorización de Usuario
- **RF-01.1** El sistema debe permitir el inicio de sesión mediante usuario y contraseña.
- **RF-01.2** El sistema debe generar un token JWT al autenticar correctamente al usuario.
- **RF-01.3** El token JWT debe tener un tiempo de expiración configurable.
- **RF-01.4** El sistema debe rechazar credenciales inválidas con un mensaje de error apropiado (HTTP 401).
- **RF-01.5** Todos los endpoints protegidos deben requerir un token JWT válido en el header `Authorization: Bearer <token>`.
- **RF-01.6** El sistema debe implementar un único rol en la fase inicial (MVP): ADMIN con acceso total a todas las funcionalidades.
- **RF-01.7** El sistema debe diseñarse con arquitectura RBAC (Role-Based Access Control) para facilitar la incorporación de roles adicionales en el futuro.
- **RF-01.8** La autorización debe centralizarse en un único módulo, evitando lógica hardcodeada por pantalla o endpoint.

### RF-01B: Autorización y Control de Acceso (RBAC)
- **RF-01B.1** El sistema debe implementar un modelo de autorización basado en roles (RBAC).
- **RF-01B.2** Para la fase MVP, el sistema debe soportar un único rol: ADMIN con acceso total.
- **RF-01B.3** El rol ADMIN debe tener permisos para: gestionar productos, realizar ventas, realizar reembolsos, modificar configuración del sistema, gestionar inventario y acceder a reportes.
- **RF-01B.4** La lógica de autorización debe estar centralizada en un único módulo de autorización.
- **RF-01B.5** El sistema debe evitar lógica de autorización hardcodeada por pantalla o endpoint.
- **RF-01B.6** El diseño del sistema de autorización debe permitir la adición futura de roles adicionales sin modificar la arquitectura base.

### RF-02: Gestión de Productos
- **RF-02.1** El usuario autenticado puede crear un nuevo producto con los campos: nombre, descripción, precio unitario y cantidad en stock.
- **RF-02.2** El usuario autenticado puede editar los datos de un producto existente.
- **RF-02.3** El usuario autenticado puede eliminar un producto del sistema (eliminación lógica).
- **RF-02.4** El usuario autenticado puede listar todos los productos activos disponibles.
- **RF-02.5** El sistema debe validar que el precio sea mayor a cero y el stock no sea negativo.
- **RF-02.6** El sistema debe retornar HTTP 404 si se intenta acceder a un producto inexistente.

### RF-03: Proceso de Venta POS con Facturación
- **RF-03.1** El usuario autenticado puede registrar una nueva venta enviando una lista de productos con sus cantidades, datos del cliente y múltiples métodos de pago.
- **RF-03.2** El sistema debe capturar obligatoriamente el nombre del cliente antes de confirmar la venta.
- **RF-03.2.1** El nombre del cliente debe contener solo letras, espacios, tildes y ñ.
- **RF-03.2.2** El nombre del cliente debe tener máximo 50 caracteres.
- **RF-03.2.3** El sistema debe aplicar trim() al nombre antes de validar.
- **RF-03.2.4** El sistema debe normalizar múltiples espacios consecutivos a un solo espacio separador.
- **RF-03.2.5** El nombre del cliente debe contener mínimo 2 palabras válidas después de normalización.
- **RF-03.3** El sistema debe capturar obligatoriamente la cédula del cliente (validada: exactamente 10 dígitos numéricos sin guiones, puntos ni espacios) antes de confirmar la venta.
- **RF-03.4** El sistema debe permitir registrar múltiples métodos de pago por venta (EFECTIVO, TARJETA, TRANSFERENCIA).
- **RF-03.5** El sistema debe validar que la suma de todos los pagos sea exactamente igual al total de la venta usando BigDecimal.compareTo() después de redondear ambos valores a 2 decimales con ROUND_HALF_UP, sin tolerancias arbitrarias.
- **RF-03.5.1** El sistema debe usar BigDecimal para todos los cálculos monetarios.
- **RF-03.5.2** El sistema debe redondear el total y la suma de pagos a 2 decimales con ROUND_HALF_UP antes de comparar.
- **RF-03.5.3** El sistema debe comparar los valores usando compareTo() == 0 (no tolerancias arbitrarias).
- **RF-03.6** El sistema debe obtener automáticamente la tasa de impuesto desde la configuración global del sistema.
- **RF-03.7** El sistema debe generar automáticamente un número de factura único con formato FAC-YYYYMMDD-NNNNNN (6 dígitos) al confirmar la venta.
- **RF-03.8** El sistema debe identificar automáticamente al cajero que registra la venta desde el username del token JWT.
- **RF-03.9** El sistema debe validar que la cantidad solicitada por producto no supere el stock disponible.
- **RF-03.10** Al confirmar la venta, el sistema debe descontar el stock de cada producto vendido de forma atómica.
- **RF-03.11** El sistema debe asociar la venta al usuario autenticado que la registra.
- **RF-03.12** Una venta confirmada no puede modificarse ni eliminarse.
- **RF-03.13** La fecha de la factura debe tomarse al momento de CONFIRMAR la venta, no al iniciarla.

### RF-04: Cálculo Automático de Totales con Impuestos
- **RF-04.1** El sistema debe calcular el subtotal por línea de detalle como `precio_unitario × cantidad` y redondearlo a 2 decimales usando ROUND_HALF_UP.
- **RF-04.2** El sistema debe calcular el subtotal general de la venta como la suma de todos los subtotales de líneas ya redondeados.
- **RF-04.3** El sistema debe calcular el monto del impuesto como `subtotal × tasa_impuesto` y redondearlo a 2 decimales usando ROUND_HALF_UP.
- **RF-04.4** El sistema debe calcular el total final de la venta como `subtotal + impuesto` y redondearlo a 2 decimales usando ROUND_HALF_UP.
- **RF-04.5** El precio unitario registrado en el detalle debe ser el precio del producto al momento de la venta.
- **RF-04.6** Los valores calculados (subtotal, impuesto, total) deben persistirse junto con el registro de la venta.
- **RF-04.7** El redondeo debe aplicarse por línea antes de sumar, no al final del cálculo total.

### RF-05: Generación de Número de Factura
- **RF-05.1** El sistema debe generar automáticamente un número de factura único para cada venta.
- **RF-05.2** El formato del número de factura debe ser: FAC-YYYYMMDD-NNNNNN (ej: FAC-20260507-000001) con 6 dígitos para la secuencia.
- **RF-05.3** La secuencia numérica (NNNNNN) debe reiniciarse a 000001 cada día.
- **RF-05.4** El sistema debe soportar hasta 999,999 ventas por día.
- **RF-05.5** El sistema debe garantizar que no existan números de factura duplicados mediante constraint UNIQUE en base de datos.
- **RF-05.6** El sistema debe validar el formato del número de factura antes de persistir.
- **RF-05.7** La generación del número de factura y su asignación a la venta debe ocurrir dentro de una transacción atómica.
- **RF-05.8** Si se alcanza el límite de 999,999 facturas en un día, el sistema debe retornar HTTP 409 CONFLICT.
- **RF-05.9** El mensaje de error al alcanzar el límite debe ser: "Se alcanzó el límite de 999,999 facturas para el período actual. Contacte al administrador."
- **RF-05.10** El sistema debe recomendar monitorear cuando se alcance el 90% del límite diario (899,999 facturas).

### RF-06: Consulta de Ventas
- **RF-06.1** El usuario autenticado puede consultar una venta específica por su ID.
- **RF-06.2** El usuario autenticado puede consultar una venta específica por su número de factura.
- **RF-06.3** El usuario autenticado puede listar todas las ventas registradas en el sistema con paginación.
- **RF-06.4** El sistema debe permitir filtrar ventas por fecha (formato ISO-8601 YYYY-MM-DD, aplicado al día completo 00:00:00 a 23:59:59), cajero, cédula de cliente y método de pago (retorna ventas que contengan AL MENOS un pago con ese método).
- **RF-06.4.1** El filtro por fecha debe aceptar formato ISO-8601 (YYYY-MM-DD) obligatoriamente.
- **RF-06.4.2** El filtro por fecha debe aplicarse al día completo (00:00:00 a 23:59:59).
- **RF-06.4.3** El sistema debe normalizar fechas en backend para evitar problemas de zona horaria.
- **RF-06.4.4** El filtro por método de pago debe retornar ventas que contengan AL MENOS un pago con ese método (ANY match).
- **RF-06.5** El sistema debe soportar paginación con tamaño de página configurable (predeterminado: 20 registros).
- **RF-06.7** Si se solicita una página que no existe, el sistema debe retornar lista vacía con metadata de paginación (totalPages, totalItems), no error 404.
- **RF-06.6** El sistema debe retornar HTTP 404 si se intenta acceder a una venta inexistente.
- **RF-06.7** Si una página solicitada no existe, el sistema debe retornar lista vacía (no HTTP 404).
- **RF-06.8** La respuesta de paginación debe incluir metadata: totalPages, totalItems, currentPage, pageSize.

### RF-07: Resumen de Venta Completo
- **RF-07.1** Al consultar una venta, el sistema debe retornar: número de factura, datos del cajero (username), datos del cliente (nombre y cédula), lista de productos con cantidades y precios, lista de pagos con métodos y montos, subtotal, tasa de impuesto, monto de impuesto, total final, fecha de registro, estado de reembolso y datos del reembolso si existe.
- **RF-07.2** Cada línea de detalle debe incluir: nombre del producto, cantidad, precio unitario y subtotal de la línea.
- **RF-07.3** Cada pago debe incluir: método de pago y monto.
- **RF-07.4** La respuesta debe estar en formato JSON estructurado y consistente con el estándar REST de la API.

### RF-08: Sistema de Reembolsos
- **RF-08.1** El usuario autenticado puede reembolsar una venta existente proporcionando el ID de la venta y un motivo.
- **RF-08.2** El motivo del reembolso debe tener entre 10 y 500 caracteres.
- **RF-08.3** El sistema debe validar que la venta no haya sido reembolsada previamente.
- **RF-08.4** El sistema solo permite reembolsos totales de la venta completa (no reembolsos parciales por producto o monto).
- **RF-08.5** Al reembolsar una venta, el sistema debe devolver automáticamente todos los productos al inventario incrementando el stock.
- **RF-08.6** El sistema debe permitir reembolsar ventas aunque el producto esté inactivo, siempre que el producto exista en la base de datos.
- **RF-08.7** El sistema debe incrementar el stock del producto aunque esté marcado como inactivo.
- **RF-08.8** El sistema debe separar el estado del producto (activo/inactivo) de su existencia histórica en ventas.
- **RF-08.9** El sistema debe registrar el reembolso con: ID de venta, motivo, fecha, usuario que autoriza (desde JWT) y nombre del usuario.
- **RF-08.10** El sistema debe marcar la venta como reembolsada (campo booleano).
- **RF-08.11** Una venta solo puede reembolsarse una vez (constraint UNIQUE en base de datos).
- **RF-08.12** El reembolso y la devolución de stock deben ocurrir en una transacción atómica.
- **RF-08.13** Al consultar una venta reembolsada, el sistema debe incluir los datos del reembolso en la respuesta.
- **RF-08.14** El modelo de datos debe estar preparado para soportar reembolsos parciales en versiones futuras.

### RF-09: Configuración de Tasa de Impuesto Global
- **RF-09.1** El sistema debe mantener una tasa de impuesto global configurable almacenada en base de datos.
- **RF-09.2** La tasa de impuesto predefinida debe ser 5% (0.05).
- **RF-09.3** El usuario autenticado puede consultar la tasa de impuesto global actual.
- **RF-09.4** El usuario autenticado puede actualizar la tasa de impuesto global.
- **RF-09.8** El sistema debe utilizar bloqueo explícito o aislamiento transaccional (SERIALIZABLE) para garantizar que las actualizaciones de tasa de impuesto sean secuenciales y evitar conflictos de concurrencia.
- **RF-09.5** La tasa de impuesto debe estar entre 0.0 y 1.0 (0% a 100%).
- **RF-09.6** Al registrar una venta, el sistema debe obtener automáticamente la tasa de impuesto desde la configuración global.
- **RF-09.7** La tasa de impuesto se almacena con cada venta para mantener el registro histórico.
- **RF-09.8** La actualización de la tasa de impuesto debe utilizar bloqueo explícito o aislamiento transaccional SERIALIZABLE.
- **RF-09.9** Las actualizaciones de la tasa de impuesto deben ser secuenciales para evitar condiciones de carrera.
- **RF-09.10** El sistema debe preferir locking explícito sobre la configuración en lugar de solo aislamiento transaccional.

### RF-10: Validaciones Específicas de Datos
- **RF-10.1** La cédula del cliente debe tener exactamente 10 dígitos numéricos sin guiones, puntos ni espacios.
- **RF-10.2** El nombre del cliente debe contener solo letras, espacios, tildes y ñ.
- **RF-10.3** El nombre del cliente debe tener máximo 50 caracteres.
- **RF-10.4** El sistema debe aplicar trim() al nombre del cliente antes de validar.
- **RF-10.5** El sistema debe normalizar múltiples espacios consecutivos en el nombre a un solo espacio separador.
- **RF-10.6** El nombre del cliente debe contener al menos 2 palabras válidas después de normalización.
- **RF-10.7** El username del cajero debe obtenerse automáticamente del token JWT.
- **RF-10.8** Cada método de pago debe ser uno de: EFECTIVO, TARJETA o TRANSFERENCIA.
- **RF-10.9** El monto de cada pago debe ser mayor a cero.
- **RF-10.10** La suma de todos los pagos debe ser exactamente igual al total de la venta.
- **RF-10.11** El sistema debe usar BigDecimal para todos los cálculos monetarios.
- **RF-10.12** El sistema debe redondear valores monetarios a 2 decimales con ROUND_HALF_UP antes de comparar.
- **RF-10.13** El sistema debe comparar valores monetarios usando compareTo() == 0 (no tolerancias arbitrarias como ±0.01).

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
| RN-07 | El total de la venta es la suma de `(precio_unitario × cantidad)` por cada línea de detalle, redondeado por línea. | Domain (`Venta.calcularSubtotal`) |
| RN-08 | La lógica de negocio del dominio no debe depender de ningún framework externo. | Domain |
| RN-09 | El nombre del cliente es obligatorio y debe contener al menos 2 palabras (nombre y apellido). | Application / Validation |
| RN-10 | La cédula del cliente es obligatoria y debe tener exactamente 10 dígitos numéricos. | Application / Validation |
| RN-11 | El método de pago debe ser uno de: EFECTIVO, TARJETA o TRANSFERENCIA. | Application / Validation |
| RN-12 | La tasa de impuesto debe estar entre 0.0 y 1.0 (0% a 100%). | Application / Validation |
| RN-13 | El número de factura debe ser único en todo el sistema. | Infrastructure (Database Constraint) |
| RN-14 | El número de factura debe seguir el formato FAC-YYYYMMDD-NNNNNN (6 dígitos). | Domain Service |
| RN-15 | La secuencia de factura se reinicia a 000001 cada día. | Application Service |
| RN-16 | El impuesto se calcula como `subtotal × tasa_impuesto` con redondeo ROUND_HALF_UP a 2 decimales. | Domain (`Venta.calcularImpuesto`) |
| RN-17 | El total final se calcula como `subtotal + impuesto` con redondeo ROUND_HALF_UP a 2 decimales. | Domain (`Venta.calcularTotal`) |
| RN-18 | El cajero se identifica automáticamente desde el username del token JWT. | Application Service |
| RN-19 | El nombre del cliente solo puede contener letras, espacios, tildes y ñ. | Application / Validation |
| RN-20 | El nombre del cliente debe tener máximo 50 caracteres. | Application / Validation |
| RN-21 | Una venta puede tener múltiples métodos de pago. | Domain (`Venta.pagos`) |
| RN-22 | La suma de todos los pagos debe ser exactamente igual al total de la venta. | Domain (`Venta.validarPagos`) |
| RN-23 | Cada pago debe tener un monto mayor a cero. | Application / Validation |
| RN-24 | Una venta solo puede reembolsarse una vez. | Infrastructure (Database Constraint) |
| RN-25 | Al reembolsar una venta, se debe devolver el stock de todos los productos. | Application Service |
| RN-26 | El motivo del reembolso debe tener entre 10 y 500 caracteres. | Application / Validation |
| RN-27 | La tasa de impuesto se obtiene de la configuración global al registrar una venta. | Application Service |
| RN-28 | La tasa de impuesto predefinida es 5% (0.05). | Infrastructure (Database) |
| RN-29 | El redondeo de decimales debe aplicarse por línea antes de sumar. | Domain (`DetalleVenta.calcularSubtotal`) |
| RN-30 | La fecha de la factura se toma al momento de CONFIRMAR la venta. | Application Service |
| RN-31 | El sistema debe implementar RBAC con un único rol ADMIN en MVP. | Infrastructure (Security) |
| RN-32 | La autorización debe estar centralizada en un único módulo. | Infrastructure (Security) |
| RN-33 | El filtro por método de pago retorna ventas con AL MENOS un pago de ese método (ANY match). | Application Service |
| RN-34 | El nombre del cliente debe normalizarse: trim() y múltiples espacios a uno solo. | Application / Validation |
| RN-35 | Los cálculos monetarios deben usar BigDecimal con redondeo ROUND_HALF_UP. | Domain |
| RN-36 | La comparación de valores monetarios debe usar compareTo() == 0. | Domain |
| RN-37 | Al alcanzar 999,999 facturas diarias, retornar HTTP 409 CONFLICT. | Application Service |
| RN-38 | Solo se permiten reembolsos totales (no parciales) en esta versión. | Application Service |
| RN-39 | El modelo de datos debe prepararse para reembolsos parciales futuros. | Domain Model |
| RN-40 | La actualización de tasa de impuesto debe usar bloqueo explícito o SERIALIZABLE. | Infrastructure (Repository) |
| RN-41 | Los filtros de fecha deben usar formato ISO-8601 (YYYY-MM-DD). | Application / Validation |
| RN-42 | Los filtros de fecha deben normalizarse en backend para evitar problemas de zona horaria. | Application Service |
| RN-43 | La paginación sin resultados retorna lista vacía (no HTTP 404). | Application Service |
| RN-44 | Las respuestas paginadas deben incluir metadata completa. | Application Service |
| RN-45 | Se permite reembolsar ventas con productos inactivos si existen históricamente. | Application Service |
| RN-46 | El stock se incrementa en reembolsos aunque el producto esté inactivo. | Application Service |

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

### CU-03: Registrar Venta POS con Facturación
- **Actor:** Usuario autenticado con rol ADMIN
- **Precondición:** Token JWT válido; productos con stock disponible; datos del cliente disponibles.
- **Port involucrado:** `RegistrarVentaUseCase`
- **Flujo principal:**
  1. El usuario envía datos de venta a `POST /api/ventas` incluyendo:
     - Lista de `{ productoId, cantidad }`
     - Nombre del cliente (obligatorio)
     - Cédula del cliente (obligatoria, exactamente 10 dígitos numéricos)
     - Lista de pagos con `{ metodoPago, monto }` (uno o más)
  2. `VentaController` delega a `RegistrarVentaUseCase`.
  3. `VentaService` valida autorización del usuario (rol ADMIN) mediante módulo centralizado.
  4. `VentaService` normaliza el nombre del cliente: aplica trim() y convierte múltiples espacios a uno solo.
  5. `VentaService` valida que el nombre contenga al menos 2 palabras después de normalización.
  6. `VentaService` valida que el nombre contenga solo letras, espacios, tildes y ñ (máximo 50 caracteres).
  7. `VentaService` obtiene el usuario autenticado desde el JWT para identificar al cajero (username).
  8. `ConfiguracionService` obtiene la tasa de impuesto global desde la configuración con bloqueo explícito.
  9. `SecuenciaFacturaService` genera el siguiente número de factura único (FAC-YYYYMMDD-NNNNNN).
  10. Si se alcanza el límite de 999,999 facturas, lanza excepción que retorna HTTP 409 CONFLICT con mensaje: "Se alcanzó el límite de 999,999 facturas para el período actual. Contacte al administrador."
  11. `VentaService` obtiene cada producto via `ProductoRepositoryPort` con bloqueo (SELECT FOR UPDATE).
  12. Llama a `producto.descontarStock(cantidad)` — el dominio valida el stock.
  13. Calcula subtotal por línea usando BigDecimal: `precio_unitario × cantidad` con redondeo ROUND_HALF_UP a 2 decimales.
  14. Calcula subtotal general: suma de todos los subtotales de líneas ya redondeados.
  15. Calcula impuesto usando BigDecimal: `subtotal × tasa_impuesto` con redondeo ROUND_HALF_UP a 2 decimales.
  16. Calcula total final usando BigDecimal: `subtotal + impuesto` con redondeo ROUND_HALF_UP a 2 decimales.
  17. Calcula suma de pagos usando BigDecimal con redondeo ROUND_HALF_UP a 2 decimales.
  18. Valida que suma de pagos sea exactamente igual al total usando compareTo() == 0 (no tolerancias arbitrarias).
  19. Asigna la fecha actual (LocalDateTime.now()) como fecha de confirmación.
  20. Persiste la venta con todos los datos (factura, cajero, cliente, detalles, pagos, cálculos) via `VentaRepositoryPort` en una transacción atómica.
  21. Retorna `VentaResponse` con todos los datos de la venta registrada.
- **Flujo alternativo:** 
  - Si el usuario no tiene rol ADMIN, retorna HTTP 403 FORBIDDEN.
  - Si `descontarStock` lanza `StockInsuficienteException`, la transacción hace rollback y retorna HTTP 400.
  - Si el nombre no cumple validaciones después de normalización, retorna HTTP 400 con mensaje específico.
  - Si faltan datos obligatorios del cliente, retorna HTTP 400 con mensaje de validación.
  - Si algún método de pago es inválido, retorna HTTP 400.
  - Si la suma de pagos no coincide con el total (compareTo() != 0), retorna HTTP 400 con `PagosInvalidosException`.
  - Si se alcanza el límite de 999,999 facturas en el día, retorna HTTP 409 CONFLICT con mensaje específico.

### CU-04: Consultar Venta por Número de Factura
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido; número de factura válido.
- **Port involucrado:** `ObtenerVentaPorFacturaUseCase`
- **Flujo principal:**
  1. El usuario envía solicitud a `GET /api/ventas/factura/{numeroFactura}`.
  2. `VentaController` delega a `ObtenerVentaPorFacturaUseCase`.
  3. `VentaService` busca la venta via `VentaRepositoryPort.findByNumeroFactura()`.
  4. Retorna `VentaResponse` con todos los datos de la venta (factura, cajero, cliente, productos, pagos, cálculos, estado de reembolso).
- **Flujo alternativo:** Si la factura no existe, retorna HTTP 404.

### CU-05: Listar Todas las Ventas con Filtros
- **Actor:** Usuario autenticado con rol ADMIN
- **Precondición:** Token JWT válido.
- **Port involucrado:** `ListarVentasUseCase`
- **Flujo principal:**
  1. El usuario envía solicitud a `GET /api/ventas` con parámetros opcionales:
     - `fecha`: filtrar por fecha específica (formato ISO-8601: YYYY-MM-DD)
     - `cajeroId`: filtrar por ID del cajero
     - `cedulaCliente`: filtrar por cédula del cliente
     - `metodoPago`: filtrar por método de pago (EFECTIVO, TARJETA, TRANSFERENCIA)
     - `page`: número de página (predeterminado: 0)
     - `size`: tamaño de página (predeterminado: 20)
  2. `VentaController` delega a `ListarVentasUseCase`.
  3. `VentaService` valida autorización del usuario (rol ADMIN) mediante módulo centralizado.
  4. `VentaService` valida y normaliza el filtro de fecha si está presente (formato ISO-8601).
  5. `VentaService` normaliza la fecha en backend para evitar problemas de zona horaria.
  6. `VentaService` aplica el filtro de fecha al día completo (00:00:00 a 23:59:59).
  7. `VentaService` aplica el filtro de método de pago con lógica ANY match (ventas con AL MENOS un pago de ese método).
  8. `VentaService` obtiene las ventas via `VentaRepositoryPort.findAll(filtro)` con paginación.
  9. Si la página solicitada no existe, retorna lista vacía (no HTTP 404).
  10. Retorna lista paginada de `VentaResponse` con metadata: totalPages, totalItems, currentPage, pageSize.
- **Flujo alternativo:** 
  - Si el usuario no tiene rol ADMIN, retorna HTTP 403 FORBIDDEN.
  - Si el formato de fecha es inválido, retorna HTTP 400 con mensaje de validación.
  - Si no hay ventas que cumplan los filtros, retorna lista vacía con metadata de paginación.

### CU-06: Reembolsar Venta
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido; venta existente no reembolsada previamente.
- **Port involucrado:** `ReembolsarVentaUseCase`
- **Flujo principal:**
  1. El usuario envía solicitud a `POST /api/ventas/{id}/reembolso` con:
     - `motivo`: razón del reembolso (10-500 caracteres)
  2. `VentaController` delega a `ReembolsarVentaUseCase`.
  3. `VentaService` obtiene la venta via `VentaRepositoryPort.findById()`.
  4. Valida que la venta no esté ya reembolsada.
  5. Valida que no exista un reembolso previo via `ReembolsoRepositoryPort.existsByVentaId()`.
  6. Para cada producto en la venta, obtiene el producto con bloqueo (SELECT FOR UPDATE).
  7. Incrementa el stock del producto con la cantidad vendida.
  8. Obtiene el usuario autenticado desde el JWT.
  9. Crea un registro de `Reembolso` con: ventaId, motivo, fecha actual, usuarioId, username.
  10. Persiste el reembolso via `ReembolsoRepositoryPort.save()`.
  11. Marca la venta como reembolsada (`venta.setReembolsada(true)`).
  12. Persiste la venta actualizada.
  13. Retorna `ReembolsoResponse` con los datos del reembolso.
- **Flujo alternativo:**
  - Si la venta no existe, retorna HTTP 404.
  - Si la venta ya está reembolsada, retorna HTTP 400 con `VentaYaReembolsadaException`.
  - Si el motivo no cumple validaciones, retorna HTTP 400.

### CU-07: Obtener Tasa de Impuesto Global
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido.
- **Port involucrado:** `ObtenerTasaImpuestoUseCase`
- **Flujo principal:**
  1. El usuario envía solicitud a `GET /api/configuracion/tasa-impuesto`.
  2. `ConfiguracionController` delega a `ObtenerTasaImpuestoUseCase`.
  3. `ConfiguracionService` busca la configuración via `ConfiguracionRepositoryPort.findByClave("tasa_impuesto")`.
  4. Si existe, retorna el valor como BigDecimal.
  5. Si no existe, retorna el valor predefinido (0.05).
- **Flujo alternativo:** Ninguno (siempre retorna un valor).

### CU-08: Actualizar Tasa de Impuesto Global
- **Actor:** Usuario autenticado
- **Precondición:** Token JWT válido.
- **Port involucrado:** `ActualizarTasaImpuestoUseCase`
- **Flujo principal:**
  1. El usuario envía solicitud a `PUT /api/configuracion/tasa-impuesto` con:
     - `tasaImpuesto`: nuevo valor (0.0 a 1.0)
  2. `ConfiguracionController` delega a `ActualizarTasaImpuestoUseCase`.
  3. `ConfiguracionService` valida que la tasa esté entre 0.0 y 1.0.
  4. Busca o crea la configuración con clave "tasa_impuesto".
  5. Actualiza el valor.
  6. Persiste via `ConfiguracionRepositoryPort.save()` con nivel de aislamiento SERIALIZABLE.
  7. Retorna confirmación.
- **Flujo alternativo:**
  - Si la tasa está fuera de rango, retorna HTTP 400 con mensaje de validación.
