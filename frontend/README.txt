# Frontend — Mascotas al Día

Frontend mobile-first construido con Angular 21 standalone.

## Arranque

Con el backend ejecutándose en `http://localhost:8080`:

```powershell
cd frontend
npm ci
npm start
```

Abre `http://localhost:4200`.

## Rutas

- `/login`: inicio de sesión.
- `/registro`: creación de cuenta.
- `/mis-mascotas`: ruta privada detrás de `authGuard`.

Usuarios autenticados que intenten entrar en login/registro son redirigidos a `/mis-mascotas`.

## Piezas reutilizables

- `ButtonComponent`: botón primario/secundario, estado disabled/loading.
- `FormFieldComponent`: label + input + error accesible.
- `ThemeToggleComponent`: alterna tema claro/oscuro.
- `AppShellComponent`: header + menú autenticado desplegable.
- `AuthService`: contrato con `/api/auth` y estado reactivo de sesión.
- `TokenStorageService`: persistencia y validación básica de expiración JWT.
- `authInterceptor`: adjunta `Bearer` únicamente a peticiones del API.
- `authGuard` / `publicOnlyGuard`: control de navegación.

## Tema

El sistema visual está basado en verde salvia/teal, asociado a cuidado y bienestar. Los temas claro y oscuro se definen con custom properties globales en `src/styles.css`; los componentes consumen tokens semánticos y no colores hardcodeados.

## Pruebas

```powershell
npm test
npm run build
```

Los tests básicos cubren creación de la aplicación, almacenamiento/expiración de sesión, login y comportamiento del interceptor JWT.
