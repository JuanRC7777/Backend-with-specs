-- Usuario admin inicial
-- Contraseña: admin123 (BCrypt)
INSERT IGNORE INTO usuario (username, password, nombre, rol, activo)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'ADMIN', true);
