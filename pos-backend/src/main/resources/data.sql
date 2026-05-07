-- Usuario admin inicial
-- Contraseña: admin123 (BCrypt)
INSERT INTO usuario (username, password, nombre, rol, activo, created_at)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'ADMIN', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'admin');
