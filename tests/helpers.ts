import { Locator, Page, expect } from '@playwright/test';

export function daysFromToday(days: number): string {
  const date = new Date();
  date.setDate(date.getDate()+ days);
  const month = String(date.getMonth()+1).padStart(2,'0');
  const day = String(date.getDate()).padStart(2,'0');
  return `${date.getFullYear()}-${month}-${day}`;
}

export async function login(page: Page, email = 'ana@example.com', password = 'password123') {
  await page.goto('/inicio');
  await page.getByRole('link', { name: 'Iniciar sesión' }).click();
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill(email);
  await page.getByRole('textbox', { name: 'Contraseña' }).fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page.getByRole('link', { name: 'Cuidados' })).toBeVisible({ timeout: 15000 });
}

export async function crearCuidado(page: Page, nombre: string, fecha = daysFromToday(7)) {
  await page.getByRole('link', { name: 'Cuidados' }).click();
  await page.getByRole('link', { name: 'Registrar cuidado' }).click();
  await page.getByLabel('Nombre de la mascota').fill(nombre);
  await page.getByLabel('Cuidado', { exact: true }).fill('Control');
  await page.getByLabel('Tipo de animal').selectOption('Perro');
  await page.getByLabel('Fecha').fill(fecha);
  await page.getByRole('button', { name: 'Guardar cuidado' }).click();
  await expect(page.getByText(nombre)).toBeVisible();
}

// careCard de UN cuidado: el div más interno que contiene sus botones.
export function careCard(page: Page, nombre: string): Locator {
  return page
    .locator('app-care-record-card')
    .filter({ hasText: nombre});
}
// Alias temporal: para las specs que todavia dicen tarjeta(...) siguen funcionamientos 
export const tarjeta = careCard;