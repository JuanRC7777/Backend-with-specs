# 📮 Archivos de Postman - Sistema POS

## 📦 Archivos Incluidos

### 1. **POS-API.postman_collection.json**
   - ✅ Colección completa con todos los endpoints
   - ✅ 8 requests pre-configurados
   - ✅ Tests automáticos incluidos
   - ✅ Scripts para guardar variables automáticamente

### 2. **POS-Local.postman_environment.json**
   - ✅ Environment con variables pre-configuradas
   - ✅ `base_url`, `token`, `producto_id`, `venta_id`

### 3. **GUIA_POSTMAN.md**
   - ✅ Guía completa paso a paso
   - ✅ Explicación detallada de cada endpoint
   - ✅ Scripts de automatización
   - ✅ Solución de problemas

### 4. **POSTMAN_QUICKSTART.md**
   - ✅ Inicio rápido en 3 pasos
   - ✅ Flujo de prueba básico
   - ✅ Tips y trucos

---

## 🚀 Inicio Rápido (3 minutos)

### Paso 1: Importar en Postman
```
1. Abre Postman
2. Import → Arrastra "POS-API.postman_collection.json"
3. Import → Arrastra "POS-Local.postman_environment.json"
4. Selecciona "POS Local" en el dropdown de environments
```

### Paso 2: Ejecutar Login
```
1. Abre: Autenticación → Login
2. Click en "Send"
3. ✅ Token guardado automáticamente
```

### Paso 3: Probar Endpoints
```
1. Productos → Crear Producto → Send
2. Productos → Listar Productos → Send
3. Ventas → Registrar Venta → Send
4. Ventas → Listar Ventas → Send
```

---

## 📊 Estructura de la Colección

```
POS API - Sistema Point of Sale
│
├── 📁 Autenticación
│   └── Login (POST)
│
├── 📁 Productos
│   ├── Crear Producto (POST)
│   ├── Listar Productos (GET)
│   ├── Obtener Producto por ID (GET)
│   ├── Actualizar Producto (PUT)
│   └── Eliminar Producto (DELETE)
│
└── 📁 Ventas
    ├── Registrar Venta (POST)
    └── Listar Ventas (GET)
```

---

## ✨ Características Incluidas

### 🤖 Automatización
- ✅ Token JWT se guarda automáticamente después del login
- ✅ IDs de productos y ventas se guardan automáticamente
- ✅ Tests automáticos en cada request
- ✅ Logs detallados en la consola

### 🧪 Tests Incluidos
- ✅ Verificación de códigos de respuesta HTTP
- ✅ Validación de estructura de datos
- ✅ Verificación de tipos de datos
- ✅ Conteo de elementos en arrays
- ✅ Cálculos automáticos (totales, stock, etc.)

### 📝 Scripts Pre-Request
- ✅ Validación de variables requeridas
- ✅ Mensajes de error descriptivos
- ✅ Logs informativos

### 📊 Scripts Post-Response
- ✅ Guardado automático de variables
- ✅ Logs de resultados
- ✅ Validaciones de negocio

---

## 🎯 Ejemplos de Uso

### Ejemplo 1: Flujo Completo Básico
```
1. Login
2. Crear Producto (Laptop)
3. Listar Productos
4. Registrar Venta (2 Laptops)
5. Verificar Stock Descontado
```

### Ejemplo 2: Venta Múltiple
```
1. Login
2. Crear Producto 1 (Laptop)
3. Crear Producto 2 (Mouse)
4. Crear Producto 3 (Teclado)
5. Registrar Venta con los 3 productos
6. Listar Ventas
```

### Ejemplo 3: Actualización de Inventario
```
1. Login
2. Crear Producto (Stock: 10)
3. Actualizar Producto (Stock: 20)
4. Registrar Venta (5 unidades)
5. Verificar Stock Final (15)
```

---

## 🔍 Variables de Entorno

| Variable | Descripción | Se guarda automáticamente |
|----------|-------------|---------------------------|
| `base_url` | URL base de la API | ❌ Manual |
| `token` | Token JWT de autenticación | ✅ Después del login |
| `producto_id` | ID del último producto creado | ✅ Después de crear producto |
| `venta_id` | ID de la última venta | ✅ Después de registrar venta |

---

## 📈 Tests Automáticos

### Login
- ✅ Status code 200
- ✅ Token existe
- ✅ Username es "admin"

### Crear Producto
- ✅ Status code 200/201
- ✅ ID existe
- ✅ Precio es número
- ✅ Stock es número

### Listar Productos
- ✅ Status code 200
- ✅ Respuesta es array
- ✅ Estructura correcta

### Registrar Venta
- ✅ Status code 200/201
- ✅ ID existe
- ✅ Total existe
- ✅ Detalles es array

---

## 🎨 Personalización

### Cambiar URL del servidor
```
Environments → POS Local → base_url → Cambiar valor
```

### Agregar nuevos productos
```
Productos → Crear Producto → Body → Modificar JSON
```

### Venta con múltiples productos
```json
{
  "items": [
    {"productoId": 1, "cantidad": 2},
    {"productoId": 2, "cantidad": 3},
    {"productoId": 3, "cantidad": 1}
  ]
}
```

---

## 🐛 Troubleshooting

### Error: Connection refused
```
✅ Verifica que el servidor esté corriendo
✅ Verifica el puerto (8081)
✅ Verifica la URL en base_url
```

### Error: 401 Unauthorized
```
✅ Ejecuta Login nuevamente
✅ Verifica que el token esté guardado
✅ Revisa la variable "token" en el environment
```

### Error: 404 Not Found
```
✅ Verifica que producto_id tenga valor
✅ Ejecuta "Crear Producto" primero
✅ Revisa las variables en el environment
```

---

## 📚 Documentación Adicional

- **GUIA_POSTMAN.md** - Guía completa y detallada
- **POSTMAN_QUICKSTART.md** - Inicio rápido
- **GUIA_PRUEBAS.md** - Guía general de pruebas
- **PRUEBAS_RAPIDAS.md** - Comandos curl rápidos

---

## 🎓 Recursos de Aprendizaje

### Postman Básico
1. Importar colecciones
2. Usar variables de entorno
3. Ejecutar requests
4. Ver respuestas

### Postman Intermedio
1. Escribir tests
2. Usar scripts pre-request
3. Ejecutar colecciones completas
4. Exportar/compartir colecciones

### Postman Avanzado
1. Variables dinámicas
2. Monitors automáticos
3. Integración con Newman (CLI)
4. CI/CD con Postman

---

## ✅ Checklist de Verificación

- [ ] Postman instalado
- [ ] Colección importada
- [ ] Environment importado
- [ ] Environment activado
- [ ] Servidor corriendo (puerto 8081)
- [ ] Login ejecutado
- [ ] Token guardado
- [ ] Producto creado
- [ ] Venta registrada
- [ ] Todos los tests pasan

---

## 🚀 Próximos Pasos

1. ✅ Importa la colección y el environment
2. ✅ Ejecuta el flujo básico
3. ✅ Experimenta con diferentes datos
4. ✅ Revisa los tests automáticos
5. ✅ Personaliza según tus necesidades
6. ✅ Comparte con tu equipo

---

## 💡 Tips Pro

### 1. Collection Runner
- Ejecuta toda la colección de una vez
- Útil para regression testing
- Genera reportes automáticos

### 2. Postman Console
- View → Show Postman Console
- Ver logs detallados
- Debugging de scripts

### 3. Code Snippets
- Click en "Code" (</> icono)
- Copia código en diferentes lenguajes
- Útil para integración

### 4. Documentación Automática
- Genera documentación de la API
- Comparte con el equipo
- Mantiene actualizada automáticamente

---

## 🆘 Soporte

¿Problemas? Revisa:
1. Consola de Postman (View → Show Postman Console)
2. Logs del servidor Spring Boot
3. Variables de entorno (icono 👁️)
4. Documentación completa en GUIA_POSTMAN.md

---

¡Feliz testing con Postman! 🎉
