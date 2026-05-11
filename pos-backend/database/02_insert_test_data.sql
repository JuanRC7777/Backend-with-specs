-- =============================================
-- Script de Datos de Prueba
-- Sistema POS - Point of Sale con Facturación
-- Versión: 3.2.0
-- Fecha: 2026-05-08
-- =============================================

USE pos_db;

-- =============================================
-- USUARIOS DE PRUEBA
-- =============================================
-- Contraseña para todos: admin123
INSERT INTO usuario (username, password, nombre, rol, activo) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador del Sistema', 'ADMIN', TRUE),
('jperez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Juan Pérez', 'ADMIN', TRUE),
('mgarcia', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'María García', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- =============================================
-- PRODUCTOS DE PRUEBA
-- =============================================
INSERT INTO producto (nombre, descripcion, precio, stock, activo) VALUES
-- Bebidas
('Café Premium 500g', 'Café colombiano de alta calidad, tostado medio', 15000.00, 50, TRUE),
('Té Verde Orgánico', 'Té verde orgánico importado, caja de 25 sobres', 8500.00, 30, TRUE),
('Jugo de Naranja 1L', 'Jugo natural de naranja sin azúcar añadida', 4500.00, 100, TRUE),
('Agua Mineral 600ml', 'Agua mineral natural sin gas', 1500.00, 200, TRUE),
('Gaseosa Cola 2L', 'Bebida gaseosa sabor cola', 3500.00, 80, TRUE),

-- Alimentos
('Azúcar 1kg', 'Azúcar blanca refinada', 5000.00, 60, TRUE),
('Arroz Blanco 5kg', 'Arroz blanco de primera calidad', 18000.00, 40, TRUE),
('Aceite de Oliva 500ml', 'Aceite de oliva extra virgen', 22000.00, 25, TRUE),
('Pasta Spaghetti 500g', 'Pasta italiana de trigo duro', 4200.00, 70, TRUE),
('Sal Marina 500g', 'Sal marina natural sin refinar', 2800.00, 90, TRUE),

-- Lácteos
('Leche Entera 1L', 'Leche entera pasteurizada', 3800.00, 120, TRUE),
('Queso Mozzarella 250g', 'Queso mozzarella para pizza', 8900.00, 35, TRUE),
('Yogurt Natural 1L', 'Yogurt natural sin azúcar', 6500.00, 45, TRUE),
('Mantequilla 250g', 'Mantequilla sin sal', 7200.00, 50, TRUE),

-- Panadería
('Pan Integral 500g', 'Pan integral de trigo', 4500.00, 80, TRUE),
('Galletas de Avena 300g', 'Galletas de avena con pasas', 5800.00, 60, TRUE),
('Croissant x6', 'Croissants de mantequilla', 9500.00, 40, TRUE),

-- Snacks
('Papas Fritas 150g', 'Papas fritas sabor natural', 3200.00, 100, TRUE),
('Chocolate con Leche 100g', 'Chocolate con leche premium', 4800.00, 75, TRUE),
('Maní Salado 200g', 'Maní tostado y salado', 3500.00, 85, TRUE),

-- Limpieza
('Detergente Líquido 1L', 'Detergente líquido para ropa', 12000.00, 50, TRUE),
('Jabón de Manos 500ml', 'Jabón líquido antibacterial', 6800.00, 70, TRUE),
('Papel Higiénico x4', 'Papel higiénico triple hoja', 8500.00, 90, TRUE),
('Desinfectante 1L', 'Desinfectante multiusos', 9200.00, 55, TRUE),

-- Cuidado Personal
('Shampoo 400ml', 'Shampoo para todo tipo de cabello', 15500.00, 40, TRUE),
('Crema Dental 150g', 'Crema dental con flúor', 7800.00, 65, TRUE),
('Desodorante Roll-On', 'Desodorante antitranspirante', 8900.00, 50, TRUE),

-- Productos inactivos (para probar reembolsos)
('Producto Descontinuado', 'Este producto ya no se vende', 10000.00, 10, FALSE)
ON DUPLICATE KEY UPDATE nombre = nombre;

-- =============================================
-- VENTAS DE PRUEBA
-- =============================================

-- Venta 1: Pago único en efectivo
INSERT INTO venta (numero_factura, usuario_id, nombre_cajero, nombre_cliente, cedula_cliente, subtotal, tasa_impuesto, impuesto, total, fecha, reembolsada)
VALUES ('FAC-20260507-000001', 1, 'admin', 'María González Pérez', '1234567890', 35000.00, 0.05, 1750.00, 36750.00, '2026-05-07 10:30:00', FALSE);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unit, subtotal) VALUES
(1, 1, 2, 15000.00, 30000.00),  -- 2 Café Premium
(1, 6, 1, 5000.00, 5000.00);     -- 1 Azúcar

INSERT INTO pago_venta (venta_id, metodo_pago, monto) VALUES
(1, 'EFECTIVO', 36750.00);

-- Venta 2: Múltiples métodos de pago
INSERT INTO venta (numero_factura, usuario_id, nombre_cajero, nombre_cliente, cedula_cliente, subtotal, tasa_impuesto, impuesto, total, fecha, reembolsada)
VALUES ('FAC-20260507-000002', 2, 'jperez', 'Carlos Rodríguez López', '0987654321', 50000.00, 0.05, 2500.00, 52500.00, '2026-05-07 11:45:00', FALSE);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unit, subtotal) VALUES
(2, 7, 1, 18000.00, 18000.00),   -- 1 Arroz 5kg
(2, 8, 1, 22000.00, 22000.00),   -- 1 Aceite de Oliva
(2, 11, 2, 3800.00, 7600.00),    -- 2 Leche
(2, 15, 1, 4500.00, 4500.00);    -- 1 Pan Integral

INSERT INTO pago_venta (venta_id, metodo_pago, monto) VALUES
(2, 'EFECTIVO', 30000.00),
(2, 'TARJETA', 22500.00);

-- Venta 3: Pago con transferencia
INSERT INTO venta (numero_factura, usuario_id, nombre_cajero, nombre_cliente, cedula_cliente, subtotal, tasa_impuesto, impuesto, total, fecha, reembolsada)
VALUES ('FAC-20260507-000003', 3, 'mgarcia', 'Ana Martínez Silva', '1122334455', 28000.00, 0.05, 1400.00, 29400.00, '2026-05-07 14:20:00', FALSE);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unit, subtotal) VALUES
(3, 12, 2, 8900.00, 17800.00),   -- 2 Queso Mozzarella
(3, 13, 1, 6500.00, 6500.00),    -- 1 Yogurt
(3, 9, 1, 4200.00, 4200.00);     -- 1 Pasta

INSERT INTO pago_venta (venta_id, metodo_pago, monto) VALUES
(3, 'TRANSFERENCIA', 29400.00);

-- Venta 4: Venta grande con múltiples productos
INSERT INTO venta (numero_factura, usuario_id, nombre_cajero, nombre_cliente, cedula_cliente, subtotal, tasa_impuesto, impuesto, total, fecha, reembolsada)
VALUES ('FAC-20260507-000004', 1, 'admin', 'Pedro Sánchez Gómez', '5566778899', 95000.00, 0.05, 4750.00, 99750.00, '2026-05-07 16:00:00', FALSE);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unit, subtotal) VALUES
(4, 1, 3, 15000.00, 45000.00),   -- 3 Café Premium
(4, 7, 2, 18000.00, 36000.00),   -- 2 Arroz 5kg
(4, 21, 1, 12000.00, 12000.00),  -- 1 Detergente
(4, 4, 2, 1500.00, 3000.00);     -- 2 Agua Mineral

INSERT INTO pago_venta (venta_id, metodo_pago, monto) VALUES
(4, 'EFECTIVO', 50000.00),
(4, 'TARJETA', 49750.00);

-- Venta 5: Venta reembolsada
INSERT INTO venta (numero_factura, usuario_id, nombre_cajero, nombre_cliente, cedula_cliente, subtotal, tasa_impuesto, impuesto, total, fecha, reembolsada)
VALUES ('FAC-20260507-000005', 2, 'jperez', 'Laura Fernández Castro', '9988776655', 20000.00, 0.05, 1000.00, 21000.00, '2026-05-07 17:30:00', TRUE);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unit, subtotal) VALUES
(5, 18, 2, 3200.00, 6400.00),    -- 2 Papas Fritas
(5, 19, 2, 4800.00, 9600.00),    -- 2 Chocolate
(5, 20, 1, 3500.00, 3500.00);    -- 1 Maní

INSERT INTO pago_venta (venta_id, metodo_pago, monto) VALUES
(5, 'EFECTIVO', 21000.00);

INSERT INTO reembolso (venta_id, motivo, fecha, usuario_id, nombre_usuario) VALUES
(5, 'Cliente insatisfecho con la calidad del producto. Solicitó devolución completa.', '2026-05-07 18:00:00', 1, 'admin');

-- =============================================
-- SECUENCIA DE FACTURAS
-- =============================================
INSERT INTO secuencia_factura (fecha, ultimo_numero) VALUES
('2026-05-07', 5)
ON DUPLICATE KEY UPDATE ultimo_numero = 5;

-- =============================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- =============================================
SELECT 'Usuarios' AS Tabla, COUNT(*) AS Total FROM usuario
UNION ALL
SELECT 'Productos', COUNT(*) FROM producto
UNION ALL
SELECT 'Ventas', COUNT(*) FROM venta
UNION ALL
SELECT 'Detalles de Venta', COUNT(*) FROM detalle_venta
UNION ALL
SELECT 'Pagos', COUNT(*) FROM pago_venta
UNION ALL
SELECT 'Reembolsos', COUNT(*) FROM reembolso
UNION ALL
SELECT 'Configuración', COUNT(*) FROM configuracion
UNION ALL
SELECT 'Secuencias de Factura', COUNT(*) FROM secuencia_factura;

-- Mostrar resumen de ventas
SELECT 
    v.numero_factura AS 'Número Factura',
    v.nombre_cliente AS 'Cliente',
    v.total AS 'Total',
    v.fecha AS 'Fecha',
    v.reembolsada AS 'Reembolsada',
    GROUP_CONCAT(pv.metodo_pago SEPARATOR ', ') AS 'Métodos de Pago'
FROM venta v
LEFT JOIN pago_venta pv ON v.id = pv.venta_id
GROUP BY v.id
ORDER BY v.fecha DESC;
