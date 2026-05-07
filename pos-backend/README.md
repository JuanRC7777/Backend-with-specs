# POS Backend — Sistema Point of Sale

API REST para gestión de inventario y ventas, construida con **Spring Boot 3.x**, **Java 21** y **arquitectura hexagonal (Ports & Adapters)**.

---

## Requisitos

- Java 21
- Maven 3.x
- MySQL 8.x / MariaDB 10.x

---

## Configuración local

### 1. Base de datos

Crea la base de datos en MySQL:

```sql
CREATE DATABASE pos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Variables de entorno (opcional para desarrollo)

Edita `src/main/resources/application.properties` con tus credenciales de base de datos, o define las variables de entorno para producción:

| Variable       | Descripción                        |
|----------------|------------------------------------|
| `DB_URL`       | URL JDBC de la base de datos       |
| `DB_USERNAME`  | Usuario de la base de datos        |
| `DB_PASSWORD`  | Contraseña de la base de datos     |
| `JWT_SECRET`   | Clave secreta para firmar JWT      |

### 3. Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Compilar y ejecutar el JAR

```bash
./mvnw clean package -DskipTests
java -jar target/pos-backend-1.0.0-SNAPSHOT.jar
```

Para producción con variables de entorno:

```bash
export DB_URL=jdbc:mysql://localhost:3306/pos_db
export DB_USERNAME=root
export DB_PASSWORD=secreto
export JWT_SECRET=miClaveSecretaSegura
java -jar target/pos-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Docker

### Build de la imagen

```bash
docker build -t pos-backend:latest .
```

### Ejecutar el contenedor

```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/pos_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secreto \
  -e JWT_SECRET=miClaveSecretaSegura \
  pos-backend:latest
```

---

## Ejecutar tests

```bash
./mvnw test
```

---

## Endpoints principales

### Autenticación

| Método | Endpoint           | Descripción              | Auth |
|--------|--------------------|--------------------------|------|
| POST   | `/api/auth/login`  | Login, retorna JWT       | No   |

**Body:**
```json
{ "username": "admin", "password": "admin123" }
```

### Productos

| Método | Endpoint               | Descripción              | Auth |
|--------|------------------------|--------------------------|------|
| GET    | `/api/productos`       | Listar productos activos | JWT  |
| GET    | `/api/productos/{id}`  | Obtener producto por ID  | JWT  |
| POST   | `/api/productos`       | Crear producto           | JWT  |
| PUT    | `/api/productos/{id}`  | Actualizar producto      | JWT  |
| DELETE | `/api/productos/{id}`  | Eliminar producto        | JWT  |

### Ventas

| Método | Endpoint       | Descripción          | Auth |
|--------|----------------|----------------------|------|
| POST   | `/api/ventas`  | Registrar venta      | JWT  |
| GET    | `/api/ventas`  | Listar ventas        | JWT  |

---

## Usuario inicial

| Campo    | Valor      |
|----------|------------|
| username | `admin`    |
| password | `admin123` |
| rol      | `ADMIN`    |

---

## Arquitectura

```
domain/          → Entidades y reglas de negocio puras (sin frameworks)
application/     → Casos de uso (ports + services)
infrastructure/  → Adapters: REST controllers, JPA repositories, JWT, Security
```
