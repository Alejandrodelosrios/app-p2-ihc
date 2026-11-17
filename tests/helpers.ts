import { APIRequestContext, Locator, Page, expect } from '@playwright/test';

export const API = 'http://localhost:8080/api';
export const PASSWORD = 'password123';

/** Id corto y único para que cada test use datos propios y no choque con otros. */
export function uid(): string {
  return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;
}

/** Fecha local en formato YYYY-MM-DD, `days` días a partir de hoy. */
export function daysFromToday(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export interface TestUser {
  id: string;
  fullName: string;
  email: string;
  password: string;
  token: string;
}

/* ----------------------------- Preparación vía API ----------------------------- */
/* Se usa para dejar datos listos rápido; lo que se PRUEBA siempre se hace por UI.  */

export async function createUserViaApi(request: APIRequestContext, prefix = 'qa'): Promise<TestUser> {
  const id = uid();
  const body = { fullName: `${prefix} ${id}`, email: `${prefix}.${id}@example.com`, password: PASSWORD };
  const res = await request.post(`${API}/auth/register`, { data: body });
  expect(res.ok(), `registro API falló: ${res.status()}`).toBeTruthy();
  const json = await res.json();
  return { id: json.user.id, fullName: body.fullName, email: body.email, password: PASSWORD, token: json.accessToken };
}

const auth = (u: TestUser) => ({ Authorization: `Bearer ${u.token}` });

export async function createPetViaApi(
  request: APIRequestContext,
  user: TestUser,
  name: string,
  species = 'DOG',
): Promise<{ id: string; name: string }> {
  const res = await request.post(`${API}/pets`, { data: { name, species }, headers: auth(user) });
  expect(res.ok(), `crear mascota API falló: ${res.status()}`).toBeTruthy();
  return res.json();
}

export async function createCareViaApi(
  request: APIRequestContext,
  user: TestUser,
  data: { petId: string; care: string; careDate: string; assigneeEmail?: string | null; recurrenceIntervalDays?: number | null },
): Promise<{ id: string }> {
  const res = await request.post(`${API}/care-records`, {
    data: { assigneeEmail: null, recurrenceIntervalDays: null, ...data },
    headers: auth(user),
  });
  expect(res.ok(), `crear cuidado API falló: ${res.status()}`).toBeTruthy();
  return res.json();
}

/* --------------------------------- Acciones UI --------------------------------- */

export async function loginUi(page: Page, email: string, password = PASSWORD): Promise<void> {
  await fillLogin(page, email, password);
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

export async function fillLogin(page: Page, email: string, password: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Correo electrónico').fill(email);
  await page.getByLabel('Contraseña', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
}

export async function openUserMenu(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Abrir menú' }).click();
}

export async function logoutUi(page: Page): Promise<void> {
  await openUserMenu(page);
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await expect(page).toHaveURL(/\/(login|inicio)/);
}

export async function createPetUi(page: Page, name: string, speciesLabel = 'Perro', breed = ''): Promise<void> {
  await page.goto('/mis-mascotas');
  await page.getByRole('button', { name: /Registrar (mascota|mi primera mascota)/ }).first().click();
  await page.getByPlaceholder('Ej. Luna').fill(name);
  await page.getByLabel('Especie').selectOption({ label: speciesLabel });
  if (breed) await page.getByPlaceholder('Ej. Labrador').fill(breed);
  await page.getByRole('button', { name: 'Guardar mascota' }).click();
  await expect(page.getByText('Mascota registrada correctamente.')).toBeVisible();
}

export interface CareUiOptions {
  petName: string;
  speciesLabel?: string;
  care: string;
  date: string;
  assignee?: { email: string; fullName: string };
  repeatEveryDays?: number;
}

/** Rellena y guarda el formulario /cuidados/nuevo. Termina en el listado de cuidados. */
export async function createCareUi(page: Page, o: CareUiOptions): Promise<void> {
  await page.goto('/cuidados');
  await page.getByRole('link', { name: 'Registrar cuidado' }).first().click();
  await expect(page.getByRole('heading', { name: 'Registrar cuidado' })).toBeVisible();

  await page.locator('select.pet-select').selectOption({ label: `${o.petName} — ${o.speciesLabel ?? 'Perro'}` });
  await page.getByLabel('Cuidado', { exact: true }).fill(o.care);
  await page.getByLabel('Fecha', { exact: true }).fill(o.date);

  if (o.repeatEveryDays) {
    await page.getByLabel('Repetir este cuidado').check();
    await page.getByLabel('Repetir cada (días)').fill(String(o.repeatEveryDays));
  }

  if (o.assignee) {
    await page.getByLabel('Asignar este cuidado a otra persona').check();
    await page.getByLabel('Correo electrónico').fill(o.assignee.email);
    await page.getByRole('button', { name: new RegExp(o.assignee.email.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')) }).click();
    await expect(page.getByText(o.assignee.fullName)).toBeVisible();
  }

  await page.getByRole('button', { name: 'Guardar cuidado' }).click();
  await expect(page).toHaveURL(/\/cuidados(\?.*)?$/);
}

/** Tarjeta de un cuidado, identificada por texto único (nombre de mascota o texto del cuidado). */
export function careCard(page: Page, uniqueText: string): Locator {
  return page.locator('app-care-record-card').filter({ hasText: uniqueText });
}

export function petCard(page: Page, name: string): Locator {
  return page.locator('article.pet-card').filter({ hasText: name });
}