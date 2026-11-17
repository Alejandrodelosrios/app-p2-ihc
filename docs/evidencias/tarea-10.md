# Tarea 10 · Eliminar cuenta honesta

Integrantes: Alejandro De Los Rios Aranacibia
App: Mascotas al Día (Angular + Spring Boot)

---

## 1. Mis datos: ver y descargar

- Pantalla `/mis-datos` (menú de usuario → *Mis datos*): `frontend/src/app/pages/my-data/my-data.page.ts`.
- Endpoint `GET /api/me/datos?formato=json|csv`: `backend/.../account/presentation/AccountController.java`.
- Minimización de datos (Clase 21): solo devuelve lo de la persona autenticada. No trae correos ni nombres de otras personas; de los cuidados asignados solo indica el rol (`gestor` / `asignado`).
- El CSV neutraliza fórmulas (`=`, `+`, `-`, `@`) y lleva BOM para que Excel respete las tildes.

## 2. Eliminar la cuenta con la regla de la app

Regla de Mascota al Día (diapositiva 13): *si comparte, los accesos se revocan; los cuidados realizados conservan al autor.* La diapositiva habla de «usuario eliminado»; aquí se decidió conservar el **nombre** del autor (ver justificación abajo).

En la app no existe «compartir mascotas»: cada mascota tiene un solo dueño. Lo que sí se comparte son los **cuidados asignados** (`care_record.assigned_to_user_id`). La regla se aplicó así (`AccountService.deleteAccount`, una sola transacción):

| Qué tiene la persona | Qué pasa al eliminar |
|---|---|
| Cuidados pendientes que otros le asignaron | Se revoca el acceso: quedan sin asignar y siguen con quien los creó |
| Cuidados pendientes que creó y asignó a otras personas | Siguen visibles para esas personas (pueden marcarlos como realizados); nadie podrá editarlos |
| Cuidados pendientes que creó sin asignar a nadie | Se eliminan (nadie más los ve) |
| Historial y cuidados ya realizados | Se conservan con su nombre como autor |
| Sus mascotas | Se desactivan (borrado lógico, como al eliminar una mascota) |
| Su usuario | Se **anonimiza**: se borran el correo (queda `eliminado-<id>@eliminado.invalid`) y la contraseña (queda una aleatoria); se conserva el nombre; se marca `deleted_at` |
| Enlaces de recuperación de contraseña | Se borran |

¿Por qué anonimizar y no borrar la fila? `pet.owner_id` tiene una clave foránea hacia `app_user`, y el historial referencia al usuario como autor. Anonimizar evita dejar datos huérfanos.

**¿Por qué se conserva el nombre?** Para **trazabilidad ante reclamos**: si dos personas cuidan a la misma mascota y hay una disputa por un cuidado, hay que poder saber quién lo hizo. Con «usuario eliminado» no se sabría. En la lista de cuidados, quien recibió una asignación sigue viendo «Te asignó Marta (cuenta eliminada)» y no una persona sin nombre.

**Tensión declarada con la minimización de datos (Clase 21):** el nombre sigue siendo un dato personal y se aparta de la redacción de la diapositiva 13. Se justifica porque tiene un propósito concreto (atribuir cuidados), se limita al nombre (el correo y la contraseña sí se borran) y el aviso al usuario lo dice con claridad antes de confirmar.

**Límite conocido:** el historial solo lo consulta cada persona sobre lo suyo y la pantalla no muestra autores; el nombre conservado se ve en la lista de cuidados y queda en la base de datos para una auditoría.

**Esa cuenta no puede iniciar sesión:** `AuthService.login` rechaza cuentas eliminadas, el correo original queda libre y un validador JWT (`SecurityConfig.jwtDecoder`) rechaza los tokens de cuentas eliminadas aunque todavía no hayan vencido (el token dura 8 h).

**La regla no bloquea la eliminación.** Se decidió que siempre se puede eliminar, así que en Mascota al Día no hay un «bloqueo» que probar (en la diapositiva 12 es «cuando corresponda»). El aviso, en cambio, siempre dice por qué pasa cada cosa y cómo conservar una copia (*Descargar mis datos*).

## 3. Confirmación honesta

Pantalla `/eliminar-cuenta` (`delete-account.page.ts`), no un `confirm()` nativo:

- El texto sale de `GET /api/me/eliminacion/resumen`: usa los números reales de la persona (`deletion-consequences.ts`) y solo muestra lo que le aplica.
- **Botones del mismo tamaño**: «Conservar mi cuenta» y «Eliminar mi cuenta» comparten tamaño, tipografía y peso visual; solo cambia el color del texto/borde para distinguir lo irreversible (Nielsen #5).
- **Sin culpa y sin pasos de más** (Nielsen #3): no hay frases de reproche, ni pantallas intermedias, ni escribir «ELIMINAR». «Conservar mi cuenta» es una salida a un clic.
- Si el resumen no carga, **no se permite eliminar a ciegas**: se ofrece reintentar.
- Tras eliminar: se cierra la sesión y se muestra un aviso en el login.

## 4. Revisión de los seis patrones oscuros en toda la app

> Completar con los seis patrones de la Clase 22 (la tabla de abajo es la estructura pedida).

| Patrón | Dónde se revisó | Qué se encontró | Qué se corrigió | Qué queda pendiente |
|---|---|---|---|---|
| Honestidad (confirmación sin consecuencias) | Eliminar mascota | `confirm()` sin explicar el alcance | Mensaje con consecuencias (Clase 22) | Reemplazar el `confirm()` por un diálogo propio |
| Honestidad (confirmación sin consecuencias) | Eliminar cuenta | No existía la función | Pantalla honesta con datos reales | — |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |

## 5. E2E, eventos y evidencia

**Eventos de la Tarea 9** (lista blanca en `AnalyticsEvent.java` y `analytics.model.ts`, sin datos personales):

| Evento | Quién lo emite | Cuándo |
|---|---|---|
| `eliminar_cuenta_inicio` | Frontend | Al abrir la pantalla de eliminar cuenta |
| `eliminar_cuenta_exito` | Backend (`AccountService`) | Al terminar la eliminación; lo emite el servidor porque después el token ya no sirve |
| `datos_descargados` (`formato`: `json`/`csv`) | Frontend | Al descargar «Mis datos» |
| `eliminar_cuenta_bloqueo` | — | No aplica: la regla de Mascota al Día no bloquea |

**Pruebas**

- E2E (Playwright): `tests/eliminarCuenta.spec.ts` — flujo completo, botones del mismo tamaño, evento de inicio sin datos personales, login rechazado después de eliminar, cancelar, y descarga JSON/CSV.
- Backend: `AccountServiceTest`, `AccountDataCsvTest`, `AccountDeletionIntegrationTest` (el token de una cuenta eliminada recibe 401 y el login falla).
- Frontend: `deletion-consequences.spec.ts`.

**Capturas:** *(agregar)* `tarea-10-mis-datos.png`, `tarea-10-confirmacion.png`, `tarea-10-login-bloqueado.png`.

**Commits separados:** ver `docs/commits.md`.
