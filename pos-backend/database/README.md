# Scripts de Base de Datos - Sistema POS

## 📋 Descripción

Scripts SQL para crear y poblar la base de datos del Sistema POS v3.2.0 con arquitectura hexagonal.

## 📂 Archivos

| Archivo | Descripción |
|---------|-------------|
| `01_create_database.sql` | Crea la base de datos, tablas, índices y configuración inicial |
| `02_insert_test_data.sql` | Inserta datos de prueba (usuarios, productos, ventas) |

## 🚀 Instrucciones de Instalación

### Opción 1: Desde MySQL Workbench

1. Abre MySQL Workbench
2. Conecta a tu servidor MySQL/MariaDB
3. Abre el archivo `01_create_database.sql`
4. Ejecuta el script completo (⚡ Execute)
5. Abre el archivo `02_insert_test_data.sql`
6. Ejecuta el script completo (⚡ Execute)

### Opción 2: Desde línea de comandos

```bash
# Crear base de datos y tablas
mysql -u root -p < 01_create_database.sql

# Insertar datos de prueba
mysql -u root -p < 02_insert_test_data.sql
```

### Opción 3: Desde MySQL CLI

```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar scripts
source C:/Users/mMJua/OneDrive/Desktop/kiro/backend/pos-backend/database/01_create_database.sql
source C:/Users/mMJua/OneDrive/Desktop/kiro/backend/pos-backend/database/02_insert_test_data.sql
```

## 📊 Estructura de la Base de Datos

### Tablas Principales

1. **usuario** - Usuarios del sistema con autenticación JWT
2. **producto** - Catálogo de productos disponibles
3. **venta** - Registro de ventas con facturación
4. **detalle_venta** - Líneas de detalle de cada venta
5. **pago_venta** - Múltiples métodos de pago por venta (v3.1.0)
6. **reembolso** - Sistema de reembolsos (v3.1.0)
7. **configuracion** - Configuración global del sistema (v3.1.0)
8. **secuencia_factura** - Control de secuencia de facturas (v3.2.0)

### Diagrama de Relaciones

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
SecuenciaFactura (tabla global)
```

## 👤 Usuarios de Prueba

| Username | Password | Rol | Nombre |
|----------|----------|-----|--------|
| admin | admin123 | ADMIN | Administrador del Sistema |
| jperez | admin123 | ADMIN | Juan Pérez |
| mgarcia | admin123 | ADMIN | María García |

**Nota**: Las contraseñas están hasheadas con BCrypt.

## 🛍️ Productos de Prueba

El script incluye **27 productos** en las siguientes categorías:
- Bebidas (5 productos)
- Alimentos (5 productos)
- Lácteos (4 productos)
- Panadería (3 productos)
- Snacks (3 productos)
- Limpieza (4 productos)
- Cuidado Personal (3 productos)

## 💰 Ventas de Prueba

El script incluye **5 ventas de ejemplo**:

1. **FAC-20260507-000001** - Pago único en efectivo ($36,750)
2. **FAC-20260507-000002** - Múltiples métodos de pago ($52,500)
3. **FAC-20260507-000003** - Pago con transferencia ($29,400)
4. **FAC-20260507-000004** - Venta grande ($99,750)
5. **FAC-20260507-000005** - Venta reembolsada ($21,000)

## ⚙️ Configuración Inicial

- **Tasa de impuesto**: 5% (0.05)
- **Charset**: utf8mb4
- **Collation**: utf8mb4_unicode_ci
- **Engine**: InnoDB

## 🔍 Verificación

Después de ejecutar los scripts, verifica que todo esté correcto:

```sql
USE pos_db;

-- Ver todas las tablas
SHOW TABLES;

-- Ver resumen de datos
SELECT 'Usuarios' AS Tabla, COUNT(*) AS Total FROM usuario
UNION ALL SELECT 'Productos', COUNT(*) FROM producto
UNION ALL SELECT 'Ventas', COUNT(*) FROM venta
UNION ALL SELECT 'Detalles', COUNT(*) FROM detalle_venta
UNION ALL SELECT 'Pagos', COUNT(*) FROM pago_venta
UNION ALL SELECT 'Reembolsos', COUNT(*) FROM reembolso;

-- Ver ventas con sus pagos
SELECT 
    v.numero_factura,
    v.nombre_cliente,
    v.total,
    GROUP_CONCAT(pv.metodo_pago SEPARATOR ', ') AS metodos_pago
FROM venta v
LEFT JOIN pago_venta pv ON v.id = pv.venta_id
GROUP BY v.id;
```

## 🔄 Actualización desde Versión Anterior

Si ya tienes una base de datos `pos_db` de una versión anterior, el script:
- ✅ Usa `CREATE TABLE IF NOT EXISTS` (no sobrescribe tablas existentes)
- ✅ Usa `ON DUPLICATE KEY UPDATE` para datos iniciales
- ✅ Mantiene tus datos existentes

## 🗑️ Eliminar Base de Datos

**⚠️ CUIDADO: Esto eliminará TODOS los datos**

```sql
DROP DATABASE IF EXISTS pos_db;
```

## 📝 Notas Importantes

1. **Formato de Factura**: FAC-YYYYMMDD-NNNNNN (6 dígitos, hasta 999,999 por día)
2. **Cédula**: Exactamente 10 dígitos numéricos
3. **Nombre Cliente**: Mínimo 2 palabras (nombre y apellido)
4. **Métodos de Pago**: EFECTIVO, TARJETA, TRANSFERENCIA
5. **Reembolsos**: Solo totales (no parciales en v3.2.0)
6. **Redondeo**: ROUND_HALF_UP a 2 decimales en todos los cálculos monetarios

## 🆕 Cambios en v3.2.0

- ✅ Ampliado formato de factura a 6 dígitos (999,999 facturas/día)
- ✅ Agregadas clarificaciones de ambigüedades
- ✅ Mejoradas validaciones y constraints
- ✅ Optimizados índices para consultas

## 📞 Soporte

Para más información, consulta:
- `requirements.md` - Especificación de requerimientos
- `design.md` - Diseño técnico
- `tasks.md` - Plan de implementación
