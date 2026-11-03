import { test, expect } from '@playwright/test';
import { login, crearCuidado, tarjeta } from './helpers';

test('Ana reprograma un cuidado y ve la fecha nueva ', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  await login(page);
  await crearCuidado(page, nombre, '2026-11-11');

  await page.getByRole('link', { name:new RegExp(`Editar cuidado de ${nombre}`) }).click();
  await page.getByLabel('Fecha').fill('2026-12-11');
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  
  await expect(tarjeta(page,nombre).getByText('2026-12-11')).toBeVisible();
});