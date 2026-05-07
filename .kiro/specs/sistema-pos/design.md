# Documento de Diseño Técnico
# Sistema POS - Point of Sale
**Versión:** 2.0.0  
**Fecha:** 2026-04-22  
**Arquitectura:** Hexagonal (Ports & Adapters)

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
    private Long usuarioId;
    private List<DetalleVenta> detalles;
    private BigDecimal total;
    private LocalDateTime fecha;

    public BigDecimal calcularTotal() {
        return detalles.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)   NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    activo      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE venta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT         NOT NULL,
    total       DECIMAL(10, 2) NOT NULL,
    fecha       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE detalle_venta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id    BIGINT         NOT NULL,
    producto_id BIGINT         NOT NULL,
    cantidad    INT            NOT NULL,
    precio_unit DECIMAL(10, 2) NOT NULL,
    subtotal    DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES venta(id),
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);
```

### Relaciones

```
Usuario ──< Venta ──< DetalleVenta >── Producto
  1           1:N         N:1            1
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
interface ListarVentasUseCase     { List<VentaResponse> listar(); }

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
    List<Venta> findAll();
}

interface UsuarioRepositoryPort {
    Optional<Usuario> findByUsername(String username);
}
```

---

## 6. Endpoints REST

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
| POST | `/api/ventas` | Registrar nueva venta | JWT |
| GET | `/api/ventas` | Listar ventas | JWT |
| GET | `/api/ventas/{id}` | Obtener detalle de venta | JWT |

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

## 7. Estructura de Carpetas (Hexagonal)

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
│   │   │   │   └── DetalleVenta.java
│   │   │   └── exception/
│   │   │       ├── StockInsuficienteException.java
│   │   │       └── RecursoNoEncontradoException.java
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
│   │   │   │   │   │   └── ListarVentasUseCase.java
│   │   │   │   │   └── auth/
│   │   │   │   │       └── LoginUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── ProductoRepositoryPort.java
│   │   │   │       ├── VentaRepositoryPort.java
│   │   │   │       └── UsuarioRepositoryPort.java
│   │   │   ├── service/
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── VentaService.java
│   │   │   │   └── AuthService.java
│   │   │   └── dto/
│   │   │       ├── command/
│   │   │       │   ├── CrearProductoCommand.java
│   │   │       │   ├── RegistrarVentaCommand.java
│   │   │       │   └── LoginCommand.java
│   │   │       └── response/
│   │   │           ├── ProductoResponse.java
│   │   │           ├── VentaResponse.java
│   │   │           └── LoginResponse.java
│   │   │
│   │   └── infrastructure/
│   │       ├── adapter/
│   │       │   ├── in/web/
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── ProductoController.java
│   │       │   │   └── VentaController.java
│   │       │   └── out/persistence/
│   │       │       ├── entity/
│   │       │       │   ├── UsuarioEntity.java
│   │       │       │   ├── ProductoEntity.java
│   │       │       │   ├── VentaEntity.java
│   │       │       │   └── DetalleVentaEntity.java
│   │       │       ├── repository/
│   │       │       │   ├── JpaUsuarioRepository.java
│   │       │       │   ├── JpaProductoRepository.java
│   │       │       │   └── JpaVentaRepository.java
│   │       │       ├── mapper/
│   │       │       │   ├── ProductoMapper.java
│   │       │       │   └── VentaMapper.java
│   │       │       └── adapter/
│   │       │           ├── ProductoRepositoryAdapter.java
│   │       │           ├── VentaRepositoryAdapter.java
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
│       │       └── VentaTest.java
│       ├── application/service/
│       │   ├── ProductoServiceTest.java
│       │   ├── VentaServiceTest.java
│       │   └── AuthServiceTest.java
│       └── infrastructure/adapter/in/web/
│           ├── ProductoControllerTest.java
│           └── VentaControllerTest.java
├── pom.xml
└── README.md
```

---

## 8. Estrategia de Tests Unitarios

### Principio: Testear el dominio y los casos de uso de forma aislada

```
┌─────────────────────────────────────────────────────┐
│  Tests Unitarios (sin Spring context)               │
│  - Domain model: Producto, Venta                    │
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
```

### Ejemplo: Test unitario de caso de uso
```java
// VentaServiceTest.java
class VentaServiceTest {

    @Mock ProductoRepositoryPort productoRepository;
    @Mock VentaRepositoryPort ventaRepository;
    @InjectMocks VentaService ventaService;

    @Test
    void debeRegistrarVentaYDescontarStock() {
        // given
        Producto producto = new Producto(1L, "Café", BigDecimal.TEN, 10);
        given(productoRepository.findById(1L)).willReturn(Optional.of(producto));
        given(ventaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RegistrarVentaCommand cmd = new RegistrarVentaCommand(
            "user1", List.of(new ItemVentaCommand(1L, 3))
        );

        // when
        VentaResponse response = ventaService.registrar(cmd);

        // then
        assertThat(response.getTotal()).isEqualByComparingTo("30.00");
        assertThat(producto.getStock()).isEqualTo(7);
    }
}
```

---

## 9. Flujo del Sistema

### 9.1 Flujo de Login
```
REST Client          AuthController        LoginUseCase(AuthService)     JwtPort
     │                     │                        │                       │
     │─ POST /auth/login ──►│                        │                       │
     │                     │─ login(cmd) ───────────►│                       │
     │                     │                        │─ validar credenciales ►│
     │                     │                        │◄─ token JWT ───────────│
     │◄─ 200 { token } ────│◄─ LoginResponse ────────│                       │
```

### 9.2 Flujo de Venta POS
```
REST Client        VentaController    RegistrarVentaUseCase    ProductoRepositoryPort
     │                   │                    │                        │
     │─ POST /ventas ────►│                    │                        │
     │                   │─ registrar(cmd) ───►│                        │
     │                   │                    │─ findById(id) ─────────►│
     │                   │                    │◄─ Producto ─────────────│
     │                   │                    │─ validar stock           │
     │                   │                    │─ calcular total          │
     │                   │                    │─ save(venta) ───────────►│
     │                   │                    │─ descontar stock ────────►│
     │◄─ 201 { venta } ──│◄─ VentaResponse ───│                        │
```
