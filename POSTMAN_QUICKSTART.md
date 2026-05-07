# 🚀 Postman Quick Start - Sistema POS

## ⚡ Importación Rápida (3 pasos)

### Paso 1: Importar Colección
1. Abre Postman
2. Haz clic en **Import** (esquina superior izquierda)
3. Arrastra el archivo `POS-API.postman_collection.json`
4. Haz clic en **Import**

### Paso 2: Importar Environment
1. Haz clic en **Import** nuevamente
2. Arrastra el archivo `POS-Local.postman_environment.json`
3. Haz clic en **Import**

### Paso 3: Activar Environment
1. En la esquina superior derecha, selecciona **POS Local** del dropdown
2. ¡Listo! Ya puedes empezar a probar

---

## 🎯 Flujo de Prueba Rápido

### 1. Login (OBLIGATORIO - Ejecutar primero)
- Carpeta: **Autenticación** → **Login**
- Haz clic en **Send**
- ✅ El token se guarda automáticamente

### 2. Crear Producto
- Carpeta: **Productos** → **Crear Producto**
- Haz clic en **Send**
- ✅ El ID del producto se guarda automáticamente

### 3. Listar Productos
- Carpeta: **Productos** → **Listar Productos**
- Haz clic en **Send**
- ✅ Verás todos los productos creados

### 4. Registrar Venta
- Carpeta: **Ventas** → **Registrar Venta**
- Haz clic en **Send**
- ✅ Se registra la venta y descuenta el stock

### 5. Listar Ventas
- Carpeta: **Ventas** → **Listar Ventas**
- Haz clic en **Send**
- ✅ Verás el historial de ventas

---

## 🤖 Ejecutar Toda la Colección

1. Haz clic derecho en **POS API - Sistema Point of Sale**
2. Selecciona **Run collection**
3. Haz clic en **Run POS API**
4. ✅ Se ejecutarán todas las pruebas automáticamente

---

## 📊 Ver Resultados de Tests

Después de cada request:
1. Ve a la pestaña **Test Results**
2. Verás los tests que pasaron ✅ o fallaron ❌
3. En la **Console** (View → Show Postman Console) verás logs detallados

---

## 🔍 Variables Automáticas

Estas variables se guardan automáticamente:
- `token` - Token JWT del login
- `producto_id` - ID del último producto creado
- `venta_id` - ID de la última venta registrada

Para verlas:
1. Haz clic en el icono de ojo 👁️ (esquina superior derecha)
2. Verás todas las variables y sus valores

---

## 📝 Modificar Datos de Prueba

### Crear diferentes productos:

**Ejemplo 1: Mouse**
```json
{
  "nombre": "Mouse Logitech MX Master 3",
  "descripcion": "Mouse inalámbrico ergonómico",
  "precio": 500.00,
  "stock": 20
}
```

**Ejemplo 2: Teclado**
```json
{
  "nombre": "Teclado Mecánico Corsair K95",
  "descripcion": "Teclado mecánico RGB",
  "precio": 1200.00,
  "stock": 15
}
```

**Ejemplo 3: Monitor**
```json
{
  "nombre": "Monitor LG 27 pulgadas 4K",
  "descripcion": "Monitor IPS 4K con HDR",
  "precio": 8000.00,
  "stock": 5
}
```

### Venta con múltiples productos:
```json
{
  "items": [
    {
      "productoId": 1,
      "cantidad": 2
    },
    {
      "productoId": 2,
      "cantidad": 3
    }
  ]
}
```

---

## 🐛 Solución de Problemas

### ❌ Error: "Could not send request"
- **Solución:** Verifica que el servidor esté corriendo en `http://localhost:8081`

### ❌ Error: 401 Unauthorized
- **Solución:** Ejecuta nuevamente el request de **Login**

### ❌ Error: 404 Not Found
- **Solución:** Verifica que `producto_id` tenga un valor válido
- Ve a: Environments → POS Local → Verifica las variables

### ❌ Tests fallan
- **Solución:** Revisa la consola de Postman (View → Show Postman Console)
- Verifica los logs del servidor Spring Boot

---

## 💡 Tips Útiles

### 1. Duplicar Requests
- Haz clic derecho en un request → **Duplicate**
- Útil para crear variaciones

### 2. Guardar Respuestas
- Haz clic en **Save Response** → **Save as example**
- Útil para documentación

### 3. Compartir Colección
- Haz clic derecho en la colección → **Export**
- Comparte el archivo JSON con tu equipo

### 4. Ver Código
- Haz clic en **Code** (</> icono a la derecha)
- Copia el código en diferentes lenguajes (curl, JavaScript, Python, etc.)

---

## 📚 Documentación Completa

Para más detalles, consulta:
- `GUIA_POSTMAN.md` - Guía completa con todos los detalles
- `GUIA_PRUEBAS.md` - Guía general de pruebas
- `PRUEBAS_RAPIDAS.md` - Comandos rápidos con curl

---

## ✅ Checklist de Verificación

- [ ] Colección importada
- [ ] Environment importado y activado
- [ ] Servidor corriendo en puerto 8081
- [ ] Login ejecutado exitosamente
- [ ] Token guardado en variables
- [ ] Producto creado
- [ ] Venta registrada
- [ ] Todos los tests pasan ✅

---

## 🎓 Siguiente Nivel

Una vez que domines lo básico:
1. Crea tus propios tests personalizados
2. Usa variables dinámicas ({{$randomInt}}, {{$timestamp}})
3. Configura Pre-request Scripts para datos dinámicos
4. Crea Monitors para ejecutar tests automáticamente
5. Integra con Newman para CI/CD

---

## 🆘 ¿Necesitas Ayuda?

1. Revisa la consola de Postman
2. Verifica los logs del servidor
3. Consulta `GUIA_POSTMAN.md` para detalles
4. Verifica que todas las variables estén configuradas

¡Feliz testing! 🚀
