import { Page, expect } from '@playwright/test';

export async function login(page: Page, email = 'ana@example.com', password = 'password123') {
  await page.goto('/inicio');
  await page.getByRole('link', { name: 'Iniciar sesión' }).click();
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill(email);
  await page.getByRole('textbox', { name: 'Contraseña' }).fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page.getByRole('link', { name: 'Cuidados' })).toBeVisible({ timeout: 15000 });
}

export async function crearCuidado(page: Page, nombre: string, fecha = '2026-11-11') {
  await page.getByRole('link', { name: 'Cuidados' }).click();
  await page.getByRole('link', { name: 'Registrar cuidado' }).click();
  await page.getByLabel('Nombre de la mascota').fill(nombre);
  await page.getByLabel('Cuidado', { exact: true }).fill('Control');
  await page.getByLabel('Tipo de animal').selectOption('Perro');
  await page.getByLabel('Fecha').fill(fecha);
  await page.getByRole('button', { name: 'Guardar cuidado' }).click();
  await expect(page.getByText(nombre)).toBeVisible();
}

// Tarjeta de UN cuidado: el div más interno que contiene sus botones.
export function tarjeta(page: Page, nombre: string) {
  return page
    .locator('div')
    .filter({ has: page.getByRole('button', { name: new RegExp(`cuidado de ${nombre}`) }) })
    .last();
}