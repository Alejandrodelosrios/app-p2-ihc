# Backend IHC — Mascotas al Día

Backend de autenticación para **Mascotas al Día** con Java 25, Spring Boot 4.1.1, Gradle 9.3.0 y PostgreSQL 17.

## Requisitos

- JDK 25.
- Docker Desktop + Docker Compose v2.
- Conexión a Internet la primera vez que Gradle descarga dependencias.

## Arranque

```powershell
cd backend
Copy-Item .env.example .env   # si todavía no existe

docker compose up -d
.\gradlew.bat bootRun
```

El backend escucha en `http://localhost:8080` y PostgreSQL en `127.0.0.1:5432` por defecto.

## Endpoints

| Método | Ruta | Auth | Descripción |
| --- | --- | --- | --- |
| `GET` | `/api/health` | No | Comprueba la conexión real con PostgreSQL. |
| `POST` | `/api/auth/register` | No | Crea una cuenta y devuelve un JWT. |
| `POST` | `/api/auth/login` | No | Valida credenciales y devuelve un JWT. |
| `GET` | `/api/auth/me` | Sí | Devuelve el usuario asociado al JWT. |
| `GET` | `/actuator/health` | No | Health check de Spring Boot. |

Todas las demás rutas bajo `/api/**` requieren un `Authorization: Bearer <token>` válido.

### Registro

```json
{
  "fullName": "Ana Pérez",
  "email": "ana@example.com",
  "password": "password123"
}
```

### Login

```json
{
  "email": "ana@example.com",
  "password": "password123"
}
```

### Respuesta de autenticación

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 28800,
  "user": {
    "id": "<uuid>",
    "fullName": "Ana Pérez",
    "email": "ana@example.com"
  }
}
```

### Datos de demostración

Al iniciar el backend se crea automáticamente una cuenta de demostración y tres
registros de cuidado si todavía no existen:

```text
Email: ana@example.com
Contraseña: password123
```

La semilla es idempotente y no modifica datos existentes. Para desactivarla,
configura `APP_SEED_ENABLED=false` en `.env`.

## Seguridad

- BCrypt para hashes de contraseña.
- JWT HS256 con expiración e issuer validados.
- Backend stateless: no crea sesión HTTP.
- El secreto JWT se configura mediante `JWT_SECRET`; el valor de ejemplo es solo para desarrollo.
- CORS limitado a `FRONTEND_ORIGIN`.
- Los mensajes de login no revelan si el correo existe.

## Base de datos y migraciones

Flyway ejecuta automáticamente `src/main/resources/db/migration/V1__create_app_user.sql`.
Hibernate usa `ddl-auto: validate`: la estructura se modifica únicamente con migraciones.

Si ya tenías el volumen de la antigua base `despensa`, este cambio usa un volumen nuevo (`mascotas_postgres_data`).

## Pruebas

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Se cubren servicio de autenticación, credenciales inválidas, duplicados, generación/validación del JWT, validación HTTP de los payloads y health check.

## Detener PostgreSQL

```powershell
docker compose down
# o para borrar también datos locales:
docker compose down -v
```
