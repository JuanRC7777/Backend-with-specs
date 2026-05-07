# 📮 Guía Completa de Postman - Sistema POS

## 📋 Índice
1. [Configuración Inicial](#configuración-inicial)
2. [Importar Colección](#importar-colección)
3. [Configurar Variables de Entorno](#configurar-variables-de-entorno)
4. [Pruebas Paso a Paso](#pruebas-paso-a-paso)
5. [Automatización con Scripts](#automatización-con-scripts)

---

## 🚀 Configuración Inicial

### Requisitos
- ✅ Postman instalado ([Descargar aquí](https://www.postman.com/downloads/))
- ✅ Servidor corriendo en `http://localhost:8081`

---

## 📥 Importar Colección

### Opción 1: Importar archivo JSON
1. Descarga el archivo `POS-API.postman_collection.json` (lo crearé abajo)
2. Abre Postman
3. Haz clic en **Import** (esquina superior izquierda)
4. Arrastra el archivo o selecciónalo
5. Haz clic en **Import**

### Opción 2: Crear manualmente
Sigue las instrucciones de la sección [Pruebas Paso a Paso](#pruebas-paso-a-paso)

---

## 🔧 Configurar Variables de Entorno

### Paso 1: Crear Environment
1. Haz clic en **Environments** (icono de ojo 👁️ en la esquina superior derecha)
2. Haz clic en **Create Environment** o el botón **+**
3. Nombra el environment: `POS Local`

### Paso 2: Agregar Variables
Agrega estas variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8081` | `http://localhost:8081` |
| `token` | (vacío) | (vacío) |
| `producto_id` | (vacío) | (vacío) |
| `venta_id` | (vacío) | (vacío) |

### Paso 3: Activar Environment
1. Selecciona **POS Local** en el dropdown de environments (esquina superior derecha)

---

## 🧪 Pruebas Paso a Paso

### 1️⃣ Login (Autenticación)

**Configuración:**
- **Método:** `POST`
- **URL:** `{{base_url}}/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw - JSON):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Script Post-Response (Tests tab):**
```javascript
// Guardar el token automáticamente
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.test("Login exitoso", function () {
        pm.expect(jsonData.token).to.exist;
    });
    console.log("Token guardado: " + jsonData.token.substring(0, 50) + "...");
} else {
    pm.test("Login fallido", function () {
        pm.expect.fail("Error en login");
    });
}
```

**Resultado Esperado:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "nombre": "Administrador"
}
```

---

### 2️⃣ Crear Producto

**Configuración:**
- **Método:** `POST`
- **URL:** `{{base_url}}/api/productos`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {{token}}`
- **Body (raw - JSON):**
```json
{
  "nombre": "Laptop Dell Inspiron 15",
  "descripcion": "Laptop Dell con procesador Intel Core i7",
  "precio": 15000.00,
  "stock": 10
}
```

**Script Post-Response (Tests tab):**
```javascript
if (pm.response.code === 200 || pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("producto_id", jsonData.id);
    pm.test("Producto creado exitosamente", function () {
        pm.expect(jsonData.id).to.exist;
        pm.expect(jsonData.nombre).to.eql("Laptop Dell Inspiron 15");
    });
    console.log("Producto ID guardado: " + jsonData.id);
}
```

**Resultado Esperado:**
```json
{
  "id": 1,
  "nombre": "Laptop Dell Inspiron 15",
  "descripcion": "Laptop Dell con procesador Intel Core i7",
  "precio": 15000.00,
  "stock": 10,
  "activo": true
}
```

---

### 3️⃣ Listar Productos

**Configuración:**
- **Método:** `GET`
- **URL:** `{{base_url}}/api/productos`
- **Headers:**
  - `Authorization: Bearer {{token}}`

**Script Post-Response (Tests tab):**
```javascript
pm.test("Status code es 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Respuesta es un array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});

pm.test("Productos tienen estructura correcta", function () {
    var jsonData = pm.response.json();
    if (jsonData.length > 0) {
        pm.expect(jsonData[0]).to.have.property('id');
        pm.expect(jsonData[0]).to.have.property('nombre');
        pm.expect(jsonData[0]).to.have.property('precio');
        pm.expect(jsonData[0]).to.have.property('stock');
    }
});
```

**Resultado Esperado:**
```json
[
  {
    "id": 1,
    "nombre": "Laptop Dell Inspiron 15",
    "descripcion": "Laptop Dell con procesador Intel Core i7",
    "precio": 15000.00,
    "stock": 10,
    "activo": true
  }
]
```

---

### 4️⃣ Obtener Producto por ID

**Configuración:**
- **Método:** `GET`
- **URL:** `{{base_url}}/api/productos/{{producto_id}}`
- **Headers:**
  - `Authorization: Bearer {{token}}`

**Script Post-Response (Tests tab):**
```javascript
pm.test("Producto encontrado", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.eql(parseInt(pm.environment.get("producto_id")));
});
```

---

### 5️⃣ Actualizar Producto

**Configuración:**
- **Método:** `PUT`
- **URL:** `{{base_url}}/api/productos/{{producto_id}}`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {{token}}`
- **Body (raw - JSON):**
```json
{
  "nombre": "Laptop Dell XPS 15",
  "descripcion": "Laptop Dell XPS con pantalla 4K",
  "precio": 18000.00,
  "stock": 8
}
```

**Script Post-Response (Tests tab):**
```javascript
pm.test("Producto actualizado", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.nombre).to.eql("Laptop Dell XPS 15");
    pm.expect(jsonData.precio).to.eql(18000.00);
});
```

---

### 6️⃣ Registrar Venta

**Configuración:**
- **Método:** `POST`
- **URL:** `{{base_url}}/api/ventas`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {{token}}`
- **Body (raw - JSON):**
```json
{
  "items": [
    {
      "productoId": {{producto_id}},
      "cantidad": 2
    }
  ]
}
```

**Nota:** El `usuarioId` se obtiene automáticamente del token JWT. No es necesario enviarlo en el body.

**Script Pre-Request (Pre-request Script tab):**
```javascript
// Asegurarse de que producto_id esté disponible
var productoId = pm.environment.get("producto_id");
if (!productoId) {
    console.error("producto_id no está definido. Ejecuta 'Crear Producto' primero.");
}
```

**Script Post-Response (Tests tab):**
```javascript
if (pm.response.code === 200 || pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("venta_id", jsonData.id);
    pm.test("Venta registrada exitosamente", function () {
        pm.expect(jsonData.id).to.exist;
        pm.expect(jsonData.total).to.exist;
        pm.expect(jsonData.detalles).to.be.an('array');
    });
    console.log("Venta ID guardado: " + jsonData.id);
    console.log("Total de la venta: $" + jsonData.total);
}
```

**Resultado Esperado:**
```json
{
  "id": 1,
  "usuarioNombre": "Administrador",
  "total": 36000.00,
  "fecha": "2026-05-06T20:30:00",
  "detalles": [
    {
      "productoNombre": "Laptop Dell XPS 15",
      "cantidad": 2,
      "precioUnit": 18000.00,
      "subtotal": 36000.00
    }
  ]
}
```

---

### 7️⃣ Listar Ventas

**Configuración:**
- **Método:** `GET`
- **URL:** `{{base_url}}/api/ventas`
- **Headers:**
  - `Authorization: Bearer {{token}}`

**Script Post-Response (Tests tab):**
```javascript
pm.test("Ventas listadas correctamente", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
    if (jsonData.length > 0) {
        pm.expect(jsonData[0]).to.have.property('id');
        pm.expect(jsonData[0]).to.have.property('total');
        pm.expect(jsonData[0]).to.have.property('fecha');
        pm.expect(jsonData[0]).to.have.property('detalles');
    }
});
```

---

### 8️⃣ Eliminar Producto

**Configuración:**
- **Método:** `DELETE`
- **URL:** `{{base_url}}/api/productos/{{producto_id}}`
- **Headers:**
  - `Authorization: Bearer {{token}}`

**Script Post-Response (Tests tab):**
```javascript
pm.test("Producto eliminado", function () {
    pm.response.to.have.status(204);
});
```

---

## 🤖 Automatización con Collection Runner

### Ejecutar toda la colección

1. Haz clic en la colección **POS API**
2. Haz clic en **Run** (botón azul)
3. Selecciona el environment **POS Local**
4. Haz clic en **Run POS API**
5. Observa los resultados de todas las pruebas

### Orden de ejecución recomendado:
1. ✅ Login
2. ✅ Crear Producto
3. ✅ Listar Productos
4. ✅ Obtener Producto por ID
5. ✅ Actualizar Producto
6. ✅ Registrar Venta
7. ✅ Listar Ventas
8. ✅ Eliminar Producto

---

## 📊 Tests Automáticos Incluidos

Cada request incluye tests que verifican:
- ✅ Código de respuesta HTTP correcto
- ✅ Estructura de datos correcta
- ✅ Valores esperados en los campos
- ✅ Guardado automático de variables (token, IDs)

---

## 🔍 Verificar Stock Descontado

Después de registrar una venta:

1. Ejecuta **Obtener Producto por ID**
2. Verifica que el campo `stock` se haya descontado
3. Ejemplo: Si tenías 8 y vendiste 2, ahora deberías tener 6

---

## 🐛 Solución de Problemas

### Error 401 Unauthorized
- **Causa:** Token expirado o inválido
- **Solución:** Ejecuta nuevamente el request de **Login**

### Error 404 Not Found
- **Causa:** El ID del producto/venta no existe
- **Solución:** Verifica que las variables `producto_id` o `venta_id` estén definidas

### Error 400 Bad Request
- **Causa:** Datos inválidos en el body
- **Solución:** Verifica el formato JSON y los tipos de datos

### Error 500 Internal Server Error
- **Causa:** Error en el servidor
- **Solución:** Revisa los logs del servidor Spring Boot

---

## 💡 Tips y Trucos

### 1. Ver variables de entorno
- Haz clic en el icono de ojo 👁️ (esquina superior derecha)
- Verás todas las variables y sus valores actuales

### 2. Copiar token manualmente
Si los scripts no funcionan:
1. Ejecuta Login
2. Copia el valor de `token` de la respuesta
3. Ve a Environments → POS Local
4. Pega el token en la variable `token`

### 3. Duplicar requests
- Haz clic derecho en un request → **Duplicate**
- Útil para crear variaciones (ej: crear múltiples productos)

### 4. Organizar en carpetas
Crea carpetas en la colección:
- 📁 Autenticación
- 📁 Productos
- 📁 Ventas

### 5. Exportar colección
- Haz clic derecho en la colección → **Export**
- Comparte con tu equipo

---

## 📝 Datos de Prueba Sugeridos

### Productos para crear:

**Producto 1:**
```json
{
  "nombre": "Laptop Dell Inspiron 15",
  "descripcion": "Laptop Dell con procesador Intel Core i7",
  "precio": 15000.00,
  "stock": 10
}
```

**Producto 2:**
```json
{
  "nombre": "Mouse Logitech MX Master 3",
  "descripcion": "Mouse inalámbrico ergonómico",
  "precio": 500.00,
  "stock": 20
}
```

**Producto 3:**
```json
{
  "nombre": "Teclado Mecánico Corsair K95",
  "descripcion": "Teclado mecánico RGB con switches Cherry MX",
  "precio": 1200.00,
  "stock": 15
}
```

**Producto 4:**
```json
{
  "nombre": "Monitor LG 27 pulgadas 4K",
  "descripcion": "Monitor IPS 4K con HDR",
  "precio": 8000.00,
  "stock": 5
}
```

**Producto 5:**
```json
{
  "nombre": "Webcam Logitech C920",
  "descripcion": "Webcam Full HD 1080p",
  "precio": 2500.00,
  "stock": 12
}
```

---

## 🎯 Escenarios de Prueba Completos

### Escenario 1: Venta Simple
1. Login
2. Crear 1 producto
3. Registrar venta de 2 unidades
4. Verificar stock descontado

### Escenario 2: Venta Múltiple
1. Login
2. Crear 3 productos diferentes
3. Registrar venta con los 3 productos
4. Listar ventas
5. Verificar totales

### Escenario 3: Actualización de Inventario
1. Login
2. Crear producto con stock 10
3. Actualizar stock a 20
4. Registrar venta de 5 unidades
5. Verificar stock final (15)

---

## 📦 Próximos Pasos

1. ✅ Importa la colección
2. ✅ Configura el environment
3. ✅ Ejecuta el flujo completo
4. ✅ Experimenta con diferentes datos
5. ✅ Crea tus propios tests personalizados

---

## 🆘 Soporte

Si tienes problemas:
1. Verifica que el servidor esté corriendo
2. Revisa las variables de entorno
3. Consulta los logs del servidor
4. Revisa la consola de Postman (View → Show Postman Console)
