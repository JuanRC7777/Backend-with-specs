# 🧪 Guía de Pruebas — Sistema POS API

**Base URL:** `http://localhost:8081`  
**Swagger UI:** `http://localhost:8081/swagger-ui.html`

---

## ⚙️ Cómo correr el proyecto

```bash
# Opción 1 — Maven wrapper (recomendado para desarrollo)
./mvnw spring-boot:run

# Opción 2 — Con contraseña de MySQL
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-DDB_PASSWORD=tu_password"

# Opción 3 — JAR generado
java -jar target/pos-backend-1.0.0-SNAPSHOT.jar

# Opción 4 — Variables de entorno completas
DB_URL=jdbc:mysql://localhost:3306/pos_db DB_USERNAME=root DB_PASSWORD=secret ./mvnw spring-boot:run
```

---

## 🔐 1. Autenticación

### Login (obtener token JWT)

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta esperada (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "nombre": "Administrador"
}
```

> ⚠️ Copia el `token` — lo necesitas en todos los demás endpoints como header:
> `Authorization: Bearer <token>`

---

## 📦 2. Productos

### Crear producto

```http
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Café Premium",
  "descripcion": "Café colombiano de alta calidad",
  "precio": 15000.00,
  "stock": 100
}
```

**Respuesta esperada (201):**
```json
{
  "id": 1,
  "nombre": "Café Premium",
  "descripcion": "Café colombiano de alta calidad",
  "precio": 15000.00,
  "stock": 100,
  "activo": true
}
```

---

### Crear más productos para pruebas

```http
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Leche Entera",
  "descripcion": "Leche entera 1 litro",
  "precio": 3500.00,
  "stock": 50
}
```

```http
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Pan Integral",
  "descripcion": "Pan integral artesanal",
  "precio": 4200.00,
  "stock": 30
}
```

---

### Listar todos los productos

```http
GET /api/productos
Authorization: Bearer <token>
```

---

### Obtener producto por ID

```http
GET /api/productos/1
Authorization: Bearer <token>
```

**Error esperado si no existe (404):**
```json
{
  "success": false,
  "message": "Producto no encontrado con ID: 99",
  "timestamp": "2026-05-08T10:00:00"
}
```

---

### Actualizar producto

```http
PUT /api/productos/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Café Premium Especial",
  "descripcion": "Café colombiano de alta calidad - edición especial",
  "precio": 18000.00,
  "stock": 80
}
```

---

### Eliminar producto (eliminación lógica)

```http
DELETE /api/productos/1
Authorization: Bearer <token>
```

**Respuesta esperada (204 No Content)**

> El producto queda con `activo: false` — no aparece en el listado pero sigue en BD.

---

## 🛒 3. Ventas

### Registrar venta con un solo método de pago

```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "María García",
  "cedulaCliente": "0987654321",
  "items": [
    {
      "productoId": 2,
      "cantidad": 2
    },
    {
      "productoId": 3,
      "cantidad": 1
    }
  ],
  "pagos": [
    {
      "metodoPago": "EFECTIVO",
      "monto": 11200.00
    }
  ]
}
```

> 💡 El total se calcula automáticamente:
> - Subtotal: (3500 × 2) + (4200 × 1) = 11200.00
> - Impuesto (5%): 560.00
> - **Total: 11760.00**
> 
> ⚠️ El monto del pago debe ser exactamente igual al total. Ajusta el monto a `11760.00`.

**Versión corregida:**
```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "María García",
  "cedulaCliente": "0987654321",
  "items": [
    { "productoId": 2, "cantidad": 2 },
    { "productoId": 3, "cantidad": 1 }
  ],
  "pagos": [
    { "metodoPago": "EFECTIVO", "monto": 11760.00 }
  ]
}
```

**Respuesta esperada (201):**
```json
{
  "id": 1,
  "numeroFactura": "FAC-20260508-000001",
  "nombreCajero": "admin",
  "nombreCliente": "María García",
  "cedulaCliente": "0987654321",
  "subtotal": 11200.00,
  "tasaImpuesto": 0.05,
  "impuesto": 560.00,
  "total": 11760.00,
  "pagos": [
    { "id": 1, "metodoPago": "EFECTIVO", "monto": 11760.00 }
  ],
  "reembolsada": false,
  "reembolso": null
}
```

---

### Registrar venta con múltiples métodos de pago

```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "Carlos Rodríguez",
  "cedulaCliente": "1122334455",
  "items": [
    { "productoId": 2, "cantidad": 3 }
  ],
  "pagos": [
    { "metodoPago": "EFECTIVO", "monto": 5000.00 },
    { "metodoPago": "TARJETA", "monto": 6512.50 }
  ]
}
```

> Subtotal: 3500 × 3 = 10500, Impuesto: 525, Total: 11025 → pagos: 5000 + 6025 = 11025

---

### Errores de validación en ventas

**Cédula inválida (no tiene 10 dígitos):**
```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "Juan Pérez",
  "cedulaCliente": "12345",
  "items": [{ "productoId": 2, "cantidad": 1 }],
  "pagos": [{ "metodoPago": "EFECTIVO", "monto": 3675.00 }]
}
```
**Respuesta esperada (400)**

---

**Nombre con solo una palabra:**
```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "Juan",
  "cedulaCliente": "1234567890",
  "items": [{ "productoId": 2, "cantidad": 1 }],
  "pagos": [{ "metodoPago": "EFECTIVO", "monto": 3675.00 }]
}
```
**Respuesta esperada (400)**

---

**Método de pago inválido:**
```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "Juan Pérez",
  "cedulaCliente": "1234567890",
  "items": [{ "productoId": 2, "cantidad": 1 }],
  "pagos": [{ "metodoPago": "BITCOIN", "monto": 3675.00 }]
}
```
**Respuesta esperada (400)**

---

**Suma de pagos no coincide con total:**
```http
POST /api/ventas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreCliente": "Juan Pérez",
  "cedulaCliente": "1234567890",
  "items": [{ "productoId": 2, "cantidad": 1 }],
  "pagos": [{ "metodoPago": "EFECTIVO", "monto": 1000.00 }]
}
```
**Respuesta esperada (400)** — PagosInvalidosException

---

### Obtener venta por ID

```http
GET /api/ventas/1
Authorization: Bearer <token>
```

---

### Obtener venta por número de factura

```http
GET /api/ventas/factura/FAC-20260508-000001
Authorization: Bearer <token>
```

---

### Listar ventas (con paginación)

```http
GET /api/ventas?page=0&size=10
Authorization: Bearer <token>
```

---

### Listar ventas con filtros

**Por fecha:**
```http
GET /api/ventas?fecha=2026-05-08
Authorization: Bearer <token>
```

**Por cédula del cliente:**
```http
GET /api/ventas?cedulaCliente=0987654321
Authorization: Bearer <token>
```

**Por método de pago:**
```http
GET /api/ventas?metodoPago=EFECTIVO
Authorization: Bearer <token>
```

**Combinado:**
```http
GET /api/ventas?fecha=2026-05-08&metodoPago=TARJETA&page=0&size=5
Authorization: Bearer <token>
```

---

## 💸 4. Reembolsos

### Reembolsar una venta

```http
POST /api/ventas/1/reembolso
Authorization: Bearer <token>
Content-Type: application/json

{
  "motivo": "El cliente recibió un producto defectuoso y solicita devolución completa"
}
```

**Respuesta esperada (200):**
```json
{
  "id": 1,
  "ventaId": 1,
  "motivo": "El cliente recibió un producto defectuoso y solicita devolución completa",
  "fecha": "2026-05-08T10:30:00",
  "usuarioId": 1,
  "nombreUsuario": "admin"
}
```

> ✅ El stock de los productos se devuelve automáticamente.

---

### Intentar reembolsar una venta ya reembolsada

```http
POST /api/ventas/1/reembolso
Authorization: Bearer <token>
Content-Type: application/json

{
  "motivo": "Intento de segundo reembolso"
}
```

**Respuesta esperada (400)** — VentaYaReembolsadaException

---

### Motivo muy corto (menos de 10 caracteres)

```http
POST /api/ventas/1/reembolso
Authorization: Bearer <token>
Content-Type: application/json

{
  "motivo": "Corto"
}
```

**Respuesta esperada (400)**

---

## ⚙️ 5. Configuración

### Obtener tasa de impuesto actual

```http
GET /api/configuracion/tasa-impuesto
Authorization: Bearer <token>
```

**Respuesta esperada (200):**
```json
{
  "clave": "tasa_impuesto",
  "valor": "0.05",
  "valorDecimal": 0.05
}
```

---

### Actualizar tasa de impuesto

```http
PUT /api/configuracion/tasa-impuesto
Authorization: Bearer <token>
Content-Type: application/json

{
  "tasaImpuesto": 0.12
}
```

**Respuesta esperada (200):**
```json
{
  "clave": "tasa_impuesto",
  "valor": "0.12",
  "valorDecimal": 0.12
}
```

---

### Tasa inválida (mayor a 1.0)

```http
PUT /api/configuracion/tasa-impuesto
Authorization: Bearer <token>
Content-Type: application/json

{
  "tasaImpuesto": 1.5
}
```

**Respuesta esperada (400)**

---

### Tasa negativa

```http
PUT /api/configuracion/tasa-impuesto
Authorization: Bearer <token>
Content-Type: application/json

{
  "tasaImpuesto": -0.05
}
```

**Respuesta esperada (400)**

---

## 🔒 6. Seguridad

### Sin token (401)

```http
GET /api/productos
```

**Respuesta esperada (401 Unauthorized)**

---

### Token inválido (401)

```http
GET /api/productos
Authorization: Bearer token.invalido.aqui
```

**Respuesta esperada (401 Unauthorized)**

---

## 📋 Flujo completo de prueba

Sigue este orden para probar todo el sistema de punta a punta:

```
1. POST /api/auth/login          → obtener token
2. POST /api/productos           → crear 3 productos
3. GET  /api/productos           → verificar lista
4. POST /api/ventas              → registrar venta (pago exacto al total)
5. GET  /api/ventas/1            → verificar venta con número de factura
6. GET  /api/ventas/factura/FAC-... → buscar por número de factura
7. GET  /api/configuracion/tasa-impuesto → ver tasa actual
8. PUT  /api/configuracion/tasa-impuesto → cambiar tasa a 0.10
9. POST /api/ventas              → nueva venta (verifica que usa nueva tasa)
10. POST /api/ventas/1/reembolso → reembolsar primera venta
11. GET  /api/ventas/1           → verificar que reembolsada=true
12. GET  /api/productos/2        → verificar que stock se devolvió
13. GET  /api/ventas?metodoPago=EFECTIVO → filtrar por método de pago
```

---

## 🛠️ Herramientas recomendadas

- **Swagger UI:** `http://localhost:8081/swagger-ui.html` — interfaz visual completa
- **Postman:** importa las requests de arriba
- **curl:** usa los ejemplos directamente en terminal
- **HTTPie:** `http POST localhost:8081/api/auth/login username=admin password=admin123`

---

## 📊 Formato de errores

Todos los errores siguen el mismo formato:

```json
{
  "success": false,
  "message": "Descripción del error",
  "timestamp": "2026-05-08T10:00:00"
}
```

Para errores de validación (400):
```json
{
  "success": false,
  "message": "Datos de entrada inválidos",
  "errors": {
    "cedulaCliente": "La cédula del cliente debe tener exactamente 10 dígitos",
    "nombreCliente": "El nombre del cliente solo puede contener letras, espacios y tildes"
  },
  "timestamp": "2026-05-08T10:00:00"
}
```

---

## 📌 Notas importantes

| Regla | Detalle |
|-------|---------|
| Número de factura | Formato `FAC-YYYYMMDD-NNNNNN` — generado automáticamente |
| Tasa de impuesto | Se obtiene de configuración global (default 5%) |
| Nombre cajero | Se extrae automáticamente del token JWT |
| Cédula cliente | Exactamente 10 dígitos numéricos |
| Nombre cliente | Mínimo 2 palabras, solo letras/espacios/tildes/ñ |
| Métodos de pago | Solo: `EFECTIVO`, `TARJETA`, `TRANSFERENCIA` |
| Suma de pagos | Debe ser exactamente igual al total calculado |
| Reembolso | Solo total — devuelve stock de todos los productos |
| Límite facturas | Máximo 999,999 por día |
