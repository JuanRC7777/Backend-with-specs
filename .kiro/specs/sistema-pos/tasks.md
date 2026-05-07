# Tasks - Sistema POS
**Proyecto:** Point of Sale - Spring Boot + Arquitectura Hexagonal  
**Versión:** 2.0.0  

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
- [x] 2.4 Crear entidades JPA en `infrastructure/adapter/out/persistence/entity/`:
  - [ ] `UsuarioEntity.java`
  - [ ] `ProductoEntity.java`
  - [ ] `VentaEntity.java`
  - [ ] `DetalleVentaEntity.java`
- [x] 2.5 Crear `JpaProductoRepository`, `JpaVentaRepository`, `JpaUsuarioRepository` (Spring Data JPA)
- [x] 2.6 Crear script `data.sql` con usuario admin inicial (contraseña en BCrypt)
- [x] 2.7 Configurar `application-test.properties` con H2 en memoria para tests

---

## 3. Dominio — Modelos y Excepciones

- [x] 3.1 Crear clase `Producto` (POJO puro, sin anotaciones JPA):
  - [ ] Campos: `id`, `nombre`, `descripcion`, `precio`, `stock`, `activo`
  - [ ] Método `tieneStockSuficiente(int cantidad): boolean`
  - [ ] Método `descontarStock(int cantidad)` — lanza `StockInsuficienteException` si no hay stock
- [x] 3.2 Crear clase `Venta` con método `calcularTotal()` que suma subtotales de detalles
- [x] 3.3 Crear clase `DetalleVenta` con método `calcularSubtotal()` (precio × cantidad)
- [x] 3.4 Crear clase `Usuario` (POJO puro)
- [x] 3.5 Crear `StockInsuficienteException` (RuntimeException) con mensaje descriptivo
- [x] 3.6 Crear `RecursoNoEncontradoException` (RuntimeException)

**Tests unitarios del dominio:**
- [ ] 3.7 `ProductoTest`: verificar `descontarStock` con stock suficiente
- [ ] 3.8 `ProductoTest`: verificar que lanza `StockInsuficienteException` con stock insuficiente
- [ ] 3.9 `ProductoTest`: verificar `tieneStockSuficiente` retorna false cuando stock < cantidad
- [ ] 3.10 `VentaTest`: verificar `calcularTotal` suma correctamente todos los subtotales
- [ ] 3.11 `DetalleVentaTest`: verificar `calcularSubtotal` = precio × cantidad

---

## 4. Application — Ports (Interfaces)

- [x] 4.1 Crear Input Ports en `application/port/in/producto/`:
  - [ ] `CrearProductoUseCase` — método `crear(CrearProductoCommand): ProductoResponse`
  - [ ] `ListarProductosUseCase` — método `listar(): List<ProductoResponse>`
  - [ ] `ObtenerProductoUseCase` — método `obtener(Long id): ProductoResponse`
  - [ ] `ActualizarProductoUseCase` — método `actualizar(Long id, ActualizarProductoCommand): ProductoResponse`
  - [ ] `EliminarProductoUseCase` — método `eliminar(Long id): void`
- [x] 4.2 Crear Input Ports en `application/port/in/venta/`:
  - [ ] `RegistrarVentaUseCase` — método `registrar(RegistrarVentaCommand): VentaResponse`
  - [ ] `ListarVentasUseCase` — método `listar(): List<VentaResponse>`
- [x] 4.3 Crear Input Port `LoginUseCase` — método `login(LoginCommand): LoginResponse`
- [x] 4.4 Crear Output Ports en `application/port/out/`:
  - [ ] `ProductoRepositoryPort` — `findById`, `findAllActivos`, `save`, `deleteById`
  - [ ] `VentaRepositoryPort` — `save`, `findById`, `findAll`
  - [ ] `UsuarioRepositoryPort` — `findByUsername`
- [x] 4.5 Crear Commands y Responses en `application/dto/`:
  - [ ] `CrearProductoCommand`, `ActualizarProductoCommand`, `ProductoResponse`
  - [ ] `RegistrarVentaCommand`, `ItemVentaCommand`, `VentaResponse`, `DetalleVentaResponse`
  - [ ] `LoginCommand`, `LoginResponse`

---

## 5. Application — Servicios (Casos de Uso)

- [x] 5.1 Implementar `ProductoService` (implementa los 5 use cases de producto):
  - [ ] `crear`: validar datos, mapear a dominio, persistir via port
  - [ ] `listar`: obtener todos los activos via port
  - [ ] `obtener`: buscar por ID, lanzar `RecursoNoEncontradoException` si no existe
  - [ ] `actualizar`: buscar, actualizar campos, persistir
  - [ ] `eliminar`: eliminación lógica (`activo = false`)
- [x] 5.2 Implementar `VentaService` (implementa `RegistrarVentaUseCase`, `ListarVentasUseCase`):
  - [ ] `registrar`: obtener productos, validar stock, calcular subtotales y total, persistir, descontar stock
  - [ ] Anotar con `@Transactional` para garantizar atomicidad
- [x] 5.3 Implementar `AuthService` (implementa `LoginUseCase`):
  - [ ] Cargar usuario via `UsuarioRepositoryPort`
  - [ ] Validar contraseña con `PasswordEncoder`
  - [ ] Generar y retornar token JWT

**Tests unitarios de servicios (con Mockito):**
- [ ] 5.4 `ProductoServiceTest`:
  - [ ] `crear_debeRetornarProductoCreado_cuandoDatosValidos`
  - [ ] `obtener_debeLanzarExcepcion_cuandoProductoNoExiste`
  - [ ] `eliminar_debeMarcarInactivo_cuandoProductoExiste`
- [ ] 5.5 `VentaServiceTest`:
  - [ ] `registrar_debeCalcularTotalCorrectamente_conMultiplesProductos`
  - [ ] `registrar_debeDescontarStock_alConfirmarVenta`
  - [ ] `registrar_debeLanzarStockInsuficienteException_cuandoStockEsInsuficiente`
  - [ ] `registrar_noDebeDescontarStock_siAlgunProductoFalla` (atomicidad)
- [ ] 5.6 `AuthServiceTest`:
  - [ ] `login_debeRetornarToken_cuandoCredencialesValidas`
  - [ ] `login_debeLanzarExcepcion_cuandoPasswordIncorrecto`

---

## 6. Infrastructure — Adapters de Persistencia

- [ ] 6.1 Crear `ProductoMapper` para convertir entre `Producto` (dominio) y `ProductoEntity` (JPA)
- [ ] 6.2 Crear `VentaMapper` para convertir entre `Venta` y `VentaEntity`
- [ ] 6.3 Implementar `ProductoRepositoryAdapter` (implementa `ProductoRepositoryPort`):
  - [ ] Delegar a `JpaProductoRepository`
  - [ ] Usar `ProductoMapper` para conversión
- [ ] 6.4 Implementar `VentaRepositoryAdapter` (implementa `VentaRepositoryPort`)
- [ ] 6.5 Implementar `UsuarioRepositoryAdapter` (implementa `UsuarioRepositoryPort`)

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
- [ ] 8.3 Implementar `VentaController`:
  - [ ] `POST /api/ventas` → `RegistrarVentaUseCase`
  - [ ] `GET /api/ventas` → `ListarVentasUseCase`
- [ ] 8.4 Crear `GlobalExceptionHandler` (`@RestControllerAdvice`):
  - [ ] `RecursoNoEncontradoException` → HTTP 404
  - [ ] `StockInsuficienteException` → HTTP 400
  - [ ] `MethodArgumentNotValidException` → HTTP 400 con detalle de campos
  - [ ] `Exception` genérica → HTTP 500

**Tests de controllers (`@WebMvcTest` + `MockMvc`):**
- [ ] 8.5 `ProductoControllerTest`:
  - [ ] `listar_debeRetornar200_conListaDeProductos`
  - [ ] `crear_debeRetornar201_cuandoDatosValidos`
  - [ ] `crear_debeRetornar400_cuandoDatosInvalidos`
  - [ ] `obtener_debeRetornar404_cuandoProductoNoExiste`
- [ ] 8.6 `VentaControllerTest`:
  - [ ] `registrar_debeRetornar201_cuandoVentaExitosa`
  - [ ] `registrar_debeRetornar400_cuandoStockInsuficiente`
  - [ ] `registrar_debeRetornar401_sinToken`

---

## 9. Validaciones y Manejo de Errores

- [ ] 9.1 Agregar `@Valid` en todos los endpoints que reciben body
- [ ] 9.2 Anotar Commands con Bean Validation:
  - [ ] `@NotBlank` en nombre, username, password
  - [ ] `@DecimalMin("0.01")` en precio
  - [ ] `@Min(0)` en stock y cantidad
- [ ] 9.3 Verificar que `GlobalExceptionHandler` retorna formato de error consistente
- [ ] 9.4 Verificar atomicidad de `registrarVenta` con `@Transactional` (rollback si falla stock)

---

## 10. Testing — Cobertura y Calidad

- [x] 10.1 Configurar JaCoCo en `pom.xml` para reporte de cobertura
- [x] 10.2 Establecer umbral mínimo de cobertura del 80% en `application/service`
- [x] 10.3 Ejecutar todos los tests unitarios: `mvn test`
- [x] 10.4 Verificar que ningún test unitario levanta contexto de Spring (`@SpringBootTest`)
- [ ] 10.5 Escribir tests de integración con `@SpringBootTest` + H2 para flujo completo de venta
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
