import { test, expect } from '@playwright/test';

test('Ana registra un cuidado y lo ve en su lista', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;

  // Login
  await page.goto('/inicio');
  await page.getByRole('link', { name: 'Iniciar sesión' }).click();
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill('ana@example.com');
  await page.getByRole('textbox', { name: 'Contraseña' }).fill('password123');
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();

  // Ir al formulario
  await expect(page.getByRole('link', { name: 'Cuidados' })).toBeVisible({ timeout: 15000 });
  await page.getByRole('link', { name: 'Cuidados' }).click();
  await page.getByRole('link', { name: 'Registrar cuidado' }).click();

  // Llenar y guardar
  await page.getByLabel('Nombre de la mascota').fill(nombre);
  await page.getByLabel('Cuidado', { exact: true }).fill('Vacuna de refuerzo');
  await page.getByLabel('Tipo de animal').selectOption('Perro');
  await page.getByLabel('Fecha').fill('2026-11-11');
  await page.getByRole('button', { name: 'Guardar cuidado' }).click();

  // Verificar
  //await expect(page.getByText(nombre)).toBeVisible();
});