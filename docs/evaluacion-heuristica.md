# Evaluación heurística H1–H10

## Flujo evaluado

El flujo evaluado corresponde a la tarea principal de **encontrar un cuidado y reprogramarlo**, comenzando en el listado de cuidados y terminando con el resultado de la edición.

Se evaluaron dos escenarios.

### Caso permitido

1. Ingresar al listado de cuidados.
2. Buscar o filtrar cuidados pendientes.
3. Seleccionar un cuidado pendiente.
4. Entrar a editarlo.
5. Cambiar su fecha por una fecha válida.
6. Guardar los cambios.
7. Volver al listado y comprobar la nueva fecha.

### Caso bloqueado

1. Ingresar al listado de cuidados.
2. Seleccionar un cuidado realizado.
3. Entrar a editarlo.
4. Comprobar que la fecha ya no puede modificarse.
5. Comprobar que la interfaz explica el motivo del bloqueo.

## Escala de severidad

| Severidad | Descripción |
| --- | --- |
| 0 | No es un problema de usabilidad |
| 1 | Problema cosmético |
| 2 | Problema menor: dificulta la tarea, pero permite completarla |
| 3 | Problema mayor: dificulta de forma importante completar la tarea |
| 4 | Problema catastrófico: impide completar la tarea |

## Evidencias

Las capturas tomadas durante la evaluación se encuentran en `docs/evidencias/`:

- `01-listado.png`
- `02-busqueda-pendientes.png`
- `03-reprogramacion-pendiente.png`
- `04-reprogramacion-bloqueada.png`
- `05-h3-cancelar-despues.png`
- `06-h7-contexto-despues.png`

---

## H1 · Visibilidad del estado del sistema

**Estado:** Cumple

**Pantalla o paso evaluado:** listado y filtrado de cuidados.

La interfaz mantiene visible el estado actual del sistema mientras se aplican filtros. Cuando se selecciona, por ejemplo, el estado **Pendiente**, el selector conserva esa opción y además aparece una sección de **Filtros activos** que informa qué criterio está afectando al listado.

También existen estados diferenciados para carga, error y ausencia de resultados en el listado.

Esto permite que la persona entienda qué está mostrando actualmente la aplicación y qué filtros están afectando el resultado.

**Evidencia:** `docs/evidencias/02-busqueda-pendientes.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-list.page.ts`

---

## H2 · Relación entre el sistema y el mundo real

**Estado:** Cumple

**Pantalla o paso evaluado:** listado y edición de cuidados.

La aplicación utiliza términos que corresponden directamente con el dominio de Mascota al Día, como:

- Mascota
- Cuidado
- Fecha
- Pendiente
- Realizado
- Editar cuidado
- Registrar cuidado

La información se presenta usando palabras comprensibles para una persona que administra los cuidados de sus mascotas y no mediante términos técnicos internos del sistema.

**Evidencia:**

- `docs/evidencias/01-listado.png`
- `docs/evidencias/03-reprogramacion-pendiente.png`
- `docs/evidencias/04-reprogramacion-bloqueada.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-card/care-record-card.component.html`
- `frontend/src/app/pages/care-record/care-record.html`

---

## H3 · Control y libertad del usuario

**Estado:** Corregida

**Estado inicial:** No cumplía  
**Severidad inicial:** 2

**Pantalla o paso evaluado:** edición de un cuidado.

### Problema observado

Después de entrar a **Editar cuidado**, la pantalla presentaba únicamente la acción **Guardar cambios**. No existía una acción visible para cancelar la edición y regresar al listado sin guardar.

Si la persona entraba por error o decidía no continuar, debía depender de la navegación del navegador o de otra navegación externa a la acción.

### Consecuencia

La tarea podía completarse, pero salir de una edición iniciada accidentalmente no era una opción explícita dentro de la interfaz.

### Corrección realizada

Se agregó una acción secundaria **Cancelar** en la pantalla de edición.

La acción:

- regresa al listado de cuidados;
- no envía cambios al backend;
- mantiene **Guardar cambios** como acción principal.

### Verificación

Se repitió el flujo de edición y se comprobó que la persona puede entrar a editar un cuidado y salir mediante **Cancelar** sin guardar modificaciones.

**Sub-issue:** `#9`  
**Commit:** `a9f5abe` — `fix(ui): permitir cancelar la edicion de cuidados #9`

**Evidencia antes:** `docs/evidencias/03-reprogramacion-pendiente.png`  
**Evidencia después:** `docs/evidencias/05-h3-cancelar-despues.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record/care-record.html`
- `frontend/src/app/pages/care-record/care-record.ts`
- `frontend/src/app/pages/care-record/care-record.css`

---

## H4 · Consistencia y estándares

**Estado:** Cumple

**Pantalla o paso evaluado:** listado, acciones de las tarjetas y formulario de edición.

Los mismos conceptos mantienen una representación consistente durante el flujo:

- Los cuidados pendientes se muestran como **Pendiente**.
- Los cuidados realizados se muestran como **Realizado**.
- La edición utiliza el icono de lápiz tanto desde la tarjeta como en la pantalla de edición.
- Los botones y campos mantienen la misma apariencia visual que el resto de la aplicación.
- Las acciones principales conservan estilos y jerarquías similares.

Esto reduce la necesidad de aprender nuevos significados al cambiar entre listado y edición.

**Evidencia:**

- `docs/evidencias/01-listado.png`
- `docs/evidencias/03-reprogramacion-pendiente.png`
- `docs/evidencias/04-reprogramacion-bloqueada.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-card/care-record-card.component.html`
- `frontend/src/app/pages/care-record/care-record.html`
- `frontend/src/app/shared/ui/button/button.component.ts`
- `frontend/src/app/shared/ui/form-field/form-field.component.ts`
- `frontend/src/app/shared/ui/icon/icon.component.ts`

---

## H5 · Prevención de errores

**Estado:** Cumple

**Pantalla o paso evaluado:** reprogramación de un cuidado pendiente y edición de un cuidado realizado.

La aplicación previene dos errores importantes antes de que lleguen a convertirse en una operación inválida.

En un cuidado pendiente, el campo de fecha limita la selección para que una reprogramación no pueda utilizar una fecha anterior al día actual.

En un cuidado realizado, el campo de fecha deja de mostrarse como editable. De esta forma la persona no puede intentar reprogramarlo desde la interfaz.

Además, estas restricciones no dependen solamente del frontend. El backend también valida que:

- un cuidado realizado no pueda cambiar su fecha;
- una nueva fecha no pueda ser anterior a hoy.

**Evidencia:**

- `docs/evidencias/03-reprogramacion-pendiente.png`
- `docs/evidencias/04-reprogramacion-bloqueada.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record/care-record.html`
- `frontend/src/app/shared/ui/form-field/form-field.component.ts`
- `backend/src/main/java/com/ihc/mascotas/backend/care/application/CareRecordService.java`

---

## H6 · Reconocer antes que recordar

**Estado:** Cumple

**Pantalla o paso evaluado:** listado de cuidados y selección del cuidado a editar.

Las tarjetas muestran directamente la información necesaria para reconocer cada cuidado:

- nombre de la mascota;
- tipo de animal;
- cuidado;
- fecha;
- estado.

Además, los filtros permanecen visibles mientras se utilizan y la aplicación presenta los filtros activos de forma explícita.

La persona puede decidir qué cuidado editar a partir de la información visible, sin tener que recordar datos de una pantalla anterior.

**Evidencia:**

- `docs/evidencias/01-listado.png`
- `docs/evidencias/02-busqueda-pendientes.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-card/care-record-card.component.html`

---

## H7 · Flexibilidad y eficiencia de uso

**Estado:** Corregida

**Estado inicial:** No cumplía  
**Severidad inicial:** 2

**Pantalla o paso evaluado:** búsqueda o filtrado de un cuidado, edición y regreso al listado.

### Aspecto positivo inicial

El listado ya permitía encontrar cuidados rápidamente mediante búsqueda por mascota y filtros por estado, tipo y fecha. Los filtros también podían combinarse.

### Problema observado

Cuando una persona aplicaba filtros para encontrar un cuidado, entraba a editarlo y posteriormente regresaba al listado, los filtros utilizados anteriormente se perdían.

Por ejemplo:

1. Se seleccionaba `Estado: Pendiente`.
2. Se encontraba el cuidado que se quería reprogramar.
3. Se entraba a editar.
4. Se guardaba o se regresaba al listado.
5. El listado volvía a cargar con los filtros vacíos.

La persona debía configurar nuevamente la búsqueda para recuperar el mismo contexto de trabajo.

### Consecuencia

El flujo obligaba a repetir pasos ya realizados y reducía la eficiencia cuando existían muchos cuidados registrados.

### Corrección realizada

Se conservó el estado de los filtros durante el recorrido listado → edición → listado.

Los filtros activos se representan en los parámetros de la URL y se restauran cuando la persona regresa al listado.

La corrección funciona tanto al:

- guardar una edición;
- cancelar una edición.

La acción **Limpiar filtros** continúa eliminando el contexto de filtrado cuando la persona lo solicita explícitamente.

### Verificación

Se aplicó el filtro `Estado: Pendiente`, se ingresó a editar un cuidado y luego se regresó al listado. Se comprobó que el filtro **Pendiente** continuaba seleccionado y aparecía nuevamente como filtro activo.

El problema original ocurre durante una transición entre pantallas, por lo que una captura aislada no demuestra por sí sola la pérdida de contexto. La evidencia inicial muestra el estado antes de entrar a editar y la evidencia final muestra que ese mismo contexto permanece después de regresar.

**Sub-issue:** `#10`  
**Commit:** `804f2b7` — `fix(ui): conservar filtros al volver de editar #10`

**Evidencia antes:** `docs/evidencias/02-busqueda-pendientes.png`  
**Evidencia después:** `docs/evidencias/06-h7-contexto-despues.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.ts`
- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-card/care-record-card.component.html`
- `frontend/src/app/pages/care-record/care-record.ts`

---

## H8 · Diseño estético y minimalista

**Estado:** Cumple

**Pantalla o paso evaluado:** listado y formulario de edición.

Las pantallas contienen solamente la información necesaria para realizar la tarea.

En el listado se separan claramente:

- encabezado;
- filtros;
- tarjetas;
- acciones principales.

En la edición se presentan únicamente los campos relacionados con el cuidado y las acciones necesarias para completar o cancelar la edición.

Cuando un cuidado está realizado, el campo de fecha desaparece y se reemplaza por una explicación breve, evitando mostrar un control que la persona no puede utilizar.

La interfaz mantiene una jerarquía visual clara y no introduce elementos que compitan innecesariamente con la tarea principal.

**Evidencia:**

- `docs/evidencias/01-listado.png`
- `docs/evidencias/03-reprogramacion-pendiente.png`
- `docs/evidencias/04-reprogramacion-bloqueada.png`
- `docs/evidencias/05-h3-cancelar-despues.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-list.page.css`
- `frontend/src/app/pages/care-record/care-record.html`
- `frontend/src/app/pages/care-record/care-record.css`

---

## H9 · Ayudar a reconocer, diagnosticar y recuperarse de errores

**Estado:** Cumple

**Pantalla o paso evaluado:** carga del listado y actualización de un cuidado.

La aplicación presenta mensajes comprensibles cuando una operación falla.

En el listado, si no se pueden obtener los cuidados, se muestra el mensaje:

> No se pudieron cargar los cuidados

junto con una acción **Reintentar**.

Durante la edición, si el cuidado no puede actualizarse, se mantiene a la persona en el formulario y se presenta un mensaje de error en lugar de descartar silenciosamente la información ingresada. Esto permite volver a intentar la operación.

Los mensajes utilizan lenguaje del usuario y no muestran información técnica de la implementación.

**Código relacionado:**

- `frontend/src/app/pages/care-record-list/care-record-list.page.html`
- `frontend/src/app/pages/care-record-list/care-record-list.page.ts`
- `frontend/src/app/pages/care-record/care-record.html`
- `frontend/src/app/pages/care-record/care-record.ts`

---

## H10 · Ayuda y documentación

**Estado:** Cumple

**Pantalla o paso evaluado:** intento de reprogramar un cuidado realizado.

La ayuda necesaria para comprender la principal restricción del flujo aparece en el mismo lugar donde se necesita.

Cuando un cuidado ya está realizado, la interfaz elimina la opción de cambiar la fecha y muestra el mensaje:

> Este cuidado ya fue realizado y no puede reprogramarse.

En el caso de un cuidado pendiente también se informa:

> La nueva fecha no puede ser anterior a hoy.

Estas indicaciones son breves, específicas y contextuales. La persona no necesita consultar documentación externa para comprender las restricciones de la reprogramación.

**Evidencia:**

- `docs/evidencias/03-reprogramacion-pendiente.png`
- `docs/evidencias/04-reprogramacion-bloqueada.png`

**Código relacionado:**

- `frontend/src/app/pages/care-record/care-record.html`

---

# Hallazgos encontrados y corregidos

La evaluación encontró dos problemas de usabilidad. Ambos fueron corregidos y verificados repitiendo el flujo correspondiente.

| Heurística | Hallazgo | Severidad | Sub-issue | Commit | Resultado |
| --- | --- | ---: | --- | --- | --- |
| H3 · Control y libertad | No existía una acción visible para cancelar la edición | 2 | #9 | `a9f5abe` | Corregida |
| H7 · Flexibilidad y eficiencia | Los filtros activos se perdían al regresar de la edición | 2 | #10 | `804f2b7` | Corregida |

No se identificaron problemas que impidieran completar el flujo principal.

# Evidencias de las correcciones

## H3 · Cancelar la edición

**Antes:** `docs/evidencias/03-reprogramacion-pendiente.png`  
**Después:** `docs/evidencias/05-h3-cancelar-despues.png`

La captura posterior muestra la nueva acción **Cancelar** junto a **Guardar cambios**. Se verificó además que cancelar vuelve al listado sin ejecutar una actualización del cuidado.

## H7 · Conservar el contexto de filtros

**Antes:** `docs/evidencias/02-busqueda-pendientes.png`  
**Después:** `docs/evidencias/06-h7-contexto-despues.png`

Se verificó el recorrido:

1. aplicar `Estado: Pendiente`;
2. entrar a editar un cuidado;
3. guardar o cancelar;
4. regresar al listado;
5. comprobar que `Estado: Pendiente` continúa activo.

Debido a que el hallazgo depende de una transición entre pantallas, las capturas representan el contexto inicial y final y se complementan con la ejecución del recorrido descrito.

# Resultado final

| Heurística | Resultado final |
| --- | --- |
| H1 · Visibilidad del estado del sistema | Cumple |
| H2 · Relación con el mundo real | Cumple |
| H3 · Control y libertad | **Corregida** |
| H4 · Consistencia y estándares | Cumple |
| H5 · Prevención de errores | Cumple |
| H6 · Reconocer antes que recordar | Cumple |
| H7 · Flexibilidad y eficiencia | **Corregida** |
| H8 · Diseño estético y minimalista | Cumple |
| H9 · Reconocer, diagnosticar y recuperarse de errores | Cumple |
| H10 · Ayuda y documentación | Cumple |

# Verificación final

Después de implementar las correcciones se repitió el flujo principal.

Se comprobó que:

- H3 permite cancelar una edición sin guardar;
- H7 conserva los filtros después de entrar y salir de la edición;
- el caso permitido continúa permitiendo reprogramar un cuidado pendiente;
- el caso bloqueado continúa impidiendo reprogramar un cuidado realizado;
- la interfaz continúa explicando el motivo del bloqueo;
- la nueva fecha se almacena y permanece después de recargar;
- todas las heurísticas quedan finalmente en estado **Cumple** o **Corregida**.
