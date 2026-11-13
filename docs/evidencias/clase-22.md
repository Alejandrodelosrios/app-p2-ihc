# Clase 22 · Patrones oscuros

Integrantes: Alejandro De Los Rios Aranacibia
Modalidad: sin IA

---

## 1. Patrón encontrado

**Tipo (de los seis):** _TODO: elige uno de los seis de la diapositiva y escríbelo._

**Dónde:** pantalla *Mis mascotas* (`/mis-mascotas`) · `frontend/src/app/pages/my-pets/my-pets.page.ts:227` (método `deletePet`, llamada a `confirm(...)`)

**Por qué es un patrón oscuro:** _TODO: escríbelo con tus palabras._

> Preguntas guía (bórralas al terminar):
> - ¿Qué dice hoy el mensaje de confirmación?
> - ¿Qué NO dice y el usuario necesitaría saber antes de confirmar?
> - ¿A quién beneficia esa omisión? Si la respuesta es "a nadie", dilo con honestidad.
>
> Dato verificado en el backend (`PetService.delete`): el borrado es lógico (`deletedAt`); los cuidados e historial de la mascota **no** se borran y siguen existiendo; ya no se pueden crear cuidados nuevos para ella.

---

## 2. Antes

**Captura:**

<!-- TODO: pega aquí la captura del diálogo antes del cambio -->
![Antes - diálogo de eliminar mascota](evidencias/clase-22/antes.png)

**Código:**

```ts
// TODO: pega aquí el fragmento original de deletePet (my-pets.page.ts ~línea 222-229)
```

---

## 3. Después

**Captura:**

<!-- TODO: pega aquí la captura del diálogo después del cambio -->
![Después - diálogo de eliminar mascota](evidencias/clase-22/despues.png)

**Código:**

```ts
// TODO: pega aquí tu fragmento corregido
```

**Commit:** `_TODO: hash del commit del después_`

---

## 4. Principio aplicado

**Principio:** _TODO: cuál y por qué._

**Cómo lo cumple el después:** _TODO: 2 o 3 frases, defendidas con el principio y no con "se veía feo"._