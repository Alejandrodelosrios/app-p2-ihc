import { test, expect } from '@playwright/test';
import {login,crearCuidado} from './helpers';

test('Ana cancela la eliminacion y el  cuidado se mantiene', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  await login(page);
  await crearCuidado(page,nombre);

  page.once('dialog',d => d.dismiss());
  await page.getByRole('button', { name: new RegExp(`Eliminar cuidado de ${nombre}`) }).click();
  
  await expect(page.getByText(nombre)).toBeVisible();
});

test('Ana elimina un cuidado y desaparece de la lista', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  await login(page);
  await crearCuidado(page, nombre);

  page.once('dialog', d => d.accept());
  await page.getByRole('button', { name: new RegExp(`Eliminar cuidado de ${nombre}`) }).click();

  await expect(page.getByText(nombre)).not.toBeVisible();
});