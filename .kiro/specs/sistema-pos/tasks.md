# Tasks - Sistema POS
**Proyecto:** Point of Sale - Spring Boot + Arquitectura Hexagonal  
**Versión:** 3.2.0  
**Última Actualización:** 2026-05-08  
**Cambios:** Incorporadas clarificaciones de ambigüedades (RBAC, validaciones, precisión BigDecimal, HTTP 409, reembolsos totales, concurrencia, filtros ISO-8601, paginación, productos inactivos)  

---

## 1. Configuración Inicial del Proyecto

- [x] 1.1 Crear proyecto con Spring Initializr (Spring Boot 3.x, Java 21, Maven)
- [x] 1.2 Agregar dependencias en `pom.xml`:
  - [ ] `spring-boot-starter-web`
  - [ ] `spring-boot-starter-security`
  - [ ] `spring-boot-starter-data-jpa`
  - [ ] `spring-boot-starter-validation`
  - [ ] `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
  - [ ] `mysql-connector-j`
  - [ ] `springdoc-openapi-starter-webmvc-ui`
  - [ ] `lombok`
  - [ ] `junit-jupiter`, `mockito-core`, `assertj-core` (scope test)
  - [ ] `h2` (scope test)
- [x] 1.3 Crear estructura de paquetes hexagonal:
  - [ ] `domain/model`, `domain/exception`
  - [ ] `application/port/in`, `application/port/out`, `application/service`, `application/dto`
  - [ ] `infrastructure/adapter/in/web`, `infrastructure/adapter/out/persistence`, `infrastructure/config`
- [x] 1.4 Crear clase principal `PosApplication.java`
- [x] 1.5 Configurar `application.properties` (puerto, nombre de app, JWT secret, expiración)

---

## 2. Configuración de Base de Datos

- [ ] 2.1 Crear base de datos `pos_db` en MySQL/MariaDB
- [ ] 2.2 Configurar datasource en `application.properties` (URL, usuario, contraseña, driver)
- [ ] 2.3 Configurar Hibernate (`ddl-auto=validate`, dialecto, `show-sql=false` en prod)
- [ ] 2.4 Actualizar entidades JPA en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Actualizar `VentaEntity` con nuevos campos: `numeroFactura` (VARCHAR 50 UNIQUE), `nombreCajero`, `nombreCliente`, `cedulaCliente` (VARCHAR 10), `subtotal`, `tasaImpuesto` (DECIMAL 5,4), `impuesto`, `total`, `reembolsada` (BOOLEAN)
  - [ ] Crear `SecuenciaFacturaEntity` con campos: `id`, `fecha` (DATE UNIQUE), `ultimoNumero` (INT con CHECK 0-999999), `createdAt`, `updatedAt`
  - [ ] Crear `PagoVentaEntity` con campos: `id`, `ventaId`, `metodoPago` (VARCHAR 20 con CHECK), `monto` (DECIMAL 10,2 con CHECK > 0)
  - [ ] Crear `ReembolsoEntity` con campos: `id`, `ventaId` (UNIQUE), `motivo` (TEXT), `fecha`, `usuarioId`, `nombreUsuario`
  - [ ] Crear `ConfiguracionEntity` con campos: `id`, `clave` (VARCHAR 100 UNIQUE), `valor` (VARCHAR 255), `createdAt`, `updatedAt`
  - _Requerimientos: RF-03.4, RF-05.2, RF-08.5, RF-09.1_
- [ ] 2.5 Actualizar repositorios JPA:
  - [ ] Actualizar `JpaVentaRepository` con métodos: `findByNumeroFactura`, `existsByNumeroFactura`, `findAll(Specification, Pageable)` para filtros
  - [ ] Crear `JpaSecuenciaFacturaRepository` con método: `findByFechaForUpdate` (SELECT FOR UPDATE)
  - [ ] Crear `JpaPagoVentaRepository` con métodos: `saveAll`, `findByVentaId`
  - [ ] Crear `JpaReembolsoRepository` con métodos: `save`, `findByVentaId`, `existsByVentaId`
  - [ ] Crear `JpaConfiguracionRepository` con métodos: `findByClave`, `save`
  - _Requerimientos: RF-05.3, RF-06.4, RF-08.7, RF-09.1_
- [ ] 2.6 Crear script de migración SQL para actualizar base de datos existente:
  - [ ] Actualizar tabla `venta`: agregar columnas `numero_factura` (VARCHAR 50 UNIQUE), `nombre_cajero`, `nombre_cliente`, `cedula_cliente` (VARCHAR 10), `subtotal`, `tasa_impuesto` (DECIMAL 5,4), `impuesto`, `total`, `reembolsada` (BOOLEAN DEFAULT FALSE)
  - [ ] Crear tabla `secuencia_factura` con constraint CHECK en `ultimo_numero` (0-999999) y UNIQUE en `fecha`
  - [ ] Crear tabla `pago_venta` con constraint CHECK en `metodo_pago` (EFECTIVO|TARJETA|TRANSFERENCIA) y `monto` (> 0)
  - [ ] Crear tabla `reembolso` con constraint UNIQUE en `venta_id`
  - [ ] Crear tabla `configuracion` con constraint UNIQUE en `clave`
  - [ ] Insertar configuración predefinida: `INSERT INTO configuracion (clave, valor) VALUES ('tasa_impuesto', '0.05')`
  - [ ] Crear índices: `idx_numero_factura`, `idx_fecha`, `idx_cedula_cliente`, `idx_cajero`, `idx_reembolsada` (en venta), `idx_venta` (en pago_venta y reembolso), `idx_clave` (en configuracion)
  - [ ] Script de migración para datos existentes (generar números de factura retroactivos si aplica)
  - _Requerimientos: RF-05.2, RF-05.4, RF-06.4, RF-08.7, RF-09.2_
- [x] 2.7 Configurar `application-test.properties` con H2 en memoria para tests

---

## 3. Dominio — Modelos y Excepciones

- [x] 3.1 Crear clase `Producto` (POJO puro, sin anotaciones JPA):
  - [ ] Campos: `id`, `nombre`, `descripcion`, `precio`, `stock`, `activo`
  - [ ] Método `tieneStockSuficiente(int cantidad): boolean`
  - [ ] Método `descontarStock(int cantidad)` — lanza `StockInsuficienteException` si no hay stock
  - [ ] Método `incrementarStock(int cantidad)` — para reembolsos
- [ ] 3.2 Actualizar clase `Venta` con nuevos campos y métodos:
  - [ ] Agregar campos: `numeroFactura` (String), `nombreCajero` (String), `nombreCliente` (String), `cedulaCliente` (String), `pagos` (List<Pago>), `subtotal` (BigDecimal), `impuesto` (BigDecimal), `tasaImpuesto` (BigDecimal), `total` (BigDecimal), `reembolsada` (boolean)
  - [ ] Implementar método `calcularSubtotal()`: suma de subtotales de todos los detalles ya redondeados
  - [ ] Implementar método `calcularImpuesto(BigDecimal subtotal, BigDecimal tasaImpuesto)`: retorna `subtotal × tasaImpuesto` con redondeo ROUND_HALF_UP a 2 decimales
  - [ ] Implementar método `calcularTotal(BigDecimal subtotal, BigDecimal impuesto)`: retorna `subtotal + impuesto` con redondeo ROUND_HALF_UP a 2 decimales
  - [ ] Implementar método `calcularTotales()`: orquesta el cálculo completo (subtotal, impuesto, total)
  - [ ] Implementar método `validarPagos()`: valida que la suma de pagos sea exactamente igual al total
  - _Requerimientos: RF-03.4, RF-03.5, RF-04.1, RF-04.2, RF-04.3, RF-04.4, RF-04.7_
- [x] 3.3 Crear clase `DetalleVenta` con método `calcularSubtotal()` (precio × cantidad con redondeo ROUND_HALF_UP a 2 decimales)
  - _Requerimientos: RF-04.1, RF-04.7_
- [ ] 3.4 Crear clase `Pago` (POJO puro):
  - [ ] Campos: `id`, `ventaId`, `metodoPago` (String), `monto` (BigDecimal)
  - [ ] Constructor que aplica redondeo ROUND_HALF_UP a 2 decimales en el monto
  - _Requerimientos: RF-03.4_
- [ ] 3.5 Crear clase `Reembolso` (POJO puro):
  - [ ] Campos: `id`, `ventaId`, `motivo` (String), `fecha` (LocalDateTime), `usuarioId`, `nombreUsuario` (String)
  - [ ] Constructor que asigna fecha actual automáticamente
  - _Requerimientos: RF-08.5_
- [ ] 3.6 Crear clase `Configuracion` (POJO puro):
  - [ ] Campos: `id`, `clave` (String), `valor` (String)
  - [ ] Constantes: `TASA_IMPUESTO_KEY = "tasa_impuesto"`, `TASA_IMPUESTO_DEFAULT = "0.05"`
  - [ ] Método `getValorComoDecimal()`: convierte valor String a BigDecimal
  - _Requerimientos: RF-09.1, RF-09.2_
- [x] 3.7 Crear clase `Usuario` (POJO puro)
- [x] 3.8 Crear `StockInsuficienteException` (RuntimeException) con mensaje descriptivo
- [x] 3.9 Crear `RecursoNoEncontradoException` (RuntimeException)
- [ ] 3.10 Crear `FacturaDuplicadaException` (RuntimeException) para números de factura duplicados
  - _Requerimientos: RF-05.4_
- [ ] 3.11 Crear `PagosInvalidosException` (RuntimeException) cuando la suma de pagos no coincide con el total
  - _Requerimientos: RF-03.5_
- [ ] 3.12 Crear `VentaYaReembolsadaException` (RuntimeException) cuando se intenta reembolsar una venta ya reembolsada
  - _Requerimientos: RF-08.3_
- [ ] 3.13 Crear `LimiteFacturasDiarioExcedidoException` (RuntimeException) cuando se alcanza el límite de 999,999 facturas por día
  - _Requerimientos: RF-05.8_

**Tests unitarios del dominio:**
- [ ] 3.14 `ProductoTest`: verificar `descontarStock` con stock suficiente
- [ ] 3.15 `ProductoTest`: verificar que lanza `StockInsuficienteException` con stock insuficiente
- [ ] 3.16 `ProductoTest`: verificar `tieneStockSuficiente` retorna false cuando stock < cantidad
- [ ] 3.17 `ProductoTest`: verificar `incrementarStock` aumenta el stock correctamente (para reembolsos)
- [ ] 3.18 `VentaTest`: verificar `calcularSubtotal` suma correctamente todos los subtotales de detalles
- [ ] 3.19 `VentaTest`: verificar `calcularImpuesto` retorna `subtotal × tasaImpuesto` con redondeo ROUND_HALF_UP
- [ ] 3.20 `VentaTest`: verificar `calcularTotal` retorna `subtotal + impuesto` con redondeo ROUND_HALF_UP
- [ ] 3.21 `VentaTest`: verificar `calcularTotales` actualiza correctamente subtotal, impuesto y total
- [ ] 3.22 `VentaTest`: verificar `validarPagos` no lanza excepción cuando suma de pagos = total
- [ ] 3.23 `VentaTest`: verificar `validarPagos` lanza `PagosInvalidosException` cuando suma de pagos ≠ total
- [ ] 3.24 `DetalleVentaTest`: verificar `calcularSubtotal` = precio × cantidad con redondeo ROUND_HALF_UP a 2 decimales
- [ ] 3.25 `PagoTest`: verificar constructor aplica redondeo ROUND_HALF_UP a 2 decimales en el monto

---

## 3.5. Dominio — Servicio de Dominio (GeneradorNumeroFactura)

- [ ] 3.26 Crear clase `GeneradorNumeroFactura` en `domain/service/`:
  - [ ] Implementar método `generar(LocalDate fecha, int secuencia): String` que retorna formato FAC-YYYYMMDD-NNNNNN (6 dígitos)
  - [ ] Implementar método `validarFormato(String numeroFactura): boolean` que valida el patrón regex `^FAC-\\d{8}-\\d{6}$`
  - _Requerimientos: RF-05.2, RF-05.5_

**Tests unitarios del servicio de dominio:**
- [ ] 3.27 `GeneradorNumeroFacturaTest`: verificar `generar` retorna formato correcto FAC-YYYYMMDD-NNNNNN (6 dígitos)
- [ ] 3.28 `GeneradorNumeroFacturaTest`: verificar `validarFormato` retorna true para formatos válidos con 6 dígitos
- [ ] 3.29 `GeneradorNumeroFacturaTest`: verificar `validarFormato` retorna false para formatos inválidos (4 dígitos, 5 dígitos, 7 dígitos)
- [ ] 3.30 `GeneradorNumeroFacturaTest`: verificar secuencia con ceros a la izquierda (000001, 000099, 000999, 099999, 999999)

---

## 4. Application — Ports (Interfaces)

- [x] 4.1 Crear Input Ports en `application/port/in/producto/`:
  - [ ] `CrearProductoUseCase` — método `crear(CrearProductoCommand): ProductoResponse`
  - [ ] `ListarProductosUseCase` — método `listar(): List<ProductoResponse>`
  - [ ] `ObtenerProductoUseCase` — método `obtener(Long id): ProductoResponse`
  - [ ] `ActualizarProductoUseCase` — método `actualizar(Long id, ActualizarProductoCommand): ProductoResponse`
  - [ ] `EliminarProductoUseCase` — método `eliminar(Long id): void`
- [ ] 4.2 Actualizar Input Ports en `application/port/in/venta/`:
  - [ ] `RegistrarVentaUseCase` — método `registrar(RegistrarVentaCommand): VentaResponse`
  - [ ] `ListarVentasUseCase` — método `listar(FiltroVentasCommand): Page<VentaResponse>` con paginación y filtros
  - [ ] `ObtenerVentaUseCase` — método `obtenerPorId(Long id): VentaResponse`
  - [ ] `ObtenerVentaPorFacturaUseCase` — método `obtenerPorNumeroFactura(String numeroFactura): VentaResponse`
  - [ ] `ReembolsarVentaUseCase` — método `reembolsar(ReembolsarVentaCommand): ReembolsoResponse` (NUEVO)
  - _Requerimientos: RF-06.1, RF-06.2, RF-06.4, RF-08.1_
- [ ] 4.3 Crear Input Ports en `application/port/in/configuracion/` (NUEVO):
  - [ ] `ObtenerTasaImpuestoUseCase` — método `obtenerTasaImpuesto(): BigDecimal`
  - [ ] `ActualizarTasaImpuestoUseCase` — método `actualizar(BigDecimal nuevaTasa): void`
  - _Requerimientos: RF-09.3, RF-09.4_
- [x] 4.4 Crear Input Port `LoginUseCase` — método `login(LoginCommand): LoginResponse`
- [ ] 4.5 Actualizar Output Ports en `application/port/out/`:
  - [ ] `ProductoRepositoryPort` — agregar método `findByIdForUpdate(Long id): Optional<Producto>` para SELECT FOR UPDATE
  - [ ] `VentaRepositoryPort` — agregar métodos:
    - [ ] `findByNumeroFactura(String numeroFactura): Optional<Venta>`
    - [ ] `existsByNumeroFactura(String numeroFactura): boolean`
    - [ ] `findAll(FiltroVentas filtro): Page<Venta>` con paginación y filtros
  - [ ] `PagoVentaRepositoryPort` — crear nuevo port (NUEVO):
    - [ ] `saveAll(List<Pago> pagos): List<Pago>`
    - [ ] `findByVentaId(Long ventaId): List<Pago>`
  - [ ] `ReembolsoRepositoryPort` — crear nuevo port (NUEVO):
    - [ ] `save(Reembolso reembolso): Reembolso`
    - [ ] `findByVentaId(Long ventaId): Optional<Reembolso>`
    - [ ] `existsByVentaId(Long ventaId): boolean`
  - [ ] `ConfiguracionRepositoryPort` — crear nuevo port (NUEVO):
    - [ ] `findByClave(String clave): Optional<Configuracion>`
    - [ ] `save(Configuracion configuracion): Configuracion`
  - [ ] `UsuarioRepositoryPort` — agregar método:
    - [ ] `findById(Long id): Optional<Usuario>`
  - [ ] `SecuenciaFacturaRepositoryPort` — crear nuevo port:
    - [ ] `obtenerSiguienteNumero(LocalDate fecha): int`
    - [ ] `actualizarSecuencia(LocalDate fecha, int numero): void`
  - _Requerimientos: RF-05.3, RF-05.6, RF-06.2, RF-06.4, RF-08.7, RF-09.1_
- [ ] 4.6 Actualizar Commands y Responses en `application/dto/`:
  - [ ] Crear `PagoCommand` (NUEVO):
    - [ ] `metodoPago` (String, @NotBlank, @Pattern para EFECTIVO|TARJETA|TRANSFERENCIA)
    - [ ] `monto` (BigDecimal, @NotNull, @DecimalMin("0.01"))
  - [ ] Actualizar `RegistrarVentaCommand`:
    - [ ] `nombreCliente` (String, @NotBlank, @Size(min=3, max=50), @Pattern para letras/espacios/tildes/ñ)
    - [ ] `cedulaCliente` (String, @NotBlank, @Pattern para exactamente 10 dígitos)
    - [ ] `pagos` (List<PagoCommand>, @NotEmpty, @Valid) — NUEVO: múltiples métodos de pago
    - [ ] Remover campo `metodoPago` (ahora es una lista de pagos)
    - [ ] Remover campo `tasaImpuesto` (se obtiene de configuración global)
  - [ ] Crear `ReembolsarVentaCommand` (NUEVO):
    - [ ] `ventaId` (Long, @NotNull)
    - [ ] `motivo` (String, @NotBlank, @Size(min=10, max=500))
    - [ ] `usuarioId` (Long, se obtiene del JWT)
  - [ ] Crear `FiltroVentasCommand` (NUEVO):
    - [ ] `fecha` (LocalDate, opcional)
    - [ ] `cajeroId` (Long, opcional)
    - [ ] `cedulaCliente` (String, opcional)
    - [ ] `metodoPago` (String, opcional)
    - [ ] `page` (int, default 0)
    - [ ] `size` (int, default 20)
  - [ ] Crear `ActualizarTasaImpuestoCommand` (NUEVO):
    - [ ] `tasaImpuesto` (BigDecimal, @NotNull, @DecimalMin("0.0"), @DecimalMax("1.0"))
  - [ ] Crear `PagoResponse` (NUEVO):
    - [ ] `id`, `metodoPago`, `monto`
  - [ ] Actualizar `VentaResponse`:
    - [ ] Agregar `pagos` (List<PagoResponse>) — NUEVO
    - [ ] Agregar `reembolsada` (boolean) — NUEVO
    - [ ] Agregar `reembolso` (ReembolsoResponse, nullable) — NUEVO
    - [ ] Remover campo `metodoPago` (ahora es una lista de pagos)
  - [ ] Crear `ReembolsoResponse` (NUEVO):
    - [ ] `id`, `ventaId`, `motivo`, `fecha`, `usuarioId`, `nombreUsuario`
  - [ ] Crear `ConfiguracionResponse` (NUEVO):
    - [ ] `clave`, `valor`, `valorDecimal`
  - _Requerimientos: RF-03.2, RF-03.3, RF-03.4, RF-06.4, RF-07.1, RF-07.3, RF-08.2, RF-08.9, RF-09.3, RF-10.1, RF-10.2, RF-10.3, RF-10.4, RF-10.6, RF-10.7_

---

## 5. Application — Servicios (Casos de Uso)

- [x] 5.1 Implementar `ProductoService` (implementa los 5 use cases de producto):
  - [ ] `crear`: validar datos, mapear a dominio, persistir via port
  - [ ] `listar`: obtener todos los activos via port
  - [ ] `obtener`: buscar por ID, lanzar `RecursoNoEncontradoException` si no existe
  - [ ] `actualizar`: buscar, actualizar campos, persistir
  - [ ] `eliminar`: eliminación lógica (`activo = false`)
- [ ] 5.2 Actualizar `VentaService` con nuevas funcionalidades:
  - [ ] Inyectar `SecuenciaFacturaService`, `ConfiguracionService`, `PagoVentaRepositoryPort`, `ReembolsoRepositoryPort` y `GeneradorNumeroFactura`
  - [ ] Actualizar método `registrar`:
    - [ ] Obtener usuario autenticado desde JWT via `SecurityContextHolder`
    - [ ] Extraer username del usuario autenticado como nombre del cajero
    - [ ] Obtener tasa de impuesto global via `ConfiguracionService.obtenerTasaImpuesto()`
    - [ ] Generar número de factura único via `SecuenciaFacturaService.obtenerSiguienteNumeroFactura()`
    - [ ] Validar stock de productos con SELECT FOR UPDATE via `ProductoRepositoryPort.findByIdForUpdate()`
    - [ ] Calcular subtotal por línea (precio × cantidad) con redondeo ROUND_HALF_UP a 2 decimales
    - [ ] Calcular subtotal general (suma de subtotales de líneas ya redondeados)
    - [ ] Calcular impuesto (subtotal × tasaImpuesto) con redondeo ROUND_HALF_UP a 2 decimales
    - [ ] Calcular total final (subtotal + impuesto) con redondeo ROUND_HALF_UP a 2 decimales
    - [ ] Validar que la suma de todos los pagos sea exactamente igual al total via `venta.validarPagos()`
    - [ ] Asignar fecha actual (LocalDateTime.now()) como fecha de confirmación
    - [ ] Persistir venta con `@Transactional(isolation = Isolation.REPEATABLE_READ)`
    - [ ] Persistir pagos via `PagoVentaRepositoryPort.saveAll()`
    - [ ] Descontar stock de productos
  - [ ] Implementar método `obtenerPorId(Long id)`: buscar venta por ID, incluir datos de reembolso si existe
  - [ ] Implementar método `obtenerPorNumeroFactura(String numeroFactura)`: buscar venta por número de factura, incluir datos de reembolso si existe
  - [ ] Implementar método `listar(FiltroVentasCommand filtro)`: listar ventas con paginación y filtros (fecha, cajero, cédula cliente, método de pago)
  - [ ] Implementar método `reembolsar(ReembolsarVentaCommand cmd)` (NUEVO):
    - [ ] Obtener venta por ID
    - [ ] Validar que no esté ya reembolsada
    - [ ] Validar que no exista reembolso previo via `ReembolsoRepositoryPort.existsByVentaId()`
    - [ ] Devolver productos al inventario con SELECT FOR UPDATE via `ProductoRepositoryPort.findByIdForUpdate()`
    - [ ] Incrementar stock de cada producto con la cantidad vendida
    - [ ] Obtener usuario autenticado desde JWT
    - [ ] Crear registro de `Reembolso` con ventaId, motivo, fecha actual, usuarioId, username
    - [ ] Persistir reembolso via `ReembolsoRepositoryPort.save()`
    - [ ] Marcar venta como reembolsada (`venta.setReembolsada(true)`)
    - [ ] Persistir venta actualizada
    - [ ] Anotar con `@Transactional(isolation = Isolation.REPEATABLE_READ)` para garantizar atomicidad
  - _Requerimientos: RF-03.5, RF-03.6, RF-03.10, RF-04.1, RF-04.2, RF-04.3, RF-04.4, RF-04.7, RF-06.1, RF-06.2, RF-06.4, RF-08.1, RF-08.4, RF-08.5, RF-08.8_
- [ ] 5.3 Crear `SecuenciaFacturaService` (nuevo servicio de aplicación):
  - [ ] Inyectar `SecuenciaFacturaRepositoryPort`, `VentaRepositoryPort` y `GeneradorNumeroFactura`
  - [ ] Implementar método `obtenerSiguienteNumeroFactura()`:
    - [ ] Obtener fecha actual (LocalDate.now())
    - [ ] Obtener siguiente número de secuencia via `SecuenciaFacturaRepositoryPort.obtenerSiguienteNumero(fecha)` con SELECT FOR UPDATE
    - [ ] Validar límite diario (si > 999999, lanzar `LimiteFacturasDiarioExcedidoException`)
    - [ ] Generar número de factura con `GeneradorNumeroFactura.generar(fecha, secuencia)` (formato FAC-YYYYMMDD-NNNNNN con 6 dígitos)
    - [ ] Verificar unicidad con `VentaRepositoryPort.existsByNumeroFactura()`
    - [ ] Si existe, lanzar `FacturaDuplicadaException`
    - [ ] Actualizar secuencia via `SecuenciaFacturaRepositoryPort.actualizarSecuencia(fecha, numero)`
    - [ ] Retornar número de factura generado
  - [ ] Anotar con `@Transactional(isolation = Isolation.REPEATABLE_READ)` para garantizar atomicidad
  - _Requerimientos: RF-05.1, RF-05.2, RF-05.3, RF-05.4, RF-05.6, RF-05.8_
- [ ] 5.4 Crear `ConfiguracionService` (nuevo servicio de aplicación):
  - [ ] Inyectar `ConfiguracionRepositoryPort`
  - [ ] Implementar método `obtenerTasaImpuesto()`:
    - [ ] Buscar configuración con clave "tasa_impuesto" via `ConfiguracionRepositoryPort.findByClave()`
    - [ ] Si existe, retornar valor como BigDecimal
    - [ ] Si no existe, retornar valor predefinido (0.05)
  - [ ] Implementar método `actualizarTasaImpuesto(BigDecimal nuevaTasa)`:
    - [ ] Validar que la tasa esté entre 0.0 y 1.0
    - [ ] Buscar o crear configuración con clave "tasa_impuesto"
    - [ ] Actualizar valor
    - [ ] Persistir via `ConfiguracionRepositoryPort.save()`
    - [ ] Anotar con `@Transactional(isolation = Isolation.SERIALIZABLE)` para máximo nivel de aislamiento
  - _Requerimientos: RF-09.1, RF-09.2, RF-09.3, RF-09.4, RF-09.5, RF-09.6_
- [x] 5.5 Implementar `AuthService` (implementa `LoginUseCase`):
  - [ ] Cargar usuario via `UsuarioRepositoryPort`
  - [ ] Validar contraseña con `PasswordEncoder`
  - [ ] Generar y retornar token JWT

**Tests unitarios de servicios (con Mockito):**
- [ ] 5.6 `ProductoServiceTest`:
  - [ ] `crear_debeRetornarProductoCreado_cuandoDatosValidos`
  - [ ] `obtener_debeLanzarExcepcion_cuandoProductoNoExiste`
  - [ ] `eliminar_debeMarcarInactivo_cuandoProductoExiste`
- [ ] 5.7 `VentaServiceTest`:
  - [ ] `registrar_debeGenerarNumeroFacturaUnico_alConfirmarVenta`
  - [ ] `registrar_debeObtenerNombreCajeroDesdeJWT_alConfirmarVenta`
  - [ ] `registrar_debeObtenerTasaImpuestoDesdeConfiguracion_alConfirmarVenta`
  - [ ] `registrar_debeCalcularSubtotalPorLineaConRedondeo_conMultiplesProductos`
  - [ ] `registrar_debeCalcularSubtotalGeneralCorrectamente_sumandoLineasRedondeadas`
  - [ ] `registrar_debeCalcularImpuestoConRedondeo_conTasaImpuesto`
  - [ ] `registrar_debeCalcularTotalConRedondeo_sumandoSubtotalEImpuesto`
  - [ ] `registrar_debeGuardarDatosCliente_alConfirmarVenta`
  - [ ] `registrar_debeGuardarMultiplesPagos_alConfirmarVenta`
  - [ ] `registrar_debeValidarSumaDePagosIgualAlTotal_alConfirmarVenta`
  - [ ] `registrar_debeLanzarPagosInvalidosException_cuandoSumaDePagosNoCoincide`
  - [ ] `registrar_debeDescontarStock_alConfirmarVenta`
  - [ ] `registrar_debeLanzarStockInsuficienteException_cuandoStockEsInsuficiente`
  - [ ] `registrar_noDebeDescontarStock_siAlgunProductoFalla` (atomicidad)
  - [ ] `obtenerPorId_debeRetornarVenta_cuandoExiste`
  - [ ] `obtenerPorId_debeLanzarExcepcion_cuandoNoExiste`
  - [ ] `obtenerPorNumeroFactura_debeRetornarVenta_cuandoExiste`
  - [ ] `obtenerPorNumeroFactura_debeLanzarExcepcion_cuandoNoExiste`
  - [ ] `listar_debeRetornarVentasPaginadas_conFiltros`
  - [ ] `reembolsar_debeCrearReembolsoYDevolverStock_cuandoVentaValida`
  - [ ] `reembolsar_debeLanzarVentaYaReembolsadaException_cuandoVentaYaReembolsada`
  - [ ] `reembolsar_debeIncrementarStockDeProductos_alReembolsar`
  - [ ] `reembolsar_debeMarcarVentaComoReembolsada_alReembolsar`
- [ ] 5.8 `SecuenciaFacturaServiceTest`:
  - [ ] `obtenerSiguienteNumeroFactura_debeGenerarFormatoCorrecto_con6Digitos`
  - [ ] `obtenerSiguienteNumeroFactura_debeIncrementarSecuencia_enMismoDia`
  - [ ] `obtenerSiguienteNumeroFactura_debeReiniciarSecuencia_enNuevoDia`
  - [ ] `obtenerSiguienteNumeroFactura_debeLanzarExcepcion_siNumeroFacturaDuplicado`
  - [ ] `obtenerSiguienteNumeroFactura_debeLanzarLimiteFacturasDiarioExcedidoException_cuandoSuperaLimite`
  - [ ] `obtenerSiguienteNumeroFactura_debeActualizarSecuencia_enTransaccion`
- [ ] 5.9 `ConfiguracionServiceTest`:
  - [ ] `obtenerTasaImpuesto_debeRetornarValorConfigurado_cuandoExiste`
  - [ ] `obtenerTasaImpuesto_debeRetornarValorPredefinido_cuandoNoExiste`
  - [ ] `actualizarTasaImpuesto_debeActualizarValor_cuandoTasaValida`
  - [ ] `actualizarTasaImpuesto_debeLanzarValidationException_cuandoTasaFueraDeRango`
- [ ] 5.10 `AuthServiceTest`:
  - [ ] `login_debeRetornarToken_cuandoCredencialesValidas`
  - [ ] `login_debeLanzarExcepcion_cuandoPasswordIncorrecto`

---

## 6. Infrastructure — Adapters de Persistencia

- [ ] 6.1 Actualizar `VentaEntity` en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Agregar campos: `numeroFactura` (String, @Column unique, nullable=false), `nombreCajero` (String), `nombreCliente` (String), `cedulaCliente` (String, length=10), `subtotal` (BigDecimal), `tasaImpuesto` (BigDecimal, precision=5, scale=4), `impuesto` (BigDecimal), `total` (BigDecimal), `reembolsada` (boolean, default=false)
  - [ ] Agregar relación `@OneToMany` con `PagoVentaEntity` (cascade ALL, orphanRemoval true)
  - [ ] Agregar relación `@OneToOne` con `ReembolsoEntity` (mappedBy="venta")
  - [ ] Remover campo `metodoPago` (ahora es una lista de pagos)
  - _Requerimientos: RF-03.4, RF-05.2, RF-05.4, RF-08.6_
- [ ] 6.2 Crear `SecuenciaFacturaEntity` en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Campos: `id` (Long, @Id, @GeneratedValue), `fecha` (LocalDate, @Column unique, nullable=false), `ultimoNumero` (int), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime)
  - [ ] Agregar constraint `@Column(unique = true)` en `fecha`
  - _Requerimientos: RF-05.3_
- [ ] 6.3 Crear `PagoVentaEntity` en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Campos: `id` (Long, @Id, @GeneratedValue), `ventaId` (Long), `metodoPago` (String, length=20), `monto` (BigDecimal, precision=10, scale=2)
  - [ ] Agregar relación `@ManyToOne` con `VentaEntity`
  - [ ] Agregar validación `@Check` para `metodoPago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')`
  - [ ] Agregar validación `@Check` para `monto > 0`
  - _Requerimientos: RF-03.4, RF-10.6, RF-10.7_
- [ ] 6.4 Crear `ReembolsoEntity` en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Campos: `id` (Long, @Id, @GeneratedValue), `ventaId` (Long, @Column unique), `motivo` (String, @Column type=TEXT), `fecha` (LocalDateTime), `usuarioId` (Long), `nombreUsuario` (String)
  - [ ] Agregar relación `@OneToOne` con `VentaEntity`
  - [ ] Agregar relación `@ManyToOne` con `UsuarioEntity`
  - [ ] Agregar constraint `@Column(unique = true)` en `ventaId`
  - _Requerimientos: RF-08.5, RF-08.7_
- [ ] 6.5 Crear `ConfiguracionEntity` en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] Campos: `id` (Long, @Id, @GeneratedValue), `clave` (String, @Column unique, length=100), `valor` (String, length=255), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime)
  - [ ] Agregar constraint `@Column(unique = true)` en `clave`
  - _Requerimientos: RF-09.1_
- [ ] 6.6 Actualizar `JpaVentaRepository` en `infrastructure/adapter/out/persistence/repository/`:
  - [ ] Agregar método `Optional<VentaEntity> findByNumeroFactura(String numeroFactura)`
  - [ ] Agregar método `boolean existsByNumeroFactura(String numeroFactura)`
  - [ ] Agregar método `Page<VentaEntity> findAll(Specification<VentaEntity> spec, Pageable pageable)` para filtros dinámicos
  - [ ] Crear `VentaSpecification` con métodos estáticos para filtros: `porFecha`, `porCajero`, `porCedulaCliente`, `porMetodoPago`
  - _Requerimientos: RF-06.2, RF-06.4_
- [ ] 6.7 Crear `JpaSecuenciaFacturaRepository` en `infrastructure/adapter/out/persistence/repository/`:
  - [ ] Extender `JpaRepository<SecuenciaFacturaEntity, Long>`
  - [ ] Agregar método `@Query("SELECT s FROM SecuenciaFacturaEntity s WHERE s.fecha = :fecha FOR UPDATE") Optional<SecuenciaFacturaEntity> findByFechaForUpdate(@Param("fecha") LocalDate fecha)`
  - _Requerimientos: RF-05.3_
- [ ] 6.8 Crear `JpaPagoVentaRepository` en `infrastructure/adapter/out/persistence/repository/`:
  - [ ] Extender `JpaRepository<PagoVentaEntity, Long>`
  - [ ] Agregar método `List<PagoVentaEntity> findByVentaId(Long ventaId)`
- [ ] 6.9 Crear `JpaReembolsoRepository` en `infrastructure/adapter/out/persistence/repository/`:
  - [ ] Extender `JpaRepository<ReembolsoEntity, Long>`
  - [ ] Agregar método `Optional<ReembolsoEntity> findByVentaId(Long ventaId)`
  - [ ] Agregar método `boolean existsByVentaId(Long ventaId)`
  - _Requerimientos: RF-08.7_
- [ ] 6.10 Crear `JpaConfiguracionRepository` en `infrastructure/adapter/out/persistence/repository/`:
  - [ ] Extender `JpaRepository<ConfiguracionEntity, Long>`
  - [ ] Agregar método `Optional<ConfiguracionEntity> findByClave(String clave)`
  - _Requerimientos: RF-09.1_
- [ ] 6.11 Actualizar `ProductoMapper` para convertir entre `Producto` (dominio) y `ProductoEntity` (JPA)
- [ ] 6.12 Actualizar `VentaMapper` para incluir todos los nuevos campos:
  - [ ] Mapear `numeroFactura`, `nombreCajero`, `nombreCliente`, `cedulaCliente`, `subtotal`, `tasaImpuesto`, `impuesto`, `total`, `reembolsada`
  - [ ] Mapear lista de `pagos` (List<Pago> ↔ List<PagoVentaEntity>)
  - [ ] Mapear `reembolso` (Reembolso ↔ ReembolsoEntity) si existe
  - [ ] Mapear `nombreProducto` en `DetalleVentaResponse`
  - _Requerimientos: RF-07.1, RF-07.2, RF-07.3_
- [ ] 6.13 Crear `PagoMapper` para convertir entre `Pago` (dominio) y `PagoVentaEntity` (JPA)
- [ ] 6.14 Crear `ReembolsoMapper` para convertir entre `Reembolso` (dominio) y `ReembolsoEntity` (JPA)
- [ ] 6.15 Crear `ConfiguracionMapper` para convertir entre `Configuracion` (dominio) y `ConfiguracionEntity` (JPA)
- [ ] 6.16 Actualizar `ProductoRepositoryAdapter` (implementa `ProductoRepositoryPort`):
  - [ ] Implementar `findByIdForUpdate(Long id)` con SELECT FOR UPDATE
  - [ ] Delegar a `JpaProductoRepository`
  - [ ] Usar `ProductoMapper` para conversión
- [ ] 6.17 Actualizar `VentaRepositoryAdapter` (implementa `VentaRepositoryPort`):
  - [ ] Implementar `findByNumeroFactura(String numeroFactura)`
  - [ ] Implementar `existsByNumeroFactura(String numeroFactura)`
  - [ ] Implementar `findAll(FiltroVentas filtro)` con paginación y filtros usando `VentaSpecification`
- [ ] 6.18 Actualizar `UsuarioRepositoryAdapter` (implementa `UsuarioRepositoryPort`):
  - [ ] Implementar `findById(Long id)`
- [ ] 6.19 Crear `SecuenciaFacturaRepositoryAdapter` (implementa `SecuenciaFacturaRepositoryPort`):
  - [ ] Implementar `obtenerSiguienteNumero(LocalDate fecha)`:
    - [ ] Buscar secuencia por fecha con SELECT FOR UPDATE
    - [ ] Si no existe, crear nueva con numero = 1
    - [ ] Si existe, retornar ultimoNumero + 1
  - [ ] Implementar `actualizarSecuencia(LocalDate fecha, int numero)`:
    - [ ] Actualizar o crear registro con el nuevo número
  - [ ] Anotar con `@Transactional` para garantizar atomicidad
  - _Requerimientos: RF-05.3, RF-05.6_
- [ ] 6.20 Crear `PagoVentaRepositoryAdapter` (implementa `PagoVentaRepositoryPort`):
  - [ ] Implementar `saveAll(List<Pago> pagos)`
  - [ ] Implementar `findByVentaId(Long ventaId)`
- [ ] 6.21 Crear `ReembolsoRepositoryAdapter` (implementa `ReembolsoRepositoryPort`):
  - [ ] Implementar `save(Reembolso reembolso)`
  - [ ] Implementar `findByVentaId(Long ventaId)`
  - [ ] Implementar `existsByVentaId(Long ventaId)`
- [ ] 6.22 Crear `ConfiguracionRepositoryAdapter` (implementa `ConfiguracionRepositoryPort`):
  - [ ] Implementar `findByClave(String clave)`
  - [ ] Implementar `save(Configuracion configuracion)`

---

## 7. Infrastructure — Seguridad JWT

- [ ] 7.1 Crear `JwtUtil` en `infrastructure/config/`:
  - [ ] `generateToken(String username): String`
  - [ ] `validateToken(String token): boolean`
  - [ ] `extractUsername(String token): String`
  - [ ] Leer secret y expiración desde `application.properties`
- [ ] 7.2 Implementar `UserDetailsServiceImpl` que cargue usuario via `UsuarioRepositoryPort`
- [ ] 7.3 Crear `JwtAuthFilter` (`OncePerRequestFilter`) para validar token en cada request
- [ ] 7.4 Configurar `SecurityConfig`:
  - [ ] Deshabilitar CSRF
  - [ ] Rutas públicas: `POST /api/auth/login`
  - [ ] Registrar `JwtAuthFilter`
  - [ ] Configurar `BCryptPasswordEncoder` como bean
- [ ] 7.5 Crear `BeanConfig` para exponer `AuthenticationManager` como bean

**Tests unitarios de seguridad:**
- [ ] 7.6 `JwtUtilTest`:
  - [ ] `generateToken_debeRetornarTokenValido`
  - [ ] `validateToken_debeRetornarFalse_cuandoTokenExpirado`
  - [ ] `extractUsername_debeRetornarUsernameCorrectamente`

---

## 8. Infrastructure — Controllers REST

- [ ] 8.1 Implementar `AuthController`:
  - [ ] `POST /api/auth/login` → delegar a `LoginUseCase`
- [ ] 8.2 Implementar `ProductoController` (inyectar cada use case por separado — ISP):
  - [ ] `GET /api/productos` → `ListarProductosUseCase`
  - [ ] `GET /api/productos/{id}` → `ObtenerProductoUseCase`
  - [ ] `POST /api/productos` → `CrearProductoUseCase`
  - [ ] `PUT /api/productos/{id}` → `ActualizarProductoUseCase`
  - [ ] `DELETE /api/productos/{id}` → `EliminarProductoUseCase`
- [ ] 8.3 Actualizar `VentaController` con nuevos endpoints:
  - [ ] `POST /api/ventas` → `RegistrarVentaUseCase` (actualizar validaciones para múltiples pagos y nuevos campos)
  - [ ] `GET /api/ventas` → `ListarVentasUseCase` con parámetros de paginación y filtros (fecha, cajeroId, cedulaCliente, metodoPago, page, size)
  - [ ] `GET /api/ventas/{id}` → `ObtenerVentaUseCase`
  - [ ] `GET /api/ventas/factura/{numeroFactura}` → `ObtenerVentaPorFacturaUseCase`
  - [ ] `POST /api/ventas/{id}/reembolso` → `ReembolsarVentaUseCase` (NUEVO)
  - _Requerimientos: RF-06.1, RF-06.2, RF-06.4, RF-08.1_
- [ ] 8.4 Crear `ConfiguracionController` (NUEVO):
  - [ ] `GET /api/configuracion/tasa-impuesto` → `ObtenerTasaImpuestoUseCase`
  - [ ] `PUT /api/configuracion/tasa-impuesto` → `ActualizarTasaImpuestoUseCase`
  - _Requerimientos: RF-09.3, RF-09.4_
- [ ] 8.5 Actualizar `GlobalExceptionHandler` (`@RestControllerAdvice`):
  - [ ] `RecursoNoEncontradoException` → HTTP 404
  - [ ] `StockInsuficienteException` → HTTP 400
  - [ ] `FacturaDuplicadaException` → HTTP 409
  - [ ] `PagosInvalidosException` → HTTP 400 (NUEVO)
  - [ ] `VentaYaReembolsadaException` → HTTP 400 (NUEVO)
  - [ ] `LimiteFacturasDiarioExcedidoException` → HTTP 400 (NUEVO)
  - [ ] `MethodArgumentNotValidException` → HTTP 400 con detalle de campos
  - [ ] `Exception` genérica → HTTP 500
  - _Requerimientos: RF-05.4, RF-05.8_

**Tests de controllers (`@WebMvcTest` + `MockMvc`):**
- [ ] 8.6 `ProductoControllerTest`:
  - [ ] `listar_debeRetornar200_conListaDeProductos`
  - [ ] `crear_debeRetornar201_cuandoDatosValidos`
  - [ ] `crear_debeRetornar400_cuandoDatosInvalidos`
  - [ ] `obtener_debeRetornar404_cuandoProductoNoExiste`
- [ ] 8.7 `VentaControllerTest`:
  - [ ] `registrar_debeRetornar201_cuandoVentaExitosa_conTodosLosCamposDeFacturacion`
  - [ ] `registrar_debeRetornar400_cuandoStockInsuficiente`
  - [ ] `registrar_debeRetornar400_cuandoFaltaNombreCliente`
  - [ ] `registrar_debeRetornar400_cuandoFaltaCedulaCliente`
  - [ ] `registrar_debeRetornar400_cuandoCedulaNoTiene10Digitos`
  - [ ] `registrar_debeRetornar400_cuandoNombreContieneCaracteresInvalidos`
  - [ ] `registrar_debeRetornar400_cuandoNombreNoTieneMinimo2Palabras`
  - [ ] `registrar_debeRetornar400_cuandoMetodoPagoInvalido`
  - [ ] `registrar_debeRetornar400_cuandoSumaDePagosNoCoincideConTotal`
  - [ ] `registrar_debeRetornar400_cuandoListaDePagosVacia`
  - [ ] `registrar_debeRetornar401_sinToken`
  - [ ] `obtenerPorId_debeRetornar200_cuandoVentaExiste`
  - [ ] `obtenerPorId_debeRetornar404_cuandoVentaNoExiste`
  - [ ] `obtenerPorNumeroFactura_debeRetornar200_cuandoFacturaExiste`
  - [ ] `obtenerPorNumeroFactura_debeRetornar404_cuandoFacturaNoExiste`
  - [ ] `listar_debeRetornar200_conVentasPaginadas`
  - [ ] `listar_debeRetornar200_conVentasFiltradasPorFecha`
  - [ ] `listar_debeRetornar200_conVentasFiltradasPorCajero`
  - [ ] `listar_debeRetornar200_conVentasFiltradasPorCedulaCliente`
  - [ ] `listar_debeRetornar200_conVentasFiltradasPorMetodoPago`
  - [ ] `reembolsar_debeRetornar200_cuandoReembolsoExitoso`
  - [ ] `reembolsar_debeRetornar400_cuandoVentaYaReembolsada`
  - [ ] `reembolsar_debeRetornar400_cuandoMotivoMuyCorto`
  - [ ] `reembolsar_debeRetornar404_cuandoVentaNoExiste`
- [ ] 8.8 `ConfiguracionControllerTest`:
  - [ ] `obtenerTasaImpuesto_debeRetornar200_conTasaActual`
  - [ ] `actualizarTasaImpuesto_debeRetornar200_cuandoTasaValida`
  - [ ] `actualizarTasaImpuesto_debeRetornar400_cuandoTasaNegativa`
  - [ ] `actualizarTasaImpuesto_debeRetornar400_cuandoTasaMayorA1`

---

## 9. Validaciones y Manejo de Errores

- [ ] 9.1 Agregar `@Valid` en todos los endpoints que reciben body
- [ ] 9.2 Actualizar Commands con Bean Validation:
  - [ ] `@NotBlank` en nombre, username, password
  - [ ] `@DecimalMin("0.01")` en precio
  - [ ] `@Min(0)` en stock y cantidad
  - [ ] `@NotBlank`, `@Size(min=3, max=50)`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")` en `nombreCliente`
  - [ ] `@NotBlank`, `@Pattern(regexp = "^\\d{10}$")` en `cedulaCliente`
  - [ ] `@NotBlank`, `@Pattern(regexp = "^(EFECTIVO|TARJETA|TRANSFERENCIA)$")` en `metodoPago` de `PagoCommand`
  - [ ] `@NotNull`, `@DecimalMin("0.01")` en `monto` de `PagoCommand`
  - [ ] `@NotEmpty`, `@Valid` en `pagos` de `RegistrarVentaCommand`
  - [ ] `@NotBlank`, `@Size(min=10, max=500)` en `motivo` de `ReembolsarVentaCommand`
  - [ ] `@NotNull`, `@DecimalMin("0.0")`, `@DecimalMax("1.0")` en `tasaImpuesto` de `ActualizarTasaImpuestoCommand`
  - _Requerimientos: RF-03.2, RF-03.3, RF-03.4, RF-08.2, RF-09.5, RF-10.1, RF-10.2, RF-10.3, RF-10.6, RF-10.7_
- [ ] 9.3 Agregar validación adicional en `VentaService` para nombre del cliente:
  - [ ] Validar que el nombre contenga al menos 2 palabras (nombre y apellido)
  - [ ] Lanzar `ValidationException` si no cumple
  - _Requerimientos: RF-10.4_
- [ ] 9.4 Verificar que `GlobalExceptionHandler` retorna formato de error consistente
- [ ] 9.5 Verificar atomicidad de `registrarVenta` con `@Transactional(isolation = Isolation.REPEATABLE_READ)` (rollback si falla stock, generación de factura o validación de pagos)
- [ ] 9.6 Verificar atomicidad de `reembolsar` con `@Transactional(isolation = Isolation.REPEATABLE_READ)` (rollback si falla devolución de stock o creación de reembolso)

### 9.7 Clarificaciones de Ambigüedades (v3.2.0)

- [ ] 9.7.1 **RBAC Centralizado**: Implementar módulo de autorización centralizado con rol ADMIN único
  - [ ] Crear `AuthorizationService` con método `verificarPermisoAdmin(Usuario usuario)`
  - [ ] Evitar lógica de autorización hardcodeada en controllers o services
  - [ ] Diseñar con interfaces para facilitar adición de roles futuros
  - _Requerimientos: RF-01B.1, RF-01B.4, RF-01B.5, RN-31, RN-32_

- [ ] 9.7.2 **Normalización de Nombre del Cliente**: Implementar en `VentaService.registrar()`
  - [ ] Aplicar `trim()` al nombre antes de validar
  - [ ] Normalizar múltiples espacios consecutivos a un solo espacio usando `replaceAll("\\s+", " ")`
  - [ ] Validar que contenga mínimo 2 palabras después de normalización
  - _Requerimientos: RF-03.2.3, RF-03.2.4, RF-03.2.5, RF-10.4, RF-10.5, RF-10.6, RN-34_

- [ ] 9.7.3 **Precisión BigDecimal en Validación de Pagos**: Actualizar `Venta.validarPagos()`
  - [ ] Redondear `total` a 2 decimales con `ROUND_HALF_UP` antes de comparar
  - [ ] Redondear `sumaPagos` a 2 decimales con `ROUND_HALF_UP` antes de comparar
  - [ ] Comparar usando `totalRedondeado.compareTo(sumaPagosRedondeada) == 0`
  - [ ] NO usar tolerancias arbitrarias (ej: ±0.01)
  - _Requerimientos: RF-03.5.1, RF-03.5.2, RF-03.5.3, RF-10.11, RF-10.12, RF-10.13, RN-35, RN-36_

- [ ] 9.7.4 **HTTP 409 para Límite de Facturas**: Actualizar `SecuenciaFacturaService`
  - [ ] Lanzar `LimiteFacturasDiarioExcedidoException` cuando `siguienteNumero > 999999`
  - [ ] Mapear excepción a HTTP 409 CONFLICT en `GlobalExceptionHandler`
  - [ ] Mensaje: "Se alcanzó el límite de 999,999 facturas para el período actual. Contacte al administrador."
  - [ ] Agregar logging cuando se alcance el 90% del límite (899,999 facturas)
  - _Requerimientos: RF-05.8, RF-05.9, RF-05.10, RN-37_

- [ ] 9.7.5 **Filtro por Método de Pago (ANY match)**: Actualizar `VentaRepositoryAdapter`
  - [ ] Implementar query que retorne ventas con AL MENOS un pago del método especificado
  - [ ] Usar JOIN con `pago_venta` y filtrar por `metodo_pago`
  - [ ] Ejemplo: Venta con EFECTIVO + TARJETA aparece en ambos filtros
  - _Requerimientos: RF-06.4.4, RN-33_

- [ ] 9.7.6 **Filtros de Fecha ISO-8601**: Actualizar `VentaService.listar()`
  - [ ] Validar formato ISO-8601 (YYYY-MM-DD) en `FiltroVentasCommand`
  - [ ] Normalizar fecha a rango completo: 00:00:00 a 23:59:59
  - [ ] Convertir a UTC en backend para evitar problemas de zona horaria
  - _Requerimientos: RF-06.4.1, RF-06.4.2, RF-06.4.3, RN-41, RN-42_

- [ ] 9.7.7 **Paginación sin HTTP 404**: Actualizar `VentaService.listar()`
  - [ ] Si página solicitada no existe, retornar lista vacía (no lanzar excepción)
  - [ ] Incluir metadata de paginación: `totalPages`, `totalItems`, `currentPage`, `pageSize`
  - [ ] Mantener consistencia en respuestas incluso sin datos
  - _Requerimientos: RF-06.7, RF-06.8, RN-43, RN-44_

- [ ] 9.7.8 **Reembolso de Productos Inactivos**: Actualizar `VentaService.reembolsar()`
  - [ ] Permitir reembolso aunque el producto esté marcado como `activo = false`
  - [ ] Validar que el producto exista en BD (no eliminado físicamente)
  - [ ] Incrementar stock aunque el producto esté inactivo
  - [ ] Separar estado del producto de su existencia histórica en ventas
  - _Requerimientos: RF-08.6, RF-08.7, RF-08.8, RN-45, RN-46_

- [ ] 9.7.9 **Concurrencia en Actualización de Tasa de Impuesto**: Actualizar `ConfiguracionService`
  - [ ] Anotar `actualizarTasaImpuesto()` con `@Transactional(isolation = Isolation.SERIALIZABLE)`
  - [ ] Alternativamente, usar SELECT FOR UPDATE en `ConfiguracionRepositoryPort.findByClave()`
  - [ ] Preferir locking explícito sobre solo aislamiento transaccional
  - [ ] Garantizar actualizaciones secuenciales sin condiciones de carrera
  - _Requerimientos: RF-09.8, RF-09.9, RF-09.10, RN-40_

- [ ] 9.7.10 **Solo Reembolsos Totales**: Actualizar `VentaService.reembolsar()`
  - [ ] Validar que el reembolso sea de la venta completa (no parcial)
  - [ ] Devolver stock de TODOS los productos de la venta
  - [ ] Preparar modelo de datos para reembolsos parciales futuros (comentarios en código)
  - _Requerimientos: RF-08.4, RF-08.14, RN-38, RN-39_

---

## 10. Testing — Cobertura y Calidad

- [x] 10.1 Configurar JaCoCo en `pom.xml` para reporte de cobertura
- [x] 10.2 Establecer umbral mínimo de cobertura del 80% en `application/service`
- [x] 10.3 Ejecutar todos los tests unitarios: `mvn test`
- [x] 10.4 Verificar que ningún test unitario levanta contexto de Spring (`@SpringBootTest`)
- [ ] 10.5 Escribir tests de integración con `@SpringBootTest` + H2 para flujos completos:
  - [ ] Test de flujo completo: login → crear productos → registrar venta con múltiples pagos → consultar venta por ID
  - [ ] Test de flujo completo: registrar venta → consultar por número de factura → verificar formato FAC-YYYYMMDD-NNNNNN (6 dígitos)
  - [ ] Test de secuencia de facturas: registrar múltiples ventas en el mismo día y verificar secuencia incremental (000001, 000002, 000003...)
  - [ ] Test de reinicio de secuencia: simular cambio de día y verificar que secuencia se reinicia a 000001
  - [ ] Test de límite de facturas: simular 999,999 facturas en un día y verificar que la siguiente lanza `LimiteFacturasDiarioExcedidoException`
  - [ ] Test de múltiples pagos: registrar venta con 3 métodos de pago diferentes y verificar que se persisten correctamente
  - [ ] Test de validación de pagos: intentar registrar venta con suma de pagos diferente al total y verificar que lanza `PagosInvalidosException`
  - [ ] Test de reembolso: registrar venta → reembolsar → verificar que stock se devuelve y venta se marca como reembolsada
  - [ ] Test de reembolso duplicado: intentar reembolsar una venta ya reembolsada y verificar que lanza `VentaYaReembolsadaException`
  - [ ] Test de tasa de impuesto: actualizar tasa → registrar venta → verificar que usa la nueva tasa
  - [ ] Test de paginación: crear 50 ventas → listar con page=0, size=20 → verificar que retorna 20 ventas
  - [ ] Test de filtros: crear ventas con diferentes cajeros, clientes y métodos de pago → filtrar por cada criterio → verificar resultados
  - [ ] Test de redondeo: registrar venta con precios que requieren redondeo → verificar que subtotales, impuesto y total están correctamente redondeados con ROUND_HALF_UP
  - [ ] Test de validación de cédula: intentar registrar venta con cédula de 9 dígitos, 11 dígitos, con letras → verificar que lanza validación
  - [ ] Test de validación de nombre: intentar registrar venta con nombre de 1 palabra, con números, con caracteres especiales → verificar que lanza validación
- [ ] 10.6 Verificar reporte de cobertura con `mvn verify`

---

## 11. Despliegue

- [x] 11.1 Crear `application-prod.properties` con configuración de producción
- [x] 11.2 Externalizar variables sensibles como variables de entorno (`DB_PASSWORD`, `JWT_SECRET`)
- [ ] 11.3 Generar JAR con `mvn clean package -DskipTests`
- [ ] 11.4 Verificar inicio correcto con `java -jar pos-backend.jar` (Java 21)
- [x] 11.5 Crear `Dockerfile` básico (multi-stage build con imagen `eclipse-temurin:21-jre`)
- [x] 11.6 Documentar pasos de despliegue y variables de entorno en `README.md`
- [ ] 11.7 Verificar Swagger UI disponible en `/swagger-ui.html` en entorno de desarrollo

---

## Task Dependency Graph

```json
{
  "waves": [
    {
      "id": 0,
      "tasks": ["2.1", "2.2", "2.3"]
    },
    {
      "id": 1,
      "tasks": ["2.4", "3.2", "3.4", "3.5", "3.6", "3.10", "3.11", "3.12", "3.13", "3.26"]
    },
    {
      "id": 2,
      "tasks": ["2.5", "2.6", "3.14", "3.15", "3.16", "3.17", "3.18", "3.19", "3.20", "3.21", "3.22", "3.23", "3.24", "3.25", "3.27", "3.28", "3.29", "3.30", "4.2", "4.3", "4.5", "4.6"]
    },
    {
      "id": 3,
      "tasks": ["5.2", "5.3", "5.4", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7", "6.8", "6.9", "6.10", "6.12", "6.13", "6.14", "6.15"]
    },
    {
      "id": 4,
      "tasks": ["5.6", "5.7", "5.8", "5.9", "6.16", "6.17", "6.18", "6.19", "6.20", "6.21", "6.22"]
    },
    {
      "id": 5,
      "tasks": ["8.3", "8.4", "8.5", "9.2", "9.3", "9.4", "9.5", "9.6"]
    },
    {
      "id": 6,
      "tasks": ["8.7", "8.8", "10.5"]
    },
    {
      "id": 7,
      "tasks": ["10.6"]
    }
  ]
}
```
