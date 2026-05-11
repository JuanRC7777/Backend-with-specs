-- =============================================
-- Script de Creación de Base de Datos
-- Sistema POS - Point of Sale con Facturación
-- Versión: 3.2.0
-- Fecha: 2026-05-08
-- =============================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS pos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pos_db;

-- =============================================
-- TABLA: usuario
-- Descripción: Usuarios del sistema con autenticación
-- =============================================
CREATE TABLE IF NOT EXISTS usuario (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt de la contraseña',
    nombre     VARCHAR(100) NOT NULL,
    rol        VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT 'Rol del usuario (ADMIN en MVP)',
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Usuarios del sistema con autenticación JWT';

-- =============================================
-- TABLA: producto
-- Descripción: Catálogo de productos disponibles
-- =============================================
CREATE TABLE IF NOT EXISTS producto (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)   NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10, 2) NOT NULL COMMENT 'Precio unitario del producto',
    stock       INT            NOT NULL DEFAULT 0 COMMENT 'Cantidad disponible en inventario',
    activo      BOOLEAN        NOT NULL DEFAULT TRUE COMMENT 'Indica si el producto está activo',
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (activo),
    INDEX idx_nombre (nombre),
    CONSTRAINT chk_precio_positivo CHECK (precio > 0),
    CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Catálogo de productos del sistema POS';

-- =============================================
-- TABLA: venta
-- Descripción: Registro de ventas con facturación
-- =============================================
CREATE TABLE IF NOT EXISTS venta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_factura  VARCHAR(50)    NOT NULL UNIQUE COMMENT 'Formato: FAC-YYYYMMDD-NNNNNN (6 dígitos)',
    usuario_id      BIGINT         NOT NULL COMMENT 'Usuario que registra la venta (cajero)',
    nombre_cajero   VARCHAR(100)   NOT NULL COMMENT 'Username del cajero desde JWT',
    nombre_cliente  VARCHAR(100)   NOT NULL COMMENT 'Nombre completo del cliente (mínimo 2 palabras)',
    cedula_cliente  VARCHAR(10)    NOT NULL COMMENT 'Cédula del cliente (exactamente 10 dígitos)',
    subtotal        DECIMAL(10, 2) NOT NULL COMMENT 'Total sin impuesto',
    tasa_impuesto   DECIMAL(5, 4)  NOT NULL COMMENT 'Porcentaje de impuesto (ej: 0.05 = 5%)',
    impuesto        DECIMAL(10, 2) NOT NULL COMMENT 'Monto del impuesto calculado',
    total           DECIMAL(10, 2) NOT NULL COMMENT 'Total final (subtotal + impuesto)',
    fecha           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de confirmación de la venta',
    reembolsada     BOOLEAN        NOT NULL DEFAULT FALSE COMMENT 'Indica si la venta fue reembolsada',
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha (fecha),
    INDEX idx_cedula_cliente (cedula_cliente),
    INDEX idx_cajero (usuario_id),
    INDEX idx_reembolsada (reembolsada),
    CONSTRAINT chk_cedula_formato CHECK (cedula_cliente REGEXP '^[0-9]{10}$'),
    CONSTRAINT chk_subtotal_positivo CHECK (subtotal >= 0),
    CONSTRAINT chk_impuesto_no_negativo CHECK (impuesto >= 0),
    CONSTRAINT chk_total_positivo CHECK (total > 0),
    CONSTRAINT chk_tasa_impuesto_rango CHECK (tasa_impuesto >= 0.0 AND tasa_impuesto <= 1.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de ventas con facturación completa';

-- =============================================
-- TABLA: detalle_venta
-- Descripción: Líneas de detalle de cada venta
-- =============================================
CREATE TABLE IF NOT EXISTS detalle_venta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id    BIGINT         NOT NULL,
    producto_id BIGINT         NOT NULL,
    cantidad    INT            NOT NULL COMMENT 'Cantidad vendida del producto',
    precio_unit DECIMAL(10, 2) NOT NULL COMMENT 'Precio unitario al momento de la venta',
    subtotal    DECIMAL(10, 2) NOT NULL COMMENT 'Subtotal de la línea (precio_unit × cantidad)',
    FOREIGN KEY (venta_id)    REFERENCES venta(id) ON DELETE RESTRICT,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT,
    INDEX idx_venta (venta_id),
    INDEX idx_producto (producto_id),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_precio_unit_positivo CHECK (precio_unit > 0),
    CONSTRAINT chk_subtotal_positivo CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Detalle de productos vendidos en cada venta';

-- =============================================
-- TABLA: pago_venta
-- Descripción: Múltiples métodos de pago por venta
-- =============================================
CREATE TABLE IF NOT EXISTS pago_venta (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id      BIGINT         NOT NULL,
    metodo_pago   VARCHAR(20)    NOT NULL COMMENT 'EFECTIVO, TARJETA o TRANSFERENCIA',
    monto         DECIMAL(10, 2) NOT NULL COMMENT 'Monto pagado con este método',
    FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE RESTRICT,
    INDEX idx_venta (venta_id),
    INDEX idx_metodo_pago (metodo_pago),
    CONSTRAINT chk_metodo_pago CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    CONSTRAINT chk_monto_positivo CHECK (monto > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Múltiples métodos de pago por venta (v3.1.0)';

-- =============================================
-- TABLA: reembolso
-- Descripción: Sistema de reembolsos de ventas
-- =============================================
CREATE TABLE IF NOT EXISTS reembolso (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id        BIGINT       NOT NULL COMMENT 'Venta que se reembolsa',
    motivo          TEXT         NOT NULL COMMENT 'Motivo del reembolso (10-500 caracteres)',
    fecha           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha del reembolso',
    usuario_id      BIGINT       NOT NULL COMMENT 'Usuario que autoriza el reembolso',
    nombre_usuario  VARCHAR(100) NOT NULL COMMENT 'Username del usuario que autoriza',
    FOREIGN KEY (venta_id)    REFERENCES venta(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id)  REFERENCES usuario(id) ON DELETE RESTRICT,
    INDEX idx_venta (venta_id),
    INDEX idx_fecha (fecha),
    INDEX idx_usuario (usuario_id),
    CONSTRAINT uq_venta_reembolso UNIQUE (venta_id) COMMENT 'Una venta solo puede reembolsarse una vez'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Sistema de reembolsos con devolución de stock (v3.1.0)';

-- =============================================
-- TABLA: configuracion
-- Descripción: Configuración global del sistema
-- =============================================
CREATE TABLE IF NOT EXISTS configuracion (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave      VARCHAR(100) NOT NULL UNIQUE COMMENT 'Clave de configuración (ej: tasa_impuesto)',
    valor      VARCHAR(255) NOT NULL COMMENT 'Valor de la configuración',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_clave (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Configuración global del sistema (v3.1.0)';

-- =============================================
-- TABLA: secuencia_factura
-- Descripción: Control de secuencia de números de factura
-- =============================================
CREATE TABLE IF NOT EXISTS secuencia_factura (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha           DATE           NOT NULL UNIQUE COMMENT 'Fecha para la secuencia (reinicia diariamente)',
    ultimo_numero   INT            NOT NULL DEFAULT 0 COMMENT 'Último número de secuencia usado (0-999999)',
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fecha (fecha),
    CONSTRAINT chk_ultimo_numero CHECK (ultimo_numero >= 0 AND ultimo_numero <= 999999) COMMENT 'Límite de 999,999 facturas por día'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Control de secuencia de facturas con formato FAC-YYYYMMDD-NNNNNN (v3.2.0)';

-- =============================================
-- DATOS INICIALES
-- =============================================

-- Insertar tasa de impuesto predefinida (5%)
INSERT INTO configuracion (clave, valor) 
VALUES ('tasa_impuesto', '0.05')
ON DUPLICATE KEY UPDATE valor = '0.05';

-- Insertar usuario administrador por defecto
-- Contraseña: admin123 (hash BCrypt)
INSERT INTO usuario (username, password, nombre, rol, activo)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador del Sistema', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- =============================================
-- VERIFICACIÓN DE TABLAS CREADAS
-- =============================================
SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Filas',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Tamaño (MB)',
    TABLE_COMMENT AS 'Descripción'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'pos_db'
ORDER BY TABLE_NAME;
