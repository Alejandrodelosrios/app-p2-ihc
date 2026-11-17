import { test, expect } from '@playwright/test';
import { createPetUi, createPetViaApi, createUserViaApi, loginUi, petCard, uid } from './helpers';

test.describe('Mascotas (CRUD)', () => {
  test('usuario nuevo ve el estado vacío', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');
    await expect(page.getByRole('heading', { name: 'Aún no tienes mascotas' })).toBeVisible();
  });

  test('crear una mascota y verla en el listado', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const name = `Luna ${uid()}`;
    await loginUi(page, user.email);
    await createPetUi(page, name, 'Gato', 'Siamés');

    const card = petCard(page, name);
    await expect(card).toBeVisible();
    await expect(card).toContainText('Gato');
    await expect(card).toContainText('Siamés');

    await page.reload();
    await expect(petCard(page, name)).toBeVisible();
  });

  test('no deja guardar sin nombre ni especie', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');
    await page.getByRole('button', { name: /Registrar (mascota|mi primera mascota)/ }).first().click();
    await page.getByRole('button', { name: 'Guardar mascota' }).click();
    await expect(page.getByText('Completa el nombre y la especie.')).toBeVisible();
  });

  test('editar una mascota actualiza nombre y especie', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const name = `Milo ${uid()}`;
    const renamed = `${name} editado`;
    await createPetViaApi(request, user, name, 'DOG');
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');

    await petCard(page, name).getByRole('button', { name: 'Editar' }).click();
    await expect(page.getByRole('heading', { name: 'Editar mascota' })).toBeVisible();
    await page.getByPlaceholder('Ej. Luna').fill(renamed);
    await page.getByLabel('Especie').selectOption({ label: 'Conejo' });
    await page.getByRole('button', { name: 'Guardar cambios' }).click();

    await expect(page.getByText('Mascota actualizada correctamente.')).toBeVisible();
    await expect(petCard(page, renamed)).toContainText('Conejo');
  });

  test('cancelar la eliminación conserva la mascota', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const name = `Coco ${uid()}`;
    await createPetViaApi(request, user, name);
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');

    page.once('dialog', (d) => d.dismiss());
    await petCard(page, name).getByRole('button', { name: 'Eliminar' }).click();
    await expect(petCard(page, name)).toBeVisible();
  });

  test('eliminar una mascota la quita del listado', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const name = `Rex ${uid()}`;
    await createPetViaApi(request, user, name);
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');

    page.once('dialog', (d) => d.accept());
    await petCard(page, name).getByRole('button', { name: 'Eliminar' }).click();
    await expect(page.getByText('Mascota eliminada correctamente.')).toBeVisible();
    await expect(petCard(page, name)).toHaveCount(0);

    await page.reload();
    await expect(petCard(page, name)).toHaveCount(0);
  });

  test('"Ver cuidados" lleva a la lista filtrada por esa mascota', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const name = `Nina ${uid()}`;
    const pet = await createPetViaApi(request, user, name);
    await loginUi(page, user.email);
    await page.goto('/mis-mascotas');
    await petCard(page, name).getByRole('link', { name: 'Ver cuidados' }).click();
    await expect(page).toHaveURL(new RegExp(`/cuidados\\?petId=${pet.id}`));
  });
});