# GUÍA DE IMPLEMENTACIÓN FRONTEND — MASCOTA AL DÍA

## 1. Contexto del proyecto

Estamos trabajando en **Mascota al Día**, una aplicación web para gestionar mascotas, sus cuidados y el historial de actividades.

El proyecto utiliza:

- Frontend: Angular 21 + TypeScript.
- Backend: Spring Boot + Java.
- Base de datos: PostgreSQL.
- Arquitectura backend: hexagonal.
- Comunicación: API REST.

El backend ya fue actualizado y probado. Entre los cambios recientes se encuentra la asociación de los cuidados con mascotas existentes mediante `petId`.

**Objetivo de esta tarea:** mejorar el frontend Angular tomando como referencia las observaciones que hizo el evaluador, sin romper las funcionalidades que ya existen.

## 2. Feedback que debemos resolver

El evaluador identificó los siguientes problemas:

### Dashboard y mascotas

- No encontró una opción visible para crear mascotas.
- No encontró fácilmente el listado de mascotas.
- El panel «Mis mascotas» del dashboard no mostraba información.
- Sugirió mostrar las mascotas directamente en el dashboard.
- Sugirió incorporar accesos rápidos.

### Selector de fechas

- El selector permitía asignar fechas pasadas.
- El backend ya fue corregido y probado para validar las fechas.
- Ahora debemos reflejar esa restricción también en Angular.

### Historial

- Fue difícil encontrar la sección de historial porque estaba dentro del menú desplegable superior.
- El historial no ofrecía filtros.
- Se recomendó agregar paginación para evitar un listado excesivamente largo.
- Se solicitó mostrar información adicional, como el cuidador.
- Se recomendó destacar visualmente los registros vencidos y las acciones de reprogramación.

## 3. Primera fase: inspección del frontend y backend

Antes de modificar código, inspecciona el repositorio completo.

Identifica:

1. Estructura de carpetas y módulos Angular.
2. Componentes del dashboard.
3. Componentes de mascotas.
4. Componentes de cuidados.
5. Componentes del historial.
6. Servicios HTTP.
7. Interfaces y modelos TypeScript.
8. Rutas y configuración de navegación.
9. Componentes reutilizables.
10. Estilos globales y convenciones visuales.

También inspecciona los controladores, DTOs y servicios actuales del backend para verificar los contratos reales.

**Importante:** no supongas que los endpoints mantienen sus contratos antiguos.

El modelo de cuidados ahora utiliza `petId`. Comprueba cómo se envían y reciben los siguientes datos:

- Identificador de mascota.
- Nombre de mascota para mostrar.
- Especie.
- Tipo de cuidado.
- Fecha programada.
- Estado.
- Usuario creador.
- Usuario asignado.
- Intervalo de recurrencia.

Comprueba también los endpoints disponibles para consultar mascotas, cuidados e historial.

Si el backend no ofrece algún filtro o dato que el frontend necesita, documenta esa limitación. No inventes endpoints ni simules funcionalidades que dependan de datos que no existen.

## 4. Segunda fase: implementar mejoras

Una vez comprendida la estructura real, realiza las siguientes mejoras.

### 4.1. Dashboard

Convertir el dashboard en un punto de entrada útil para el usuario.

Debe incluir:

- Resumen de mascotas registradas.
- Cantidad de cuidados pendientes.
- Cantidad de cuidados próximos.
- Cantidad de cuidados vencidos.
- Acceso rápido para registrar una mascota.
- Acceso rápido para registrar un cuidado.
- Listado visible de mascotas del usuario.

El listado debe permitir acceder a los detalles de cada mascota y consultar sus cuidados.

Evitar mostrar paneles vacíos sin una explicación. Si no existen mascotas, mostrar un estado vacío con una acción clara para registrar la primera.

### 4.2. Gestión de mascotas

Asegurar que el usuario pueda:

- Consultar sus mascotas.
- Registrar una mascota.
- Editar sus datos.
- Consultar sus detalles.
- Acceder a los cuidados relacionados.

El formulario debe utilizar el contrato real del backend.

No volver a crear mascotas indirectamente al registrar un cuidado.

La mascota debe existir previamente y seleccionarse desde el formulario de cuidados.

### 4.3. Formulario de cuidados

Actualizar el formulario para trabajar con `petId`.

Campos esperados, según los DTOs reales:

- Mascota: selector de mascotas disponibles.
- Tipo de cuidado.
- Fecha programada.
- Asignación a otro usuario, si está disponible.
- Recurrencia, si está disponible.

El selector de mascotas debe mostrar información comprensible, por ejemplo:

`Luna — Perro`

No se debe enviar `petName` o `animalType` como sustitutos de `petId` si el contrato actual ya no los utiliza.

#### Validación de fechas

El selector debe impedir elegir fechas anteriores al día actual.

Utilizar la propiedad `min` del control de fecha cuando corresponda.

También:

- Mostrar un mensaje claro cuando la fecha no sea válida.
- Evitar enviar el formulario si existe un error de validación.
- Mostrar correctamente los errores HTTP del backend.
- Aplicar las mismas reglas al crear y reprogramar cuidados.

No bloquear la consulta de cuidados que ya vencieron.

### 4.4. Listado de cuidados

Mejorar la presentación del listado.

Mostrar, cuando los datos estén disponibles:

- Mascota.
- Tipo de cuidado.
- Fecha programada.
- Estado.
- Cuidador o usuario asignado.
- Acciones permitidas.

Diferenciar visualmente:

- Pendiente.
- Completado.
- Vencido.

Un cuidado debe considerarse vencido cuando continúa pendiente y su fecha programada es anterior al día actual.

No modificar el estado persistido en el backend solamente para conseguir el efecto visual de vencido.

Respetar los permisos existentes:

- El creador puede administrar sus cuidados.
- El usuario asignado puede realizar las acciones que permite el backend.
- No mostrar acciones que el usuario autenticado no tenga autorizadas.

### 4.5. Historial

Mejorar la navegación y presentación del historial.

Requisitos:

- Acceso directo desde la navegación principal.
- Listado de eventos.
- Nombre de mascota.
- Tipo de cuidado.
- Acción realizada.
- Fecha del evento.
- Persona que realizó la acción.
- Información de reprogramación, cuando corresponda.
- Filtros.
- Paginación.

#### Filtros propuestos

- Mascota.
- Tipo de cuidado.
- Cuidador o usuario que realizó la acción.
- Fecha inicial.
- Fecha final.
- Tipo de evento, si el backend lo permite.

La paginación debe utilizar el mecanismo disponible en la API.

Si el backend solo devuelve todos los registros, analiza si es viable implementar paginación temporal en el frontend. No presentes esa solución como paginación del servidor.

Si existen limitaciones en el contrato del historial, documentarlas y proponer los cambios necesarios sin modificar el backend en esta tarea.

Destacar visualmente los eventos de reprogramación y los cuidados que correspondan a registros vencidos, siempre que esa información pueda determinarse correctamente.

### 4.6. Navegación

El historial debe dejar de estar escondido exclusivamente dentro del menú desplegable del usuario.

Evaluar una navegación principal clara, preferiblemente lateral si encaja con el diseño actual.

Secciones sugeridas:

- Dashboard.
- Mis mascotas.
- Mis cuidados.
- Historial.
- Cuidados asignados, si corresponde al funcionamiento existente.

No crear rutas duplicadas si ya existen rutas equivalentes.

## 5. Diseño visual

Mantener una interfaz moderna, clara y consistente.

Referencias visuales del proyecto:

- Verde azulado: `#237063`.
- Fondo crema: `#F7F5EE`.
- Tarjetas blancas.
- Espaciado basado en múltiplos de 8 px.
- Bordes suaves.
- Iconografía consistente.
- Diseño responsive.

Evitar:

- Sobrecargar el dashboard con demasiadas tarjetas.
- Colores excesivamente llamativos.
- Tablas difíciles de leer.
- Acciones escondidas.
- Formularios demasiado extensos sin organización.

Los estados vacíos, errores, cargas y operaciones exitosas deben tener una presentación clara.

## 6. Reglas técnicas

1. Mantener Angular 21 y TypeScript.
2. Respetar la estructura y convenciones existentes.
3. Reutilizar servicios y componentes cuando sea razonable.
4. No duplicar lógica de peticiones HTTP.
5. No introducir librerías nuevas sin una justificación clara.
6. No modificar el backend.
7. No cambiar contratos API por conveniencia del frontend.
8. No eliminar funcionalidades existentes.
9. No introducir datos ficticios para simular resultados reales.
10. Mantener los permisos y reglas de negocio del backend.
11. Evitar componentes excesivamente grandes.
12. Mantener los modelos TypeScript sincronizados con los DTOs reales.

## 7. Pruebas y validación

Después de implementar:

- Ejecutar la compilación de Angular.
- Ejecutar las pruebas existentes.
- Corregir errores de TypeScript.
- Verificar las rutas.
- Comprobar la integración con la API.
- Verificar los formularios.
- Verificar los estados vacíos.
- Verificar los filtros y la paginación.
- Comprobar la visualización en escritorio y móvil.

### Casos funcionales mínimos

1. Un usuario sin mascotas puede registrar su primera mascota.
2. Un usuario con mascotas puede verlas en el dashboard.
3. Puede crear un cuidado seleccionando una mascota existente.
4. No puede enviar un cuidado con fecha anterior a hoy.
5. Puede consultar cuidados vencidos.
6. Puede completar un cuidado según sus permisos.
7. Puede consultar el historial.
8. Puede aplicar filtros disponibles.
9. Puede navegar entre páginas del historial.
10. Puede identificar quién realizó una acción cuando el backend proporciona esa información.

## 8. Forma de trabajo

Trabaja por incrementos pequeños.

Orden recomendado:

1. Inspección de contratos y estructura.
2. Actualización de interfaces y servicios Angular.
3. Formulario de cuidados con selección de mascota y validación de fechas.
4. Gestión y listado de mascotas.
5. Dashboard.
6. Listado de cuidados y estados.
7. Historial, filtros y paginación.
8. Pruebas y correcciones visuales.

Puedes realizar los cambios directamente en el frontend, pero antes de cada grupo de modificaciones identifica los archivos afectados.

No realices cambios masivos sin verificar que el proyecto compila.

## 9. Entrega final obligatoria

Al finalizar, entrega un informe Markdown llamado `FRONTEND_IMPLEMENTATION_REPORT.md`.

Debe contener:

1. Resumen de cambios.
2. Archivos modificados.
3. Archivos nuevos.
4. Funcionalidades implementadas.
5. Endpoints utilizados.
6. Adaptaciones realizadas por el cambio a `petId`.
7. Validaciones de fechas.
8. Mejoras del dashboard.
9. Mejoras del historial.
10. Filtros y paginación implementados.
11. Pruebas ejecutadas y resultados.
12. Problemas pendientes.
13. Limitaciones encontradas en el backend.
14. Recomendaciones para la siguiente iteración.

Para cada funcionalidad, indicar si está:

- Implementada y probada.
- Implementada pero no probada.
- Pendiente por limitación del backend.
- Pendiente por falta de información.

## 10. Instrucción final

El objetivo no es rediseñar toda la aplicación desde cero.

Queremos mejorar la experiencia de uso y resolver las observaciones del evaluador, conservando las funcionalidades que ya funcionan.

Prioriza la claridad, la consistencia con el backend y la facilidad de uso.

**Primero inspecciona el proyecto real. Después implementa las mejoras por etapas y valida cada incremento.**