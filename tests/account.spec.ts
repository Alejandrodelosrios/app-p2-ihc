import { test, expect } from '@playwright/test';
import {
  createCareViaApi, createPetViaApi, createUserViaApi, daysFromToday,
  fillLogin, loginUi, openUserMenu, uid,
} from './helpers';

async function goToDeleteAccount(page: import('@playwright/test').Page) {
  await openUserMenu(page);
  await page.getByRole('link', { name: 'Eliminar cuenta' }).click();
  await expect(page.getByRole('heading', { name: 'Eliminar cuenta' })).toBeVisible();
}

test.describe('Eliminar cuenta', () => {
  test('muestra las consecuencias antes de decidir', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const pet = await createPetViaApi(request, user, `Pelusa ${uid()}`);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Control', careDate: daysFromToday(4) });
    await loginUi(page, user.email);
    await goToDeleteAccount(page);

    await expect(page.getByText('Esto es lo que pasará si eliminas tu cuenta:')).toBeVisible();
    await expect(page.locator('ul.consequences li').first()).toBeVisible();
    await expect(page.getByRole('link', { name: 'Descargar mis datos' })).toBeVisible();
  });

  test('"Conservar mi cuenta" vuelve al dashboard y la cuenta sigue activa', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await goToDeleteAccount(page);
    await page.getByRole('button', { name: 'Conservar mi cuenta' }).click();
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('eliminar la cuenta cierra la sesión e impide volver a entrar', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const pet = await createPetViaApi(request, user, `Pelusa ${uid()}`);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Control', careDate: daysFromToday(4) });
    await loginUi(page, user.email);
    await goToDeleteAccount(page);

    await page.getByRole('button', { name: 'Eliminar mi cuenta' }).click();

    await expect(page).toHaveURL(/\/login\?cuentaEliminada=1/);
    await expect(page.getByText('Tu cuenta fue eliminada. Ya no se puede iniciar sesión con ella.')).toBeVisible();

    // Ya no puede entrar con esas credenciales.
    await fillLogin(page, user.email, user.password);
    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);

    // Y las rutas privadas siguen cerradas.
    await page.goto('/mis-mascotas');
    await expect(page).not.toHaveURL(/\/mis-mascotas/);
  });
});