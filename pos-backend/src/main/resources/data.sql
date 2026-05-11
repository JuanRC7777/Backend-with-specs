-- =============================================
-- Datos iniciales - Sistema POS
-- =============================================

-- Configuración inicial: tasa de impuesto 5%
-- El usuario admin es creado por DataInitializer al arrancar la aplicación
INSERT INTO configuracion (clave, valor, created_at, updated_at)
SELECT 'tasa_impuesto', '0.05', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM configuracion WHERE clave = 'tasa_impuesto');
