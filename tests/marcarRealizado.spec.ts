import { test, expect } from '@playwright/test';
import {login,crearCuidado, tarjeta} from './helpers';

test('Ana marca un cuidado como realizado y sigue realizado al recargar', async ({ page }) => {
  const nombre = `Firulais ${Date.now()}`;
  await login(page);
  await crearCuidado(page,nombre);

  await page.getByRole('button', { name: new RegExp(`Marcar cuidado de ${nombre}`)}).click();
  await expect(tarjeta(page,nombre).getByText('Realizado')).toBeVisible();

  await page.reload();
  await expect(tarjeta(page,nombre).getByText('Realizado')).toBeVisible();
});