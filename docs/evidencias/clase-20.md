**Proyecto:** Mascotas al Día  
**Integrantes del grupo:** Alejandro de los rios arancibia  

---

## 1. Identificar

* **Pantalla seleccionada:** Historial de Cuidados (`/cuidados`)
* **Problema identificado:** En la sección de gestión de cuidados, los filtros de consulta (por estado, mascota y fecha) se encuentran ocultos o requieren desplegar paneles adicionales mediante el botón *"Mostrar más"*. Esto genera una fricción innecesaria y aumenta la carga cognitiva en usuarios que desean revisar rápidamente sus tareas o medicaciones pendientes sin navegar por menús desplegables.
* **Tarea confusa / difícil:** Filtrar y visualizar rápidamente los cuidados en estado **Pendiente** vs. **Completado**.

---

## 2. Proponer

* **Propuesta de diseño:** Rediseñar la sección superior de la pantalla de **Cuidados** agregando una barra de **filtros rápidos mediante accesos de un solo clic (Chips / Píldoras)** para alternar entre estados (*Todos*, *Pendientes*, *Completados*) y agrupar visualmente la barra de búsqueda por nombre de mascota.
* **Estructura propuesta:**
  * **Sección superior:** Barra de filtros directos (*Tabs/Píldoras*) sin necesidad de expandir *"Mostrar más"*.
  * **Etiquetas y jerarquía:** Cambio de nombres en botones a acciones claras y directas (*Ver pendientes*, *Ver completados*).
* **Justificación de la decisión:** 
  * **Reducción de clics:** Cumple con la Ley de Hick y disminuye el tiempo de decisión al tener accesos inmediatos a las tareas prioritarias.
  * **Visibilidad del estado del sistema:** El usuario identifica de inmediato cuántos cuidados tiene pendientes y en qué categoría se encuentra posicionado.

---

## 3. Implementar

Se modificaron los componentes del frontend en Angular para incluir la lógica de selección rápida y adaptar los estilos visuales:
* `frontend/src/app/pages/care-record-list/care-record-list.page.html`
* `frontend/src/app/pages/care-record-list/care-record-list.page.ts`

### Evidencia visual (Capturas)

#### Antes (Pantalla original con filtros ocultos o desplegables)
![Pantalla Antes](evidencias/clase-20-antes.png)

#### Después (Propuesta con filtros rápidos y etiquetas claras)
![Pantalla Después](evidencias/clase-20-despues.png)

---

## 4. Prueba y Entrega

### Resultado de la Prueba de Usuario
* **Persona que realizó la prueba:** [Nombre del compañero de otro grupo]
* **Indicación dada:** *"Encuentra la lista de cuidados pendientes para Coco sin desplegar el menú de opciones completas."*
* **Observaciones durante la prueba:**
  * ¿Encontró la función?: **Sí / No**
  * ¿Dudó en algún momento?: **[Comentario breve, ej: "Ubicó inmediatamente el filtro rápido de 'Pendientes' en la cabecera"].**
  * Tiempo tomado para completar la tarea: **[Ej. 4 segundos].**
