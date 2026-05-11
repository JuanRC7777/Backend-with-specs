# Documento de Diseño Técnico
# Sistema POS - Point of Sale con Facturación
**Versión:** 3.2.0  
**Fecha:** 2026-05-08  
**Arquitectura:** Hexagonal (Ports & Adapters)  
**Cambios Significativos:** Clarificaciones de ambigüedades: RBAC centralizado, validación de nombres con normalización, precisión BigDecimal, límite de facturas con HTTP 409, reembolsos totales, concurrencia SERIALIZABLE, filtros ISO-8601, paginación sin 404, reembolso de productos inactivos

---

## Registro de Cambios

### Versión 3.2.0 (2026-05-08)
**Clarificaciones incorporadas desde requirements.md v3.2.0:**

1. **Permisos y Roles de Usuario**: Especificado rol único ADMIN para MVP con diseño RBAC centralizado en módulo de autorización
2. **Filtro por Método de Pago**: Implementación de ANY match para ventas con múltiples pagos (retorna ventas que contengan AL MENOS un pago con ese método)
3. **Validación de Nombre del Cliente**: Algoritmo de normalización con trim() y conversión de múltiples espacios a uno solo antes de validar mínimo 2 palabras
4. **Precisión de Suma de Pagos**: Uso obligatorio de BigDecimal.compareTo() == 0 después de redondear con ROUND_HALF_UP, sin tolerancias arbitrarias
5. **Límite de 999,999 Facturas**: Manejo de error con HTTP 409 CONFLICT y mensaje específico al alcanzar límite diario
6. **Reembolso Parcial vs Total**: Solo reembolsos totales permitidos en esta versión, modelo de datos preparado para parciales futuros
7. **Concurrencia en Actualización de Tasa de Impuesto**: Implementación de bloqueo explícito con SELECT FOR UPDATE y aislamiento SERIALIZABLE
8. **Formato de Fecha en Filtros**: Validación ISO-8601 obligatoria (YYYY-MM-DD) con normalización de zona horaria en backend
9. **Paginación sin Resultados**: Retorno de lista vacía con metadata completa en lugar de HTTP 404
10. **Validación de Stock en Reembolso**: Permitir reembolso de productos inactivos si existen históricamente, separando estado activo/inactivo de existencia histórica

---

## 1. Arquitectura Hexagonal

La arquitectura hexagonal (Ports & Adapters) aísla el dominio de negocio de los detalles de infraestructura. El núcleo del sistema no conoce ni depende de Spring, JPA ni HTTP.

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRAESTRUCTURA                           │
│                                                             │
│  ┌──────────────┐          ┌──────────────────────────────┐ │
│  │  REST API    │          │  Persistencia (JPA/MySQL)    │ │
│  │ (Adapters    │          │  (Adapters Secundarios /     │ │
│  │  Primarios)  │          │   Driven Adapters)           │ │
│  └──────┬───────┘          └──────────────┬───────────────┘ │
│         │ Input Ports                     │ Output Ports     │
│  ───────▼─────────────────────────────────▼──────────────── │
│  │                   DOMINIO (Hexágono)                    │ │
│  │                                                         │ │
│  │   ┌─────────────────┐    ┌──────────────────────────┐  │ │
│  │   │  Use Cases      │    │  Domain Model            │  │ │
│  │   │  (Application   │    │  Producto, Venta,        │  │ │
│  │   │   Services)     │    │  Usuario, DetalleVenta   │  │ │
│  │   └─────────────────┘    └──────────────────────────┘  │ │
│  │                                                         │ │
│  │   ┌─────────────────────────────────────────────────┐  │ │
│  │   │  Ports (Interfaces)                             │  │ │
│  │   │  ProductoRepository, VentaRepository, etc.      │  │ │
│  │   └─────────────────────────────────────────────────┘  │ │
│  ───────────────────────────────────────────────────────── │ │
└─────────────────────────────────────────────────────────────┘
```

### Capas y responsabilidades

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Domain** | `domain/model` | Entidades de negocio puras, sin anotaciones de framework |
| **Domain** | `domain/exception` | Excepciones de negocio (`StockInsuficienteException`) |
| **Application** | `application/port/in` | Input Ports: interfaces de casos de uso |
| **Application** | `application/port/out` | Output Ports: interfaces de repositorios |
| **Application** | `application/service` | Implementación de casos de uso (Use Cases) |
| **Infrastructure** | `infrastructure/adapter/in/web` | Controllers REST (Adapters primarios) |
| **Infrastructure** | `infrastructure/adapter/out/persistence` | Repositorios JPA (Adapters secundarios) |
| **Infrastructure** | `infrastructure/adapter/out/security` | JWT, Spring Security |
| **Infrastructure** | `infrastructure/config` | Beans de Spring, configuración |

---

## 2. Principios SOLID Aplicados

### S — Single Responsibility
- Cada clase tiene una única razón para cambiar.
- `RegistrarVentaUseCase` solo orquesta el registro de ventas.
- `StockValidator` solo valida disponibilidad de stock.
- `TotalCalculator` solo calcula totales de venta.

### O — Open/Closed
- Los casos de uso dependen de interfaces (ports), no de implementaciones concretas.
- Agregar un nuevo adaptador de persistencia (ej. MongoDB) no modifica el dominio.

### L — Liskov Substitution
- Cualquier implementación de `ProductoRepositoryPort` puede sustituirse sin romper los casos de uso.

### I — Interface Segregation
- Los ports están segregados por responsabilidad:
  - `CrearProductoUseCase`, `ListarProductosUseCase`, `EliminarProductoUseCase` son interfaces separadas.
  - Los controllers solo dependen del port que necesitan.

### D — Dependency Inversion
- Los casos de uso dependen de abstracciones (`ProductoRepositoryPort`), no de `JpaProductoRepository`.
- Spring inyecta las implementaciones concretas en tiempo de ejecución.

---

## 2.1. Autorización y Control de Acceso (RBAC)

### Diseño Centralizado

El sistema implementa Role-Based Access Control (RBAC) con un módulo centralizado de autorización que evita lógica hardcodeada por pantalla o endpoint.

```java
// infrastructure/adapter/out/security/AuthorizationService.java
@Service
public class AuthorizationService {
    
    private static final String ADMIN_ROLE = "ADMIN";
    
    public boolean hasRole(String username, String requiredRole) {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        return usuario.getRol().equals(requiredRole);
    }
    
    public void requireAdminRole(String username) {
        if (!hasRole(username, ADMIN_ROLE)) {
            throw new AccesoDenegadoException(
                "Se requiere rol ADMIN para esta operación"
            );
        }
    }
}
```

### Rol ADMIN (MVP)

En la fase inicial (MVP), el sistema soporta un único rol: **ADMIN** con acceso total a todas las funcionalidades:

- Gestionar productos (CRUD completo)
- Realizar ventas
- Realizar reembolsos
- Modificar configuración del sistema (tasa de impuesto)
- Gestionar inventario
- Acceder a reportes y consultas

```java
// domain/model/Usuario.java
public class Usuario {
    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String rol;  // "ADMIN" en MVP
    private boolean activo;
    
    public boolean isAdmin() {
        return "ADMIN".equals(this.rol);
    }
}
```

### Aplicación en Casos de Uso

```java
// application/service/VentaService.java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public VentaResponse registrar(RegistrarVentaCommand cmd) {
    // Obtener usuario autenticado
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    
    // Validar autorización centralizada
    authorizationService.requireAdminRole(username);
    
    // Continuar con el registro de venta...
}

// application/service/ConfiguracionService.java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void actualizarTasaImpuesto(BigDecimal nuevaTasa) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    
    // Validar autorización centralizada
    authorizationService.requireAdminRole(username);
    
    // Continuar con la actualización...
}
```

### Preparación para Roles Futuros

El diseño permite agregar roles adicionales sin modificar la arquitectura base:

```java
// Ejemplo de roles futuros (no implementados en MVP)
public enum Rol {
    ADMIN,      // Acceso total (MVP)
    CAJERO,     // Solo ventas y consultas (futuro)
    SUPERVISOR, // Ventas, reembolsos y reportes (futuro)
    AUDITOR     // Solo lectura (futuro)
}

// Ejemplo de permisos granulares (futuro)
public enum Permiso {
    CREAR_PRODUCTO,
    EDITAR_PRODUCTO,
    ELIMINAR_PRODUCTO,
    REALIZAR_VENTA,
    REALIZAR_REEMBOLSO,
    MODIFICAR_CONFIGURACION,
    VER_REPORTES
}
```

### Configuración de Spring Security

```java
// infrastructure/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/**").hasRole("ADMIN")  // MVP: solo ADMIN
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 3. Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 LTS | Lenguaje principal |
| Spring Boot | 3.x | Framework de infraestructura |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Adapter de persistencia |
| JWT (jjwt) | 0.12.x | Generación y validación de tokens |
| MySQL / MariaDB | 8.x / 10.x | Base de datos relacional |
| JUnit 5 | 5.x | Framework de testing |
| Mockito | 5.x | Mocking en tests unitarios |
| AssertJ | 3.x | Assertions fluidas en tests |
| Maven | 3.x | Build y dependencias |
| Swagger / OpenAPI | 3.x | Documentación de API |

---

## 4. Modelo de Dominio

Las entidades del dominio son POJOs puros, sin anotaciones JPA ni Spring.

```java
// domain/model/Producto.java
public class Producto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private boolean activo;

    public boolean tieneStockSuficiente(int cantidad) {
        return this.stock >= cantidad;
    }

    public void descontarStock(int cantidad) {
        if (!tieneStockSuficiente(cantidad)) {
            throw new StockInsuficienteException(this.id, cantidad, this.stock);
        }
        this.stock -= cantidad;
    }
}
```

```java
// domain/model/Venta.java
public class Venta {
    private Long id;
    private String numeroFactura;        // FAC-YYYYMMDD-NNNNNN (6 dígitos)
    private Long usuarioId;
    private String nombreCajero;         // username del usuario autenticado
    private String nombreCliente;        // obligatorio, validado
    private String cedulaCliente;        // obligatorio, 10 dígitos exactos
    private List<DetalleVenta> detalles;
    private List<Pago> pagos;            // NUEVO: múltiples métodos de pago
    private BigDecimal subtotal;         // total sin impuesto
    private BigDecimal impuesto;         // monto del impuesto
    private BigDecimal tasaImpuesto;     // porcentaje (ej: 0.05 para 5%)
    private BigDecimal total;            // Total final (subtotal + impuesto)
    private LocalDateTime fecha;         // fecha de CONFIRMACIÓN de la venta
    private boolean reembolsada;         // NUEVO: indica si fue reembolsada

    public BigDecimal calcularSubtotal() {
        return detalles.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularImpuesto(BigDecimal subtotal, BigDecimal tasaImpuesto) {
        return subtotal.multiply(tasaImpuesto)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal impuesto) {
        return subtotal.add(impuesto)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public void calcularTotales() {
        this.subtotal = calcularSubtotal();
        this.impuesto = calcularImpuesto(this.subtotal, this.tasaImpuesto);
        this.total = calcularTotal(this.subtotal, this.impuesto);
    }
    
    public void validarPagos() {
        BigDecimal totalPagos = pagos.stream()
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPagos.compareTo(this.total) != 0) {
            throw new PagosInvalidosException(
                "La suma de pagos debe ser igual al total de la venta"
            );
        }
    }
}
```

```java
// domain/model/Pago.java
public class Pago {
    private Long id;
    private Long ventaId;
    private String metodoPago;           // EFECTIVO, TARJETA, TRANSFERENCIA
    private BigDecimal monto;            // monto pagado con este método
    
    public Pago(String metodoPago, BigDecimal monto) {
        this.metodoPago = metodoPago;
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
    }
}
```

```java
// domain/model/Reembolso.java
public class Reembolso {
    private Long id;
    private Long ventaId;
    private String motivo;
    private LocalDateTime fecha;
    private Long usuarioId;              // usuario que autoriza el reembolso
    private String nombreUsuario;
    
    public Reembolso(Long ventaId, String motivo, Long usuarioId, String nombreUsuario) {
        this.ventaId = ventaId;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
    }
}
```

```java
// domain/model/Configuracion.java
public class Configuracion {
    private Long id;
    private String clave;
    private String valor;
    
    public static final String TASA_IMPUESTO_KEY = "tasa_impuesto";
    public static final String TASA_IMPUESTO_DEFAULT = "0.05";  // 5%
    
    public BigDecimal getValorComoDecimal() {
        return new BigDecimal(valor);
    }
}
```

### Modelo de datos (SQL)

```sql
CREATE TABLE usuario (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    rol        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

CREATE TABLE producto (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)   NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    activo      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (activo)
);

CREATE TABLE venta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_factura  VARCHAR(50)    NOT NULL UNIQUE,
    usuario_id      BIGINT         NOT NULL,
    nombre_cajero   VARCHAR(100)   NOT NULL,
    nombre_cliente  VARCHAR(100)   NOT NULL,
    cedula_cliente  VARCHAR(10)    NOT NULL,
    subtotal        DECIMAL(10, 2) NOT NULL,
    tasa_impuesto   DECIMAL(5, 4)  NOT NULL,
    impuesto        DECIMAL(10, 2) NOT NULL,
    total           DECIMAL(10, 2) NOT NULL,
    fecha           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    reembolsada     BOOLEAN        NOT NULL DEFAULT FALSE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha (fecha),
    INDEX idx_cedula_cliente (cedula_cliente),
    INDEX idx_cajero (usuario_id),
    INDEX idx_reembolsada (reembolsada)
);

CREATE TABLE detalle_venta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id    BIGINT         NOT NULL,
    producto_id BIGINT         NOT NULL,
    cantidad    INT            NOT NULL,
    precio_unit DECIMAL(10, 2) NOT NULL,
    subtotal    DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES venta(id),
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    INDEX idx_venta (venta_id)
);

-- NUEVA TABLA: Múltiples métodos de pago por venta
CREATE TABLE pago_venta (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id      BIGINT         NOT NULL,
    metodo_pago   VARCHAR(20)    NOT NULL,
    monto         DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES venta(id),
    INDEX idx_venta (venta_id),
    CONSTRAINT chk_metodo_pago CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    CONSTRAINT chk_monto_positivo CHECK (monto > 0)
);

-- NUEVA TABLA: Sistema de reembolsos
CREATE TABLE reembolso (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id        BIGINT       NOT NULL,
    motivo          TEXT         NOT NULL,
    fecha           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    usuario_id      BIGINT       NOT NULL,
    nombre_usuario  VARCHAR(100) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES venta(id),
    FOREIGN KEY (usuario_id)  REFERENCES usuario(id),
    INDEX idx_venta (venta_id),
    INDEX idx_fecha (fecha),
    CONSTRAINT uq_venta_reembolso UNIQUE (venta_id)
);

-- NUEVA TABLA: Configuración global del sistema
CREATE TABLE configuracion (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave      VARCHAR(100) NOT NULL UNIQUE,
    valor      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_clave (clave)
);

-- Insertar tasa de impuesto predefinida (5%)
INSERT INTO configuracion (clave, valor) VALUES ('tasa_impuesto', '0.05');

-- Tabla para control de secuencia de facturas (formato ampliado a 6 dígitos)
CREATE TABLE secuencia_factura (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha           DATE           NOT NULL UNIQUE,
    ultimo_numero   INT            NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fecha (fecha),
    CONSTRAINT chk_ultimo_numero CHECK (ultimo_numero >= 0 AND ultimo_numero <= 999999)
);
```

### Relaciones

```
Usuario ──< Venta ──< DetalleVenta >── Producto
  1           1:N         N:1            1
  │           │
  │           └──< PagoVenta (1:N)
  │           │
  │           └──< Reembolso (1:1)
  │
  └──< Reembolso (1:N)

Configuracion (tabla global)
```

---

## 5. Ports (Interfaces)

### Input Ports (casos de uso)
```java
// application/port/in/producto/
interface CrearProductoUseCase    { ProductoResponse crear(CrearProductoCommand cmd); }
interface ListarProductosUseCase  { List<ProductoResponse> listar(); }
interface ObtenerProductoUseCase  { ProductoResponse obtener(Long id); }
interface ActualizarProductoUseCase { ProductoResponse actualizar(Long id, ActualizarProductoCommand cmd); }
interface EliminarProductoUseCase { void eliminar(Long id); }

// application/port/in/venta/
interface RegistrarVentaUseCase   { VentaResponse registrar(RegistrarVentaCommand cmd); }
interface ListarVentasUseCase     { Page<VentaResponse> listar(FiltroVentasCommand filtro); }
interface ObtenerVentaUseCase     { VentaResponse obtenerPorId(Long id); }
interface ObtenerVentaPorFacturaUseCase { VentaResponse obtenerPorNumeroFactura(String numeroFactura); }
interface ReembolsarVentaUseCase  { ReembolsoResponse reembolsar(ReembolsarVentaCommand cmd); }

// application/port/in/configuracion/
interface ObtenerTasaImpuestoUseCase { BigDecimal obtenerTasaImpuesto(); }
interface ActualizarTasaImpuestoUseCase { void actualizar(BigDecimal nuevaTasa); }

// application/port/in/auth/
interface LoginUseCase            { LoginResponse login(LoginCommand cmd); }
```

### Output Ports (repositorios)
```java
// application/port/out/
interface ProductoRepositoryPort {
    Optional<Producto> findById(Long id);
    List<Producto> findAllActivos();
    Producto save(Producto producto);
    void deleteById(Long id);
}

interface VentaRepositoryPort {
    Venta save(Venta venta);
    Optional<Venta> findById(Long id);
    Optional<Venta> findByNumeroFactura(String numeroFactura);
    Page<Venta> findAll(FiltroVentas filtro);
    boolean existsByNumeroFactura(String numeroFactura);
}

interface PagoVentaRepositoryPort {
    List<Pago> saveAll(List<Pago> pagos);
    List<Pago> findByVentaId(Long ventaId);
}

interface ReembolsoRepositoryPort {
    Reembolso save(Reembolso reembolso);
    Optional<Reembolso> findByVentaId(Long ventaId);
    boolean existsByVentaId(Long ventaId);
}

interface ConfiguracionRepositoryPort {
    Optional<Configuracion> findByClave(String clave);
    Configuracion save(Configuracion configuracion);
}

interface UsuarioRepositoryPort {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findById(Long id);
}

interface SecuenciaFacturaRepositoryPort {
    int obtenerSiguienteNumero(LocalDate fecha);
    void actualizarSecuencia(LocalDate fecha, int numero);
}
```

---

## 6. Generación de Número de Factura Único

### 6.1 Formato del Número de Factura (ACTUALIZADO)
```
FAC-YYYYMMDD-NNNNNN
```
- **FAC**: Prefijo fijo
- **YYYYMMDD**: Fecha actual (año, mes, día)
- **NNNNNN**: Secuencia numérica de 6 dígitos (000001-999999)

**Cambio importante:** Se amplió de 4 a 6 dígitos para soportar hasta 999,999 ventas por día.

**Ejemplos:**
- `FAC-20260507-000001` (primera factura del 7 de mayo de 2026)
- `FAC-20260507-000002` (segunda factura del mismo día)
- `FAC-20260508-000001` (primera factura del día siguiente)
- `FAC-20260507-123456` (factura 123,456 del mismo día)

### 6.2 Algoritmo de Generación

```java
// domain/service/GeneradorNumeroFactura.java
public class GeneradorNumeroFactura {
    
    public String generar(LocalDate fecha, int secuencia) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String secuenciaStr = String.format("%06d", secuencia);  // 6 dígitos
        return String.format("FAC-%s-%s", fechaStr, secuenciaStr);
    }
    
    public boolean validarFormato(String numeroFactura) {
        return numeroFactura != null && 
               numeroFactura.matches("^FAC-\\d{8}-\\d{6}$");  // 6 dígitos
    }
}
```

### 6.3 Control de Secuencia

El sistema mantiene una tabla `secuencia_factura` que almacena el último número usado por día:

```java
// application/service/SecuenciaFacturaService.java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public String obtenerSiguienteNumeroFactura() {
    LocalDate hoy = LocalDate.now();
    
    // Obtener o crear secuencia del día con SELECT FOR UPDATE
    int siguienteNumero = secuenciaRepository.obtenerSiguienteNumero(hoy);
    
    // Validar límite diario
    if (siguienteNumero > 999999) {
        throw new LimiteFacturasDiarioExcedidoException(
            "Se alcanzó el límite de 999,999 facturas por día"
        );
    }
    
    // Generar número de factura
    String numeroFactura = generadorNumeroFactura.generar(hoy, siguienteNumero);
    
    // Verificar unicidad (por seguridad)
    if (ventaRepository.existsByNumeroFactura(numeroFactura)) {
        throw new FacturaDuplicadaException(numeroFactura);
    }
    
    // Actualizar secuencia
    secuenciaRepository.actualizarSecuencia(hoy, siguienteNumero);
    
    return numeroFactura;
}
```

### 6.4 Garantías de Unicidad

1. **Constraint UNIQUE en BD**: `numero_factura` tiene índice único
2. **Transaccionalidad**: La generación y actualización de secuencia ocurre en una transacción con nivel de aislamiento REPEATABLE_READ
3. **SELECT FOR UPDATE**: Bloquea la fila de secuencia durante la transacción para evitar condiciones de carrera
4. **Verificación doble**: Se valida que no exista antes de persistir
5. **Reinicio diario**: La secuencia se reinicia a 000001 cada día
6. **Límite de seguridad**: Máximo 999,999 facturas por día

---

## 7. DTOs Actualizados

### 7.1 Commands

```java
// application/dto/command/PagoCommand.java
public class PagoCommand {
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "^(EFECTIVO|TARJETA|TRANSFERENCIA)$", 
             message = "Método de pago inválido")
    private String metodoPago;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;
}

// application/dto/command/RegistrarVentaCommand.java
public class RegistrarVentaCommand {
    private Long usuarioId;                    // Se obtiene del JWT
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", 
             message = "El nombre solo puede contener letras, espacios y tildes")
    private String nombreCliente;
    
    @NotBlank(message = "La cédula del cliente es obligatoria")
    @Pattern(regexp = "^\\d{10}$", 
             message = "La cédula debe tener exactamente 10 dígitos numéricos")
    private String cedulaCliente;
    
    // La tasa de impuesto se obtiene de la configuración global
    // No se envía en el request
    
    @NotEmpty(message = "Debe haber al menos un método de pago")
    @Valid
    private List<PagoCommand> pagos;           // NUEVO: múltiples métodos de pago
    
    @NotEmpty(message = "La venta debe tener al menos un item")
    @Valid
    private List<ItemVentaCommand> items;
}

// application/dto/command/ItemVentaCommand.java
public class ItemVentaCommand {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
    
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;
}

// application/dto/command/ReembolsarVentaCommand.java
public class ReembolsarVentaCommand {
    @NotNull(message = "El ID de la venta es obligatorio")
    private Long ventaId;
    
    @NotBlank(message = "El motivo del reembolso es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
    
    private Long usuarioId;                    // Se obtiene del JWT
}

// application/dto/command/FiltroVentasCommand.java
public class FiltroVentasCommand {
    private LocalDate fecha;
    private Long cajeroId;
    private String cedulaCliente;
    private String metodoPago;
    private int page = 0;
    private int size = 20;
}

// application/dto/command/ActualizarTasaImpuestoCommand.java
public class ActualizarTasaImpuestoCommand {
    @NotNull(message = "La tasa de impuesto es obligatoria")
    @DecimalMin(value = "0.0", message = "La tasa de impuesto no puede ser negativa")
    @DecimalMax(value = "1.0", message = "La tasa de impuesto no puede superar 100%")
    private BigDecimal tasaImpuesto;
}
```

### 7.2 Responses

```java
// application/dto/response/PagoResponse.java
public class PagoResponse {
    private Long id;
    private String metodoPago;
    private BigDecimal monto;
}

// application/dto/response/VentaResponse.java
public class VentaResponse {
    private Long id;
    private String numeroFactura;              // FAC-YYYYMMDD-NNNNNN
    private Long usuarioId;
    private String nombreCajero;               // username del usuario autenticado
    private String nombreCliente;
    private String cedulaCliente;
    private List<DetalleVentaResponse> detalles;
    private List<PagoResponse> pagos;          // NUEVO: múltiples métodos de pago
    private BigDecimal subtotal;               // Total sin impuesto
    private BigDecimal tasaImpuesto;           // Porcentaje (0.05)
    private BigDecimal impuesto;               // Monto del impuesto
    private BigDecimal total;                  // Total final
    private LocalDateTime fecha;               // Fecha de CONFIRMACIÓN
    private boolean reembolsada;               // NUEVO: indica si fue reembolsada
    private ReembolsoResponse reembolso;       // NUEVO: datos del reembolso si existe
}

// application/dto/response/DetalleVentaResponse.java
public class DetalleVentaResponse {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;               // cantidad × precioUnitario (redondeado)
}

// application/dto/response/ReembolsoResponse.java
public class ReembolsoResponse {
    private Long id;
    private Long ventaId;
    private String motivo;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String nombreUsuario;
}

// application/dto/response/ConfiguracionResponse.java
public class ConfiguracionResponse {
    private String clave;
    private String valor;
    private BigDecimal valorDecimal;           // Para tasa de impuesto
}
```

### 7.3 Ejemplo de Request: Registrar Venta con Múltiples Pagos

```json
{
  "nombreCliente": "María González Pérez",
  "cedulaCliente": "1234567890",
  "pagos": [
    {
      "metodoPago": "EFECTIVO",
      "monto": 25000.00
    },
    {
      "metodoPago": "TARJETA",
      "monto": 16650.00
    }
  ],
  "items": [
    {
      "productoId": 10,
      "cantidad": 2
    },
    {
      "productoId": 11,
      "cantidad": 1
    }
  ]
}
```

### 7.4 Ejemplo de Response Completo

```json
{
  "success": true,
  "data": {
    "id": 123,
    "numeroFactura": "FAC-20260507-000001",
    "usuarioId": 5,
    "nombreCajero": "jperez",
    "nombreCliente": "María González Pérez",
    "cedulaCliente": "1234567890",
    "detalles": [
      {
        "id": 456,
        "productoId": 10,
        "nombreProducto": "Café Premium 500g",
        "cantidad": 2,
        "precioUnitario": 15000.00,
        "subtotal": 30000.00
      },
      {
        "id": 457,
        "productoId": 11,
        "nombreProducto": "Azúcar 1kg",
        "cantidad": 1,
        "precioUnitario": 5000.00,
        "subtotal": 5000.00
      }
    ],
    "pagos": [
      {
        "id": 789,
        "metodoPago": "EFECTIVO",
        "monto": 25000.00
      },
      {
        "id": 790,
        "metodoPago": "TARJETA",
        "monto": 16650.00
      }
    ],
    "subtotal": 35000.00,
    "tasaImpuesto": 0.05,
    "impuesto": 1750.00,
    "total": 36750.00,
    "fecha": "2026-05-07T14:30:00",
    "reembolsada": false,
    "reembolso": null
  },
  "message": "Venta registrada exitosamente",
  "timestamp": "2026-05-07T14:30:00Z"
}
```

### 7.5 Ejemplo de Response: Venta Reembolsada

```json
{
  "success": true,
  "data": {
    "id": 123,
    "numeroFactura": "FAC-20260507-000001",
    "usuarioId": 5,
    "nombreCajero": "jperez",
    "nombreCliente": "María González Pérez",
    "cedulaCliente": "1234567890",
    "detalles": [...],
    "pagos": [...],
    "subtotal": 35000.00,
    "tasaImpuesto": 0.05,
    "impuesto": 1750.00,
    "total": 36750.00,
    "fecha": "2026-05-07T14:30:00",
    "reembolsada": true,
    "reembolso": {
      "id": 50,
      "ventaId": 123,
      "motivo": "Cliente insatisfecho con la calidad del producto",
      "fecha": "2026-05-07T16:45:00",
      "usuarioId": 3,
      "nombreUsuario": "admin"
    }
  },
  "message": "Venta obtenida exitosamente",
  "timestamp": "2026-05-07T17:00:00Z"
}
```

---

## 8. Endpoints REST

### Autenticación
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Autenticar usuario, retorna JWT | No |

### Productos
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | `/api/productos` | Listar productos activos | JWT |
| GET | `/api/productos/{id}` | Obtener producto por ID | JWT |
| POST | `/api/productos` | Crear nuevo producto | JWT |
| PUT | `/api/productos/{id}` | Actualizar producto | JWT |
| DELETE | `/api/productos/{id}` | Eliminar producto | JWT |

### Ventas (POS)
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | `/api/ventas` | Registrar nueva venta con factura y múltiples pagos | JWT |
| GET | `/api/ventas` | Listar ventas con paginación y filtros | JWT |
| GET | `/api/ventas/{id}` | Obtener detalle de venta por ID | JWT |
| GET | `/api/ventas/factura/{numeroFactura}` | Obtener venta por número de factura | JWT |
| POST | `/api/ventas/{id}/reembolso` | Reembolsar una venta (devuelve stock) | JWT |

### Configuración
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | `/api/configuracion/tasa-impuesto` | Obtener tasa de impuesto global | JWT |
| PUT | `/api/configuracion/tasa-impuesto` | Actualizar tasa de impuesto global | JWT |

### Formato de respuesta estándar
```json
{
  "success": true,
  "data": {},
  "message": "Operación exitosa",
  "timestamp": "2026-04-22T10:00:00Z"
}
```

---

## 9. Estructura de Carpetas (Hexagonal)

```
pos-backend/
├── src/
│   ├── main/java/com/empresa/pos/
│   │   ├── PosApplication.java
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Venta.java
│   │   │   │   ├── DetalleVenta.java
│   │   │   │   ├── Pago.java                    // NUEVO
│   │   │   │   ├── Reembolso.java               // NUEVO
│   │   │   │   └── Configuracion.java           // NUEVO
│   │   │   ├── service/
│   │   │   │   └── GeneradorNumeroFactura.java
│   │   │   └── exception/
│   │   │       ├── StockInsuficienteException.java
│   │   │       ├── RecursoNoEncontradoException.java
│   │   │       ├── FacturaDuplicadaException.java
│   │   │       ├── PagosInvalidosException.java           // NUEVO
│   │   │       ├── VentaYaReembolsadaException.java       // NUEVO
│   │   │       └── LimiteFacturasDiarioExcedidoException.java  // NUEVO
│   │   │
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── producto/
│   │   │   │   │   │   ├── CrearProductoUseCase.java
│   │   │   │   │   │   ├── ListarProductosUseCase.java
│   │   │   │   │   │   ├── ObtenerProductoUseCase.java
│   │   │   │   │   │   ├── ActualizarProductoUseCase.java
│   │   │   │   │   │   └── EliminarProductoUseCase.java
│   │   │   │   │   ├── venta/
│   │   │   │   │   │   ├── RegistrarVentaUseCase.java
│   │   │   │   │   │   ├── ListarVentasUseCase.java
│   │   │   │   │   │   ├── ObtenerVentaUseCase.java
│   │   │   │   │   │   ├── ObtenerVentaPorFacturaUseCase.java
│   │   │   │   │   │   └── ReembolsarVentaUseCase.java    // NUEVO
│   │   │   │   │   ├── configuracion/                      // NUEVO
│   │   │   │   │   │   ├── ObtenerTasaImpuestoUseCase.java
│   │   │   │   │   │   └── ActualizarTasaImpuestoUseCase.java
│   │   │   │   │   └── auth/
│   │   │   │   │       └── LoginUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── ProductoRepositoryPort.java
│   │   │   │       ├── VentaRepositoryPort.java
│   │   │   │       ├── PagoVentaRepositoryPort.java       // NUEVO
│   │   │   │       ├── ReembolsoRepositoryPort.java       // NUEVO
│   │   │   │       ├── ConfiguracionRepositoryPort.java   // NUEVO
│   │   │   │       ├── UsuarioRepositoryPort.java
│   │   │   │       └── SecuenciaFacturaRepositoryPort.java
│   │   │   ├── service/
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── VentaService.java
│   │   │   │   ├── SecuenciaFacturaService.java
│   │   │   │   ├── ConfiguracionService.java              // NUEVO
│   │   │   │   └── AuthService.java
│   │   │   └── dto/
│   │   │       ├── command/
│   │   │       │   ├── CrearProductoCommand.java
│   │   │       │   ├── RegistrarVentaCommand.java
│   │   │       │   ├── PagoCommand.java                   // NUEVO
│   │   │       │   ├── ReembolsarVentaCommand.java        // NUEVO
│   │   │       │   ├── FiltroVentasCommand.java           // NUEVO
│   │   │       │   ├── ActualizarTasaImpuestoCommand.java // NUEVO
│   │   │       │   └── LoginCommand.java
│   │   │       └── response/
│   │   │           ├── ProductoResponse.java
│   │   │           ├── VentaResponse.java
│   │   │           ├── PagoResponse.java                  // NUEVO
│   │   │           ├── ReembolsoResponse.java             // NUEVO
│   │   │           ├── ConfiguracionResponse.java         // NUEVO
│   │   │           └── LoginResponse.java
│   │   │
│   │   └── infrastructure/
│   │       ├── adapter/
│   │       │   ├── in/web/
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── ProductoController.java
│   │       │   │   ├── VentaController.java
│   │       │   │   └── ConfiguracionController.java       // NUEVO
│   │       │   └── out/persistence/
│   │       │       ├── entity/
│   │       │       │   ├── UsuarioEntity.java
│   │       │       │   ├── ProductoEntity.java
│   │       │       │   ├── VentaEntity.java
│   │       │       │   ├── DetalleVentaEntity.java
│   │       │       │   ├── PagoVentaEntity.java           // NUEVO
│   │       │       │   ├── ReembolsoEntity.java           // NUEVO
│   │       │       │   └── ConfiguracionEntity.java       // NUEVO
│   │       │       ├── repository/
│   │       │       │   ├── JpaUsuarioRepository.java
│   │       │       │   ├── JpaProductoRepository.java
│   │       │       │   ├── JpaVentaRepository.java
│   │       │       │   ├── JpaPagoVentaRepository.java    // NUEVO
│   │       │       │   ├── JpaReembolsoRepository.java    // NUEVO
│   │       │       │   └── JpaConfiguracionRepository.java // NUEVO
│   │       │       ├── mapper/
│   │       │       │   ├── ProductoMapper.java
│   │       │       │   ├── VentaMapper.java
│   │       │       │   ├── PagoMapper.java                // NUEVO
│   │       │       │   ├── ReembolsoMapper.java           // NUEVO
│   │       │       │   └── ConfiguracionMapper.java       // NUEVO
│   │       │       └── adapter/
│   │       │           ├── ProductoRepositoryAdapter.java
│   │       │           ├── VentaRepositoryAdapter.java
│   │       │           ├── PagoVentaRepositoryAdapter.java    // NUEVO
│   │       │           ├── ReembolsoRepositoryAdapter.java    // NUEVO
│   │       │           ├── ConfiguracionRepositoryAdapter.java // NUEVO
│   │       │           └── UsuarioRepositoryAdapter.java
│   │       └── config/
│   │           ├── SecurityConfig.java
│   │           ├── JwtConfig.java
│   │           ├── SwaggerConfig.java
│   │           └── BeanConfig.java
│   │
│   └── test/java/com/empresa/pos/
│       ├── domain/
│       │   └── model/
│       │       ├── ProductoTest.java
│       │       ├── VentaTest.java
│       │       └── PagoTest.java                          // NUEVO
│       ├── application/service/
│       │   ├── ProductoServiceTest.java
│       │   ├── VentaServiceTest.java
│       │   ├── ConfiguracionServiceTest.java              // NUEVO
│       │   └── AuthServiceTest.java
│       └── infrastructure/adapter/in/web/
│           ├── ProductoControllerTest.java
│           ├── VentaControllerTest.java
│           └── ConfiguracionControllerTest.java           // NUEVO
├── pom.xml
└── README.md
```

---

## 10. Validaciones Específicas

### 10.1 Validación de Cédula del Cliente

**Regla:** Exactamente 10 dígitos numéricos, sin guiones, puntos ni espacios.

```java
// application/dto/command/RegistrarVentaCommand.java
@NotBlank(message = "La cédula del cliente es obligatoria")
@Pattern(regexp = "^\\d{10}$", 
         message = "La cédula debe tener exactamente 10 dígitos numéricos sin guiones ni espacios")
private String cedulaCliente;
```

**Ejemplos válidos:**
- `1234567890`
- `0987654321`

**Ejemplos inválidos:**
- `123456789` (9 dígitos)
- `12345678901` (11 dígitos)
- `123-456-7890` (contiene guiones)
- `123.456.7890` (contiene puntos)
- `123 456 7890` (contiene espacios)
- `12345abc90` (contiene letras)

### 10.2 Validación de Nombre del Cliente

**Regla:** Solo letras, espacios, tildes y ñ. Máximo 50 caracteres. Debe contener al menos 2 palabras (nombre y apellido).

```java
// application/dto/command/RegistrarVentaCommand.java
@NotBlank(message = "El nombre del cliente es obligatorio")
@Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", 
         message = "El nombre solo puede contener letras, espacios y tildes")
private String nombreCliente;

// Validación adicional en el servicio
public void validarNombreCompleto(String nombre) {
    String[] palabras = nombre.trim().split("\\s+");
    if (palabras.length < 2) {
        throw new ValidationException(
            "El nombre debe contener al menos nombre y apellido (2 palabras)"
        );
    }
}
```

**Ejemplos válidos:**
- `María González`
- `Juan Pérez López`
- `José María Rodríguez`
- `Sofía Ñúñez`

**Ejemplos inválidos:**
- `María` (solo una palabra)
- `María123` (contiene números)
- `María@González` (contiene caracteres especiales)
- `María_González` (contiene guion bajo)

### 10.3 Validación de Fecha de Factura

**Regla:** La fecha se toma al momento de CONFIRMAR la venta, no al iniciar.

```java
// application/service/VentaService.java
@Transactional
public VentaResponse registrar(RegistrarVentaCommand cmd) {
    // ... validaciones previas ...
    
    // La fecha se asigna justo antes de persistir
    venta.setFecha(LocalDateTime.now());
    
    // Si la venta inicia a las 23:59 y se confirma a las 00:01,
    // la fecha será del día siguiente
    
    // ... persistir venta ...
}
```

**Implicaciones:**
- Si una venta se inicia el 2026-05-07 a las 23:59:00
- Y se confirma el 2026-05-08 a las 00:01:00
- La fecha de la factura será: 2026-05-08
- El número de factura será: FAC-20260508-NNNNNN

### 10.4 Validación de Múltiples Métodos de Pago

**Regla:** La suma de todos los pagos debe ser exactamente igual al total de la venta.

```java
// domain/model/Venta.java
public void validarPagos() {
    if (pagos == null || pagos.isEmpty()) {
        throw new PagosInvalidosException(
            "La venta debe tener al menos un método de pago"
        );
    }
    
    BigDecimal totalPagos = pagos.stream()
        .map(Pago::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Comparación exacta con precisión de 2 decimales
    if (totalPagos.setScale(2, RoundingMode.HALF_UP)
            .compareTo(this.total.setScale(2, RoundingMode.HALF_UP)) != 0) {
        throw new PagosInvalidosException(
            String.format(
                "La suma de pagos (%.2f) debe ser igual al total de la venta (%.2f)",
                totalPagos, this.total
            )
        );
    }
}
```

**Ejemplo válido:**
```json
{
  "total": 36750.00,
  "pagos": [
    { "metodoPago": "EFECTIVO", "monto": 20000.00 },
    { "metodoPago": "TARJETA", "monto": 16750.00 }
  ]
}
// Suma: 20000 + 16750 = 36750 ✓
```

**Ejemplo inválido:**
```json
{
  "total": 36750.00,
  "pagos": [
    { "metodoPago": "EFECTIVO", "monto": 20000.00 },
    { "metodoPago": "TARJETA", "monto": 16000.00 }
  ]
}
// Suma: 20000 + 16000 = 36000 ✗ (falta 750)
```

### 10.5 Validación de Username del Cajero

**Regla:** El username se obtiene del token JWT y debe estar siempre configurado.

```java
// application/service/VentaService.java
@Transactional
public VentaResponse registrar(RegistrarVentaCommand cmd) {
    // Obtener usuario autenticado desde el contexto de seguridad
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    
    if (username == null || username.isBlank()) {
        throw new UsuarioInvalidoException(
            "El usuario autenticado debe tener un username configurado"
        );
    }
    
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "Usuario no encontrado: " + username
        ));
    
    // Usar el username como identificador del cajero
    venta.setNombreCajero(username);
    venta.setUsuarioId(usuario.getId());
    
    // ... continuar con el registro ...
}
```

---

## 11. Redondeo de Decimales

### 11.1 Estrategia de Redondeo

**Método:** `RoundingMode.HALF_UP` (redondeo comercial estándar)

**Reglas:**
- **Precisión interna:** 4-6 decimales durante cálculos intermedios (BigDecimal)
- **Precisión visual/fiscal:** 2 decimales en resultados finales
- **Redondeo por línea:** Cada línea de detalle se redondea antes de sumar

### 11.2 Implementación

```java
// domain/model/DetalleVenta.java
public class DetalleVenta {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    
    public void calcularSubtotal() {
        // Cálculo con precisión interna
        BigDecimal subtotalInterno = precioUnitario
            .multiply(BigDecimal.valueOf(cantidad));
        
        // Redondeo a 2 decimales con HALF_UP
        this.subtotal = subtotalInterno.setScale(2, RoundingMode.HALF_UP);
    }
}

// domain/model/Venta.java
public BigDecimal calcularSubtotal() {
    // Suma de subtotales ya redondeados por línea
    return detalles.stream()
        .map(DetalleVenta::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
}

public BigDecimal calcularImpuesto(BigDecimal subtotal, BigDecimal tasaImpuesto) {
    return subtotal
        .multiply(tasaImpuesto)
        .setScale(2, RoundingMode.HALF_UP);
}

public BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal impuesto) {
    return subtotal
        .add(impuesto)
        .setScale(2, RoundingMode.HALF_UP);
}
```

### 11.3 Ejemplo de Cálculo con Redondeo

```
Producto A: precio = 15.333, cantidad = 3
  → subtotal línea = 15.333 × 3 = 45.999
  → redondeado = 46.00

Producto B: precio = 7.777, cantidad = 2
  → subtotal línea = 7.777 × 2 = 15.554
  → redondeado = 15.55

Subtotal general = 46.00 + 15.55 = 61.55

Tasa impuesto = 0.05 (5%)
Impuesto = 61.55 × 0.05 = 3.0775
  → redondeado = 3.08

Total = 61.55 + 3.08 = 64.63
```

### 11.4 Casos Especiales de Redondeo

| Valor sin redondear | Redondeado (HALF_UP) | Explicación |
|---|---|---|
| 10.125 | 10.13 | 5 o más → redondea hacia arriba |
| 10.124 | 10.12 | Menos de 5 → redondea hacia abajo |
| 10.115 | 10.12 | 5 exacto → redondea hacia arriba |
| 10.005 | 10.01 | 5 en tercer decimal → redondea hacia arriba |
| 10.004 | 10.00 | Menos de 5 → redondea hacia abajo |

---

## 12. Estrategia de Concurrencia

### 12.1 Niveles de Aislamiento

**Configuración por tipo de operación:**

```java
// Operaciones de lectura simple
@Transactional(isolation = Isolation.READ_COMMITTED)
public List<Venta> listarVentas() {
    return ventaRepository.findAll();
}

// Operaciones críticas: inventario y secuencia de facturas
@Transactional(isolation = Isolation.REPEATABLE_READ)
public VentaResponse registrarVenta(RegistrarVentaCommand cmd) {
    // Garantiza que los datos leídos no cambien durante la transacción
}

// Operaciones de configuración
@Transactional(isolation = Isolation.SERIALIZABLE)
public void actualizarTasaImpuesto(BigDecimal nuevaTasa) {
    // Máximo nivel de aislamiento para cambios globales
}
```

### 12.2 SELECT FOR UPDATE

**Uso en operaciones críticas:**

```java
// infrastructure/adapter/out/persistence/repository/JpaSecuenciaFacturaRepository.java
@Query("SELECT s FROM SecuenciaFacturaEntity s WHERE s.fecha = :fecha FOR UPDATE")
Optional<SecuenciaFacturaEntity> findByFechaForUpdate(@Param("fecha") LocalDate fecha);

// infrastructure/adapter/out/persistence/repository/JpaProductoRepository.java
@Query("SELECT p FROM ProductoEntity p WHERE p.id = :id FOR UPDATE")
Optional<ProductoEntity> findByIdForUpdate(@Param("id") Long id);
```

**Aplicación en servicios:**

```java
// application/service/VentaService.java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public VentaResponse registrar(RegistrarVentaCommand cmd) {
    // 1. Bloquear secuencia de factura
    String numeroFactura = secuenciaFacturaService.obtenerSiguienteNumeroFactura();
    
    // 2. Bloquear productos para actualizar stock
    for (ItemVentaCommand item : cmd.getItems()) {
        Producto producto = productoRepository.findByIdForUpdate(item.getProductoId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        
        // Validar y descontar stock
        producto.descontarStock(item.getCantidad());
        productoRepository.save(producto);
    }
    
    // 3. Persistir venta
    // ...
}
```

### 12.3 UNIQUE Constraints

**Garantías de integridad a nivel de base de datos:**

```sql
-- Número de factura único
ALTER TABLE venta ADD CONSTRAINT uq_numero_factura UNIQUE (numero_factura);

-- Fecha única en secuencia de facturas
ALTER TABLE secuencia_factura ADD CONSTRAINT uq_fecha UNIQUE (fecha);

-- Una venta solo puede tener un reembolso
ALTER TABLE reembolso ADD CONSTRAINT uq_venta_reembolso UNIQUE (venta_id);

-- Username único
ALTER TABLE usuario ADD CONSTRAINT uq_username UNIQUE (username);

-- Clave de configuración única
ALTER TABLE configuracion ADD CONSTRAINT uq_clave UNIQUE (clave);
```

### 12.4 Optimistic Locking

**Para entidades no críticas:**

```java
// infrastructure/adapter/out/persistence/entity/ProductoEntity.java
@Entity
@Table(name = "producto")
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;  // Optimistic locking
    
    // ... otros campos ...
}
```

**Manejo de conflictos:**

```java
// application/service/ProductoService.java
@Transactional
public ProductoResponse actualizar(Long id, ActualizarProductoCommand cmd) {
    try {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        
        // Actualizar campos
        producto.setNombre(cmd.getNombre());
        producto.setPrecio(cmd.getPrecio());
        
        // Guardar (puede lanzar OptimisticLockException)
        Producto actualizado = productoRepository.save(producto);
        
        return mapper.toResponse(actualizado);
        
    } catch (OptimisticLockException e) {
        throw new ConcurrenciaException(
            "El producto fue modificado por otro usuario. Por favor, recargue e intente nuevamente."
        );
    }
}
```

### 12.5 Resumen de Estrategias por Operación

| Operación | Nivel de Aislamiento | SELECT FOR UPDATE | Optimistic Lock | UNIQUE Constraint |
|---|---|---|---|---|
| Registrar venta | REPEATABLE_READ | ✓ (productos, secuencia) | ✗ | ✓ (numero_factura) |
| Listar ventas | READ_COMMITTED | ✗ | ✗ | ✗ |
| Actualizar producto | READ_COMMITTED | ✗ | ✓ | ✗ |
| Generar factura | REPEATABLE_READ | ✓ (secuencia) | ✗ | ✓ (numero_factura) |
| Reembolsar venta | REPEATABLE_READ | ✓ (venta, productos) | ✗ | ✓ (venta_id en reembolso) |
| Actualizar tasa impuesto | SERIALIZABLE | ✓ (configuracion) | ✗ | ✓ (clave) |

---

## 13. Sistema de Reembolsos

### 13.1 Flujo de Reembolso

```
Cliente solicita reembolso
         ↓
Cajero/Admin verifica venta
         ↓
POST /api/ventas/{id}/reembolso
         ↓
Validar que venta no esté ya reembolsada
         ↓
Devolver productos al inventario
         ↓
Registrar reembolso en BD
         ↓
Marcar venta como reembolsada
         ↓
Retornar confirmación
```

### 13.2 Implementación del Use Case

```java
// application/service/VentaService.java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public ReembolsoResponse reembolsar(ReembolsarVentaCommand cmd) {
    // 1. Obtener venta
    Venta venta = ventaRepository.findById(cmd.getVentaId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
    
    // 2. Validar que no esté ya reembolsada
    if (venta.isReembolsada()) {
        throw new VentaYaReembolsadaException(
            "La venta " + venta.getNumeroFactura() + " ya fue reembolsada"
        );
    }
    
    // 3. Validar que no exista reembolso previo
    if (reembolsoRepository.existsByVentaId(venta.getId())) {
        throw new VentaYaReembolsadaException(
            "Ya existe un reembolso para esta venta"
        );
    }
    
    // 4. Devolver productos al inventario (con bloqueo)
    for (DetalleVenta detalle : venta.getDetalles()) {
        Producto producto = productoRepository.findByIdForUpdate(detalle.getProductoId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        
        // Incrementar stock
        producto.incrementarStock(detalle.getCantidad());
        productoRepository.save(producto);
    }
    
    // 5. Obtener usuario que autoriza el reembolso
    String username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    
    // 6. Crear registro de reembolso
    Reembolso reembolso = new Reembolso(
        venta.getId(),
        cmd.getMotivo(),
        usuario.getId(),
        username
    );
    
    reembolso = reembolsoRepository.save(reembolso);
    
    // 7. Marcar venta como reembolsada
    venta.setReembolsada(true);
    ventaRepository.save(venta);
    
    // 8. Retornar respuesta
    return mapper.toReembolsoResponse(reembolso);
}
```

### 13.3 Endpoint de Reembolso

```java
// infrastructure/adapter/in/web/VentaController.java
@PostMapping("/{id}/reembolso")
public ResponseEntity<ApiResponse<ReembolsoResponse>> reembolsar(
    @PathVariable Long id,
    @Valid @RequestBody ReembolsarVentaCommand cmd
) {
    cmd.setVentaId(id);
    ReembolsoResponse response = reembolsarVentaUseCase.reembolsar(cmd);
    
    return ResponseEntity.ok(ApiResponse.success(
        response,
        "Venta reembolsada exitosamente"
    ));
}
```

### 13.4 Ejemplo de Request/Response

**Request:**
```json
POST /api/ventas/123/reembolso
{
  "motivo": "Cliente insatisfecho con la calidad del producto. Solicita devolución completa."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 50,
    "ventaId": 123,
    "motivo": "Cliente insatisfecho con la calidad del producto. Solicita devolución completa.",
    "fecha": "2026-05-07T16:45:00",
    "usuarioId": 3,
    "nombreUsuario": "admin"
  },
  "message": "Venta reembolsada exitosamente",
  "timestamp": "2026-05-07T16:45:00Z"
}
```

---

## 14. Tasa de Impuesto Global

### 14.1 Configuración Predefinida

**Valor por defecto:** 5% (0.05)

```sql
INSERT INTO configuracion (clave, valor) VALUES ('tasa_impuesto', '0.05');
```

### 14.2 Obtener Tasa de Impuesto

```java
// application/service/ConfiguracionService.java
@Transactional(readOnly = true)
public BigDecimal obtenerTasaImpuesto() {
    return configuracionRepository.findByClave(Configuracion.TASA_IMPUESTO_KEY)
        .map(Configuracion::getValorComoDecimal)
        .orElse(new BigDecimal(Configuracion.TASA_IMPUESTO_DEFAULT));
}
```

### 14.3 Actualizar Tasa de Impuesto

```java
// application/service/ConfiguracionService.java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void actualizarTasaImpuesto(BigDecimal nuevaTasa) {
    // Validar rango
    if (nuevaTasa.compareTo(BigDecimal.ZERO) < 0 || 
        nuevaTasa.compareTo(BigDecimal.ONE) > 0) {
        throw new ValidationException(
            "La tasa de impuesto debe estar entre 0.0 y 1.0"
        );
    }
    
    Configuracion config = configuracionRepository
        .findByClave(Configuracion.TASA_IMPUESTO_KEY)
        .orElse(new Configuracion());
    
    config.setClave(Configuracion.TASA_IMPUESTO_KEY);
    config.setValor(nuevaTasa.toString());
    
    configuracionRepository.save(config);
}
```

### 14.4 Uso en Registro de Venta

```java
// application/service/VentaService.java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public VentaResponse registrar(RegistrarVentaCommand cmd) {
    // Obtener tasa de impuesto global
    BigDecimal tasaImpuesto = configuracionService.obtenerTasaImpuesto();
    
    // Crear venta
    Venta venta = new Venta();
    venta.setTasaImpuesto(tasaImpuesto);
    
    // ... continuar con el registro ...
}
```

### 14.5 Endpoints de Configuración

```java
// infrastructure/adapter/in/web/ConfiguracionController.java
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {
    
    @GetMapping("/tasa-impuesto")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTasaImpuesto() {
        BigDecimal tasa = obtenerTasaImpuestoUseCase.obtener();
        return ResponseEntity.ok(ApiResponse.success(tasa, "Tasa de impuesto obtenida"));
    }
    
    @PutMapping("/tasa-impuesto")
    public ResponseEntity<ApiResponse<Void>> actualizarTasaImpuesto(
        @Valid @RequestBody ActualizarTasaImpuestoCommand cmd
    ) {
        actualizarTasaImpuestoUseCase.actualizar(cmd.getTasaImpuesto());
        return ResponseEntity.ok(ApiResponse.success(null, "Tasa de impuesto actualizada"));
    }
}
```

**Ejemplo de uso:**

```bash
# Obtener tasa actual
GET /api/configuracion/tasa-impuesto
Response: { "success": true, "data": 0.05 }

# Actualizar tasa a 19%
PUT /api/configuracion/tasa-impuesto
Body: { "tasaImpuesto": 0.19 }
Response: { "success": true, "message": "Tasa de impuesto actualizada" }
```

## 15. Estrategia de Tests Unitarios

### Principio: Testear el dominio y los casos de uso de forma aislada

```
┌─────────────────────────────────────────────────────┐
│  Tests Unitarios (sin Spring context)               │
│  - Domain model: Producto, Venta, Pago              │
│  - Application services: mocks de output ports      │
│  Herramientas: JUnit 5 + Mockito + AssertJ          │
├─────────────────────────────────────────────────────┤
│  Tests de Integración (con Spring context parcial)  │
│  - Controllers: @WebMvcTest + MockMvc               │
│  - Repositorios: @DataJpaTest + H2                  │
└─────────────────────────────────────────────────────┘
```

### Ejemplo: Test unitario de dominio
```java
// ProductoTest.java
class ProductoTest {

    @Test
    void debeDescontarStockCorrectamente() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 10);
        producto.descontarStock(3);
        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void debeLanzarExcepcionCuandoStockEsInsuficiente() {
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 2);
        assertThatThrownBy(() -> producto.descontarStock(5))
            .isInstanceOf(StockInsuficienteException.class);
    }
}

// VentaTest.java
class VentaTest {

    @Test
    void debeCalcularSubtotalCorrectamente() {
        Venta venta = new Venta();
        venta.setDetalles(List.of(
            new DetalleVenta(1L, "Producto A", 2, new BigDecimal("15.00")),
            new DetalleVenta(2L, "Producto B", 1, new BigDecimal("10.00"))
        ));
        
        BigDecimal subtotal = venta.calcularSubtotal();
        
        assertThat(subtotal).isEqualByComparingTo("40.00");
    }
    
    @Test
    void debeCalcularImpuestoCorrectamente() {
        Venta venta = new Venta();
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal tasaImpuesto = new BigDecimal("0.05");
        
        BigDecimal impuesto = venta.calcularImpuesto(subtotal, tasaImpuesto);
        
        assertThat(impuesto).isEqualByComparingTo("5.00");
    }
    
    @Test
    void debeValidarPagosCorrectamente() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("100.00"));
        venta.setPagos(List.of(
            new Pago("EFECTIVO", new BigDecimal("60.00")),
            new Pago("TARJETA", new BigDecimal("40.00"))
        ));
        
        assertThatCode(() -> venta.validarPagos()).doesNotThrowAnyException();
    }
    
    @Test
    void debeLanzarExcepcionCuandoPagosNoCoinciden() {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("100.00"));
        venta.setPagos(List.of(
            new Pago("EFECTIVO", new BigDecimal("50.00"))
        ));
        
        assertThatThrownBy(() -> venta.validarPagos())
            .isInstanceOf(PagosInvalidosException.class)
            .hasMessageContaining("La suma de pagos debe ser igual al total");
    }
}

// PagoTest.java
class PagoTest {

    @Test
    void debeRedondearMontoA2Decimales() {
        Pago pago = new Pago("EFECTIVO", new BigDecimal("10.12345"));
        
        assertThat(pago.getMonto()).isEqualByComparingTo("10.12");
    }
}
```

### Ejemplo: Test unitario de caso de uso
```java
// VentaServiceTest.java
class VentaServiceTest {

    @Mock ProductoRepositoryPort productoRepository;
    @Mock VentaRepositoryPort ventaRepository;
    @Mock PagoVentaRepositoryPort pagoVentaRepository;
    @Mock UsuarioRepositoryPort usuarioRepository;
    @Mock SecuenciaFacturaService secuenciaFacturaService;
    @Mock ConfiguracionService configuracionService;
    @InjectMocks VentaService ventaService;

    @Test
    void debeRegistrarVentaConMultiplesPagosYDescontarStock() {
        // given
        Usuario usuario = new Usuario(1L, "jperez", "Juan Pérez");
        Producto producto = new Producto(1L, "Café", new BigDecimal("15000"), 10);
        
        given(usuarioRepository.findByUsername("jperez")).willReturn(Optional.of(usuario));
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        given(secuenciaFacturaService.obtenerSiguienteNumeroFactura())
            .willReturn("FAC-20260507-000001");
        given(ventaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(pagoVentaRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

        RegistrarVentaCommand cmd = RegistrarVentaCommand.builder()
            .nombreCliente("María González Pérez")
            .cedulaCliente("1234567890")
            .pagos(List.of(
                new PagoCommand("EFECTIVO", new BigDecimal("20000")),
                new PagoCommand("TARJETA", new BigDecimal("11500"))
            ))
            .items(List.of(new ItemVentaCommand(1L, 2)))
            .build();

        // when
        VentaResponse response = ventaService.registrar(cmd);

        // then
        assertThat(response.getNumeroFactura()).isEqualTo("FAC-20260507-000001");
        assertThat(response.getNombreCajero()).isEqualTo("jperez");
        assertThat(response.getNombreCliente()).isEqualTo("María González Pérez");
        assertThat(response.getCedulaCliente()).isEqualTo("1234567890");
        assertThat(response.getSubtotal()).isEqualByComparingTo("30000.00");
        assertThat(response.getImpuesto()).isEqualByComparingTo("1500.00");
        assertThat(response.getTotal()).isEqualByComparingTo("31500.00");
        assertThat(response.getPagos()).hasSize(2);
        assertThat(producto.getStock()).isEqualTo(8);
    }
    
    @Test
    void debeRechazarVentaCuandoPagosNoCoinciden() {
        // given
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        
        RegistrarVentaCommand cmd = RegistrarVentaCommand.builder()
            .nombreCliente("María González Pérez")
            .cedulaCliente("1234567890")
            .pagos(List.of(
                new PagoCommand("EFECTIVO", new BigDecimal("10000"))
            ))
            .items(List.of(new ItemVentaCommand(1L, 2)))
            .build();
        
        // when/then
        assertThatThrownBy(() -> ventaService.registrar(cmd))
            .isInstanceOf(PagosInvalidosException.class);
    }
}

// ReembolsoServiceTest.java
class ReembolsoServiceTest {

    @Mock VentaRepositoryPort ventaRepository;
    @Mock ProductoRepositoryPort productoRepository;
    @Mock ReembolsoRepositoryPort reembolsoRepository;
    @Mock UsuarioRepositoryPort usuarioRepository;
    @InjectMocks VentaService ventaService;

    @Test
    void debeReembolsarVentaYDevolverStock() {
        // given
        Venta venta = crearVentaEjemplo();
        Producto producto = new Producto(1L, "Café", new BigDecimal("15000"), 5);
        Usuario usuario = new Usuario(1L, "admin", "Admin");
        
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        given(reembolsoRepository.existsByVentaId(1L)).willReturn(false);
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(usuarioRepository.findByUsername("admin")).willReturn(Optional.of(usuario));
        given(reembolsoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        
        ReembolsarVentaCommand cmd = new ReembolsarVentaCommand(
            1L,
            "Cliente insatisfecho con la calidad"
        );
        
        // when
        ReembolsoResponse response = ventaService.reembolsar(cmd);
        
        // then
        assertThat(response.getVentaId()).isEqualTo(1L);
        assertThat(response.getMotivo()).contains("insatisfecho");
        assertThat(producto.getStock()).isEqualTo(7); // 5 + 2 devueltos
        assertThat(venta.isReembolsada()).isTrue();
    }
    
    @Test
    void debeRechazarReembolsoDuplicado() {
        // given
        Venta venta = crearVentaEjemplo();
        venta.setReembolsada(true);
        
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        
        ReembolsarVentaCommand cmd = new ReembolsarVentaCommand(1L, "Motivo");
        
        // when/then
        assertThatThrownBy(() -> ventaService.reembolsar(cmd))
            .isInstanceOf(VentaYaReembolsadaException.class);
    }
}
```

### Ejemplo: Test del Generador de Número de Factura
```java
// GeneradorNumeroFacturaTest.java
class GeneradorNumeroFacturaTest {

    private GeneradorNumeroFactura generador = new GeneradorNumeroFactura();

    @Test
    void debeGenerarNumeroFacturaConFormatoCorrecto() {
        LocalDate fecha = LocalDate.of(2026, 5, 7);
        String numeroFactura = generador.generar(fecha, 1);
        
        assertThat(numeroFactura).isEqualTo("FAC-20260507-000001");
    }

    @Test
    void debeValidarFormatoDeNumeroFactura() {
        assertThat(generador.validarFormato("FAC-20260507-000001")).isTrue();
        assertThat(generador.validarFormato("FAC-20260507-999999")).isTrue();
        assertThat(generador.validarFormato("INVALID")).isFalse();
        assertThat(generador.validarFormato("FAC-2026-0001")).isFalse();
        assertThat(generador.validarFormato("FAC-20260507-0001")).isFalse(); // 4 dígitos (viejo formato)
    }

    @Test
    void debeGenerarSecuenciaConCerosALaIzquierda() {
        LocalDate fecha = LocalDate.of(2026, 5, 7);
        
        assertThat(generador.generar(fecha, 1)).endsWith("-000001");
        assertThat(generador.generar(fecha, 99)).endsWith("-000099");
        assertThat(generador.generar(fecha, 999)).endsWith("-000999");
        assertThat(generador.generar(fecha, 9999)).endsWith("-009999");
        assertThat(generador.generar(fecha, 999999)).endsWith("-999999");
    }
}
```

---

## 16. Flujo del Sistema

### 16.1 Flujo de Login
```
REST Client          AuthController        LoginUseCase(AuthService)     JwtPort
     │                     │                        │                       │
     │─ POST /auth/login ──►│                        │                       │
     │                     │─ login(cmd) ───────────►│                       │
     │                     │                        │─ validar credenciales ►│
     │                     │                        │◄─ token JWT ───────────│
     │◄─ 200 { token } ────│◄─ LoginResponse ────────│                       │
```

### 16.2 Flujo de Venta POS con Facturación y Múltiples Pagos
```
REST Client        VentaController    RegistrarVentaUseCase    ConfiguracionService    SecuenciaFacturaService    ProductoRepositoryPort
     │                   │                    │                          │                          │                          │
     │─ POST /ventas ────►│                    │                          │                          │                          │
     │  {nombreCliente,   │                    │                          │                          │                          │
     │   cedulaCliente,   │                    │                          │                          │                          │
     │   pagos: [         │                    │                          │                          │                          │
     │     {EFECTIVO,     │                    │                          │                          │                          │
     │      20000},       │                    │                          │                          │                          │
     │     {TARJETA,      │                    │                          │                          │                          │
     │      16750}        │                    │                          │                          │                          │
     │   ],               │                    │                          │                          │                          │
     │   items}           │                    │                          │                          │                          │
     │                   │─ registrar(cmd) ───►│                          │                          │                          │
     │                   │                    │─ obtenerTasaImpuesto ────►│                          │                          │
     │                   │                    │◄─ 0.05 ────────────────────│                          │                          │
     │                   │                    │─ obtenerSiguienteNumero ──────────────────────────────►│                          │
     │                   │                    │◄─ "FAC-20260507-000001" ───────────────────────────────│                          │
     │                   │                    │─ obtener usuario (JWT) ───►│                          │                          │
     │                   │                    │◄─ Usuario (username) ──────│                          │                          │
     │                   │                    │─ findByIdForUpdate(productoId) ───────────────────────────────────────────────►│
     │                   │                    │◄─ Producto (con bloqueo) ──────────────────────────────────────────────────────│
     │                   │                    │─ validar stock                                                                  │
     │                   │                    │─ calcular subtotal por línea (cantidad × precio) con redondeo HALF_UP          │
     │                   │                    │─ calcular subtotal general (suma de líneas)                                    │
     │                   │                    │─ calcular impuesto (subtotal × 0.05) con redondeo HALF_UP                      │
     │                   │                    │─ calcular total (subtotal + impuesto) con redondeo HALF_UP                     │
     │                   │                    │─ validar pagos (suma debe = total)                                             │
     │                   │                    │─ save(venta) ───────────────────────────────────────────────────────────────────►│
     │                   │                    │─ saveAll(pagos) ────────────────────────────────────────────────────────────────►│
     │                   │                    │─ descontar stock ────────────────────────────────────────────────────────────────►│
     │◄─ 201 {          ─│◄─ VentaResponse ───│                          │                          │                          │
     │   numeroFactura,   │   con todos los    │                          │                          │                          │
     │   nombreCajero,    │   campos           │                          │                          │                          │
     │   nombreCliente,   │   completos        │                          │                          │                          │
     │   cedulaCliente,   │   incluyendo       │                          │                          │                          │
     │   detalles,        │   múltiples        │                          │                          │                          │
     │   pagos: [...]     │   pagos            │                          │                          │                          │
     │   subtotal,        │                    │                          │                          │                          │
     │   impuesto,        │                    │                          │                          │                          │
     │   total            │                    │                          │                          │                          │
     │ }                  │                    │                          │                          │                          │
```

### 16.3 Flujo de Generación de Número de Factura
```
SecuenciaFacturaService    SecuenciaFacturaRepository    GeneradorNumeroFactura    VentaRepository
         │                            │                            │                      │
         │─ obtenerSiguienteNumero() ─►│                            │                      │
         │                            │─ SELECT * FROM             │                      │
         │                            │  secuencia_factura         │                      │
         │                            │  WHERE fecha = CURDATE()   │                      │
         │                            │  FOR UPDATE                │                      │
         │◄─ ultimoNumero (ej: 5) ────│                            │                      │
         │─ siguienteNumero = 6        │                            │                      │
         │─ validar límite (≤ 999999)  │                            │                      │
         │─ generar(fecha, 6) ─────────────────────────────────────►│                      │
         │◄─ "FAC-20260507-000006" ──────────────────────────────────│                      │
         │─ verificar unicidad ────────────────────────────────────────────────────────────►│
         │◄─ false (no existe) ────────────────────────────────────────────────────────────│
         │─ actualizar secuencia ──────►│                            │                      │
         │                            │─ UPDATE secuencia_factura  │                      │
         │                            │  SET ultimo_numero = 6     │                      │
         │                            │  WHERE fecha = CURDATE()   │                      │
         │◄─ OK ──────────────────────│                            │                      │
         │─ return "FAC-20260507-000006"│                            │                      │
```

### 16.4 Flujo de Reembolso
```
REST Client        VentaController    ReembolsarVentaUseCase    VentaRepository    ProductoRepository    ReembolsoRepository
     │                   │                    │                        │                    │                      │
     │─ POST /ventas/    │                    │                        │                    │                      │
     │   123/reembolso ──►│                    │                        │                    │                      │
     │  {motivo}          │                    │                        │                    │                      │
     │                   │─ reembolsar(cmd) ──►│                        │                    │                      │
     │                   │                    │─ findById(123) ────────►│                    │                      │
     │                   │                    │◄─ Venta ────────────────│                    │                      │
     │                   │                    │─ validar no reembolsada │                    │                      │
     │                   │                    │─ existsByVentaId(123) ──────────────────────────────────────────────►│
     │                   │                    │◄─ false ────────────────────────────────────────────────────────────│
     │                   │                    │─ FOR EACH detalle:      │                    │                      │
     │                   │                    │   findByIdForUpdate(pid)────────────────────►│                      │
     │                   │                    │◄─ Producto (bloqueado) ──────────────────────│                      │
     │                   │                    │   incrementarStock(cant)──────────────────────►│                      │
     │                   │                    │─ obtener usuario (JWT)  │                    │                      │
     │                   │                    │─ crear Reembolso        │                    │                      │
     │                   │                    │─ save(reembolso) ───────────────────────────────────────────────────►│
     │                   │                    │─ marcar venta.reembolsada = true ───────────►│                      │
     │◄─ 200 {          ─│◄─ ReembolsoResp ───│                        │                    │                      │
     │   id, ventaId,     │                    │                        │                    │                      │
     │   motivo, fecha,   │                    │                        │                    │                      │
     │   usuario          │                    │                        │                    │                      │
     │ }                  │                    │                        │                    │                      │
```

### 16.5 Flujo de Actualización de Tasa de Impuesto
```
REST Client        ConfiguracionController    ActualizarTasaImpuestoUseCase    ConfiguracionRepository
     │                       │                            │                              │
     │─ PUT /configuracion/  │                            │                              │
     │   tasa-impuesto ──────►│                            │                              │
     │  {tasaImpuesto: 0.19}  │                            │                              │
     │                       │─ actualizar(0.19) ─────────►│                              │
     │                       │                            │─ validar rango (0.0 - 1.0)   │
     │                       │                            │─ findByClave("tasa_impuesto")─►│
     │                       │                            │◄─ Configuracion ──────────────│
     │                       │                            │─ actualizar valor             │
     │                       │                            │─ save(configuracion) ─────────►│
     │◄─ 200 { success } ────│◄─ void ─────────────────────│                              │
```

---

## 17. Reglas de Negocio Actualizadas

| ID | Regla | Capa que la implementa |
|---|---|---|
| RN-01 | No se puede registrar una venta con cantidad de producto igual a cero. | Domain / Application |
| RN-02 | No se puede vender un producto cuyo stock sea insuficiente para la cantidad solicitada. | Domain (`Producto.descontarStock`) |
| RN-03 | El stock se descuenta únicamente al confirmar la venta, de forma atómica (`@Transactional`). | Application Service |
| RN-04 | Una venta confirmada no puede modificarse ni eliminarse. | Application Service |
| RN-05 | El precio de venta se toma del precio del producto al momento de confirmar la venta. | Application Service |
| RN-06 | Solo usuarios autenticados pueden acceder a cualquier funcionalidad del sistema. | Infrastructure (Security) |
| RN-07 | El subtotal de la venta es la suma de `(precio_unitario × cantidad)` por cada línea de detalle. | Domain (`Venta.calcularSubtotal`) |
| RN-08 | La lógica de negocio del dominio no debe depender de ningún framework externo. | Domain |
| **RN-09** | **El nombre del cliente es obligatorio y debe contener al menos 2 palabras (nombre y apellido).** | **Application (Validación)** |
| **RN-10** | **La cédula del cliente es obligatoria y debe tener exactamente 10 dígitos numéricos.** | **Application (Validación)** |
| **RN-11** | **Una venta puede tener múltiples métodos de pago (EFECTIVO, TARJETA, TRANSFERENCIA).** | **Domain / Application** |
| **RN-12** | **La suma de todos los pagos debe ser exactamente igual al total de la venta.** | **Domain (`Venta.validarPagos`)** |
| **RN-13** | **El impuesto se calcula como: `impuesto = subtotal × tasaImpuesto` con redondeo HALF_UP.** | **Domain (`Venta.calcularImpuesto`)** |
| **RN-14** | **El total final se calcula como: `total = subtotal + impuesto` con redondeo HALF_UP.** | **Domain (`Venta.calcularTotal`)** |
| **RN-15** | **La tasa de impuesto se obtiene de la configuración global del sistema.** | **Application (ConfiguracionService)** |
| **RN-16** | **La tasa de impuesto predefinida es 5% (0.05).** | **Infrastructure (Base de datos)** |
| **RN-17** | **Cada venta debe tener un número de factura único e irrepetible.** | **Application (SecuenciaFacturaService)** |
| **RN-18** | **El número de factura sigue el formato: FAC-YYYYMMDD-NNNNNN (6 dígitos).** | **Domain (GeneradorNumeroFactura)** |
| **RN-19** | **La secuencia de facturas se reinicia a 000001 cada día.** | **Application (SecuenciaFacturaService)** |
| **RN-20** | **El sistema soporta hasta 999,999 facturas por día.** | **Application (SecuenciaFacturaService)** |
| **RN-21** | **El cajero se identifica por su username del token JWT.** | **Application Service** |
| **RN-22** | **La fecha de factura se toma al momento de CONFIRMAR la venta, no al iniciar.** | **Application Service** |
| **RN-23** | **Todos los cálculos monetarios usan redondeo HALF_UP a 2 decimales.** | **Domain** |
| **RN-24** | **El redondeo se aplica por línea de detalle antes de sumar.** | **Domain (`DetalleVenta.calcularSubtotal`)** |
| **RN-25** | **Una venta puede ser reembolsada solo una vez.** | **Application Service** |
| **RN-26** | **Al reembolsar una venta, los productos se devuelven al inventario.** | **Application Service** |
| **RN-27** | **Una venta reembolsada no puede volver a reembolsarse.** | **Application Service** |
| **RN-28** | **El motivo del reembolso es obligatorio y debe tener entre 10 y 500 caracteres.** | **Application (Validación)** |
| **RN-29** | **El listado de ventas soporta paginación y filtros por fecha, cajero, cliente y método de pago.** | **Application Service** |
| **RN-30** | **Las operaciones críticas (inventario, secuencia) usan SELECT FOR UPDATE para evitar condiciones de carrera.** | **Infrastructure (Repository)** |

---

## 18. Consideraciones de Implementación

### 18.1 Transaccionalidad
- La generación del número de factura y el registro de la venta deben ocurrir en una única transacción.
- Si falla el descuento de stock, toda la operación debe revertirse (rollback).
- La actualización de la secuencia de factura debe ser atómica para evitar duplicados.
- El reembolso de una venta (devolución de stock + registro) debe ser transaccional.
- Usar nivel de aislamiento REPEATABLE_READ para operaciones críticas.

### 18.2 Concurrencia
- Usar `@Transactional(isolation = Isolation.REPEATABLE_READ)` para registro de ventas y reembolsos.
- Usar `SELECT FOR UPDATE` en la tabla `secuencia_factura` para bloquear la fila durante la transacción.
- Usar `SELECT FOR UPDATE` al obtener productos para actualizar stock.
- Implementar Optimistic Locking con `@Version` en entidades no críticas (Producto, Usuario).
- UNIQUE constraints en BD para garantizar integridad (numero_factura, username, clave_configuracion).

### 18.3 Validaciones
- **Cédula:** Exactamente 10 dígitos numéricos, sin guiones, puntos ni espacios.
- **Nombre:** Solo letras, espacios, tildes y ñ. Máximo 50 caracteres. Mínimo 2 palabras.
- **Pagos:** La suma de todos los pagos debe ser exactamente igual al total de la venta.
- **Métodos de pago:** Solo EFECTIVO, TARJETA o TRANSFERENCIA.
- **Tasa de impuesto:** Entre 0.0 y 1.0 (0% a 100%).
- **Motivo de reembolso:** Entre 10 y 500 caracteres.
- Validar que el usuario autenticado tenga username configurado.

### 18.4 Seguridad
- El `usuarioId` y `nombreCajero` se obtienen del token JWT, no del request body.
- Validar que el usuario autenticado existe antes de registrar la venta.
- No permitir modificar el número de factura una vez generado.
- No permitir modificar ni eliminar ventas confirmadas.
- No permitir reembolsar una venta ya reembolsada.
- Proteger endpoint de actualización de tasa de impuesto (solo administradores).

### 18.5 Performance
- Crear índices en:
  - `venta.numero_factura` (búsquedas por factura)
  - `venta.fecha` (reportes por fecha)
  - `venta.cedula_cliente` (búsquedas por cliente)
  - `venta.usuario_id` (búsquedas por cajero)
  - `venta.reembolsada` (filtrar ventas reembolsadas)
  - `secuencia_factura.fecha` (generación de secuencia)
  - `pago_venta.venta_id` (consulta de pagos)
  - `reembolso.venta_id` (consulta de reembolsos)
  - `configuracion.clave` (consulta de configuración)
- Usar paginación en listado de ventas para evitar cargar grandes volúmenes de datos.
- Limitar resultados por día en consultas de ventas.

### 18.6 Redondeo y Precisión
- Usar `BigDecimal` para todos los cálculos monetarios.
- Aplicar `RoundingMode.HALF_UP` (redondeo comercial estándar).
- Precisión interna: 4-6 decimales durante cálculos intermedios.
- Precisión visual/fiscal: 2 decimales en resultados finales.
- Redondear por línea de detalle antes de sumar subtotales.
- Redondear impuesto y total final a 2 decimales.

### 18.7 Migración de Datos Existentes

#### Migración de Formato de Factura (4 a 6 dígitos)
```sql
-- Actualizar formato de facturas existentes (si aplica)
-- De FAC-YYYYMMDD-NNNN a FAC-YYYYMMDD-NNNNNN
UPDATE venta
SET numero_factura = CONCAT(
    SUBSTRING(numero_factura, 1, 13),  -- FAC-YYYYMMDD-
    LPAD(SUBSTRING(numero_factura, 14), 6, '0')  -- NNNN -> NNNNNN
)
WHERE numero_factura LIKE 'FAC-________-____';
```

#### Migración de Método de Pago Único a Múltiples
```sql
-- Crear registros en pago_venta para ventas existentes
INSERT INTO pago_venta (venta_id, metodo_pago, monto)
SELECT id, metodo_pago, total
FROM venta
WHERE NOT EXISTS (
    SELECT 1 FROM pago_venta WHERE venta_id = venta.id
);

-- Eliminar columna metodo_pago de venta (después de migración)
-- ALTER TABLE venta DROP COLUMN metodo_pago;
```

#### Migración de Tasa de Impuesto
```sql
-- Si ya existen ventas sin tasa de impuesto registrada
UPDATE venta
SET tasa_impuesto = 0.05
WHERE tasa_impuesto IS NULL OR tasa_impuesto = 0;
```

### 18.8 Manejo de Errores

**Excepciones de dominio:**
- `StockInsuficienteException`: Stock insuficiente para la venta
- `PagosInvalidosException`: La suma de pagos no coincide con el total
- `VentaYaReembolsadaException`: Intento de reembolsar una venta ya reembolsada
- `FacturaDuplicadaException`: Número de factura duplicado
- `LimiteFacturasDiarioExcedidoException`: Se alcanzó el límite de 999,999 facturas por día
- `RecursoNoEncontradoException`: Recurso no encontrado (producto, venta, usuario)
- `UsuarioInvalidoException`: Usuario sin username configurado
- `ConcurrenciaException`: Conflicto de concurrencia (Optimistic Lock)

**Códigos HTTP:**
- 200: Operación exitosa
- 201: Recurso creado (venta registrada)
- 400: Validación fallida, datos inválidos
- 401: No autenticado
- 403: No autorizado
- 404: Recurso no encontrado
- 409: Conflicto (factura duplicada, venta ya reembolsada)
- 500: Error interno del servidor

### 18.9 Logging y Auditoría

**Eventos a registrar:**
- Registro de venta: número de factura, cajero, total, fecha
- Reembolso de venta: número de factura, usuario que autoriza, motivo
- Actualización de tasa de impuesto: valor anterior, valor nuevo, usuario
- Errores de concurrencia: número de factura, usuario, timestamp
- Errores de validación: tipo de error, datos enviados, usuario

**Formato de log:**
```
[INFO] 2026-05-07 14:30:00 - Venta registrada: FAC-20260507-000001, Cajero: jperez, Total: 36750.00
[INFO] 2026-05-07 16:45:00 - Venta reembolsada: FAC-20260507-000001, Usuario: admin, Motivo: Cliente insatisfecho
[WARN] 2026-05-07 18:00:00 - Intento de reembolso duplicado: FAC-20260507-000001, Usuario: jperez
[ERROR] 2026-05-07 20:00:00 - Error de concurrencia al generar factura, Usuario: jperez
```
