import { test, expect } from '@playwright/test';
import { login } from './helpers';

test('Ana crea un cuidado y asigna a Alex', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  await login(page);

  await page.getByRole('link', { name: 'Cuidados' }).click();
  await page.getByRole('link', { name: 'Registrar cuidado' }).click();
  await page.getByLabel('Nombre de la mascota').fill(nombre);
  await page.getByLabel('Cuidado').fill('control');
  await page.getByLabel('Tipo de animal un').selectOption('Perro');
  await page.getByLabel('Fecha').fill('2026-11-22');
  await page.getByRole('checkbox', { name: '/Asignar este cuidado a otra/' }).check();
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill('ale');
  await page.getByRole('button', { name: 'alex alex@example.com' }).click();
  await page.getByRole('button', { name: 'Guardar cuidado' }).click();
  
  await expect(page.getByText(nombre)).toBeVisible()
  await expect(page.getByText(/alex/i).first()).toBeVisible();
});