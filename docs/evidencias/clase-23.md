# Clase 23 · Inventario y tokens
Integrantes: Alejandro De Los Rios Arancibia · Modalidad: sin IA

## 1. Inventario de botones
Captura: no disponible (el backend no estaba corriendo por temas de docker). El inventario se hizo leyendo los estilos de los botones en `frontend/src/app`.

| Botón | Dónde | Relleno | Radio | Alto |
|---|---|---|---|---|
| Iniciar sesión, Crear cuenta, Registrar mascota (`app-button` primario) | `shared/ui/button` | lleno `--color-primary` | 14px (`--radius-md`) | 48px |
| Cancelar (`app-button` secundario) | `shared/ui/button` | `--color-surface-2` + borde | 14px | 48px |
| Registrar cuidado (`.new-care-button`) | cuidados | lleno `--color-primary` | 14px | 48px |
| Aplicar filtros (`.filter-button`) | cuidados | lleno `--color-primary` | 14px | 42px |
| Mostrar más (`.clear-filter-button`) | cuidados | transparente + borde | 14px | 42px |
| Ver solo vencidos (`.overdue-banner__button`) | cuidados | transparente + borde rojo (`--color-danger`) | 14px | 36px |
| Realizado tarde / Omitir (`.late-button`, `--secondary`) | tarjeta de cuidado | lleno / transparente con borde | 14px | 40px |
| Completar (`.complete-button`) | tarjeta de cuidado | `--color-primary-soft` + borde | circular (50%) | 40×40px |
| Editar / Eliminar (`.action-button`, `--danger`) | tarjeta de cuidado | transparente + borde | circular (50%) | 40×40px |
| Modo oscuro / Menú (`.theme-toggle`, `.menu-button`) | barra superior | `--color-surface` + borde | circular (50%) | 42×42px |
| Opciones (`.choice`, `.choice--delete`) | mis datos, eliminar cuenta | borde fuerte | 14px | 52px |

Diferencias marcadas:
1. **Relleno:** "Aplicar filtros" es lleno, "Mostrar más" es solo borde con fondo transparente, y "Completar" usa un fondo suave (`--color-primary-soft`).
2. **Radio:** los botones de texto usan 14px, pero los de acción de la tarjeta (Completar, Editar, Eliminar) y los de la barra superior son circulares (50%).
3. **Alto:** el botón primario cambia según la pantalla: 48px (`app-button`, Registrar cuidado), 42px (Aplicar filtros) y 40px (Realizado tarde).
4. **Estado que falta:** `app-button` y `.choice` tienen `:hover`, pero los botones de la lista de cuidados y de la tarjeta (`.filter-button`, `.late-button`, `.action-button`, `.complete-button`, etc.) solo tienen `:disabled` y `:focus-visible`, no `:hover`.

## 2. Tokens (definidos en `frontend/src/styles.css`)
| token | claro | oscuro | dónde se usa |
|---|---|---|---|
| `--color-bg` | #f4f8f6 | #10201c | fondo de todas las pantallas |
| `--color-surface` | #ffffff | #172a25 | tarjetas, menú, barra superior |
| `--color-text` | #18312b | #ecf6f2 | títulos y texto principal |
| `--color-primary` | #2f7d6d | #67c7b0 | botones principales, acentos |
| `--color-danger` | #b33a3a | #ff9a9a | Eliminar, cuidados vencidos, errores |

## 3. Colores fijos
Pantalla revisada: **Registrar/editar cuidado** (`frontend/src/app/pages/care-record/`)
Buscamos (Ctrl+Shift+F, con regex activado): `#[0-9a-fA-F]{3,8}\b`, `rgba?\(`, `hsla?\(`, y nombres de color (`white`, `black`, `red`, etc.)
Encontramos: **4**, todos escritos a mano en `care-record.css`:
- `.feedback--success` (línea 85-86): `color: #16794a` y `background: rgba(22, 121, 74, 0.1)`
- `.feedback--error` (línea 90-91): `color: #c03535` y `background: rgba(192, 53, 53, 0.1)`

No se cuentan los hex y rgba de `styles.css`, porque ahí están dentro de la definición de los tokens (`:root`). Tampoco se encontraron nombres de color. Fuera de las pantallas, `index.html` tiene un hex en `<meta name="theme-color" content="#2f7d6d">`.
Solo se cuenta, no se corrige.