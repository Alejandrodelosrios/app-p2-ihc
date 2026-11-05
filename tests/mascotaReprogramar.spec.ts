import { test, expect } from '@playwright/test';
import { login, crearCuidado, tarjeta, daysFromToday } from './helpers';

test('Ana reprograma un cuidado y ve la fecha nueva ', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  const nuevaFecha =daysFromToday(20);
  await login(page);
  await crearCuidado(page, nombre,daysFromToday(7));

  await page.getByRole('link', { name:new RegExp(`Editar cuidado de ${nombre}`) }).click();
  await page.getByLabel('Fecha').fill(nuevaFecha);
  await page.getByRole('button', { name: 'Guardar cambios' }).click();
  
  await expect(tarjeta(page,nombre).getByText(nuevaFecha)).toBeVisible();
});