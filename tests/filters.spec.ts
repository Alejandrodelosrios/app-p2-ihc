import { test, expect } from '@playwright/test';
import {
  careCard, createCareViaApi, createPetViaApi, createUserViaApi, daysFromToday, loginUi, uid,
} from './helpers';

/**
 * Datos: dos mascotas y tres cuidados con tipo, fecha y estado distintos.
 *   A: "Vacuna triple"  (en 3 días)    -> VACCINE
 *   B: "Control anual"  (en 30 días)   -> CONTROL
 *   B: "Medicamento X"  (en 2 días, luego marcado como realizado) -> MEDICATION / COMPLETED
 */
async function seedFilterData(request: Parameters<typeof createUserViaApi>[0]) {
  const user = await createUserViaApi(request);
  const id = uid();
  const petA = await createPetViaApi(request, user, `Alfa${id}`, 'DOG');
  const petB = await createPetViaApi(request, user, `Beta${id}`, 'CAT');
  const vacuna = `Vacuna triple ${id}`;
  const control = `Control anual ${id}`;
  const medic = `Medicamento X ${id}`;

  await createCareViaApi(request, user, { petId: petA.id, care: vacuna, careDate: daysFromToday(3) });
  await createCareViaApi(request, user, { petId: petB.id, care: control, careDate: daysFromToday(30) });
  const done = await createCareViaApi(request, user, { petId: petB.id, care: medic, careDate: daysFromToday(2) });
  const res = await request.patch(`http://localhost:8080/api/care-records/${done.id}/complete`, {
    headers: { Authorization: `Bearer ${user.token}` },
  });
  expect(res.ok()).toBeTruthy();

  return { user, petA: petA.name, petB: petB.name, vacuna, control, medic };
}

const apply = (page: import('@playwright/test').Page) =>
  page.getByRole('button', { name: 'Aplicar filtros' }).click();

test.describe('Filtros de cuidados', () => {
  test('sin filtros se ven todos los cuidados', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');
    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.control)).toBeVisible();
    await expect(careCard(page, d.medic)).toBeVisible();
  });

  test('filtrar por nombre de mascota', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.getByPlaceholder('Buscar por nombre').fill(d.petA);
    await apply(page);

    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.control)).toHaveCount(0);
    await expect(page.getByText(`Mascota: ${d.petA}`)).toBeVisible();
  });

  test('filtrar por tipo (Vacuna)', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.getByRole('button', { name: 'Mostrar más' }).click();
    await page.locator('select[formcontrolname="careType"]').selectOption('VACCINE');
    await apply(page);

    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.control)).toHaveCount(0);
    await expect(careCard(page, d.medic)).toHaveCount(0);
  });

  test('filtrar por estado (Realizado)', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.getByRole('button', { name: 'Mostrar más' }).click();
    await page.locator('select[formcontrolname="status"]').selectOption('COMPLETED');
    await apply(page);

    await expect(careCard(page, d.medic)).toBeVisible();
    await expect(careCard(page, d.vacuna)).toHaveCount(0);
    await expect(careCard(page, d.control)).toHaveCount(0);
  });

  test('filtrar por fecha: próximos 7 días', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.locator('select[formcontrolname="dateFilter"]').selectOption('NEXT_7_DAYS');
    await apply(page);

    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.medic)).toBeVisible();
    await expect(careCard(page, d.control)).toHaveCount(0); // está a 30 días
  });

  test('filtrar por rango de fechas', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.locator('select[formcontrolname="dateFilter"]').selectOption('RANGE');
    await page.getByLabel('Desde').fill(daysFromToday(25));
    await page.getByLabel('Hasta').fill(daysFromToday(35));
    await apply(page);

    await expect(careCard(page, d.control)).toBeVisible();
    await expect(careCard(page, d.vacuna)).toHaveCount(0);
  });

  test('sin coincidencias muestra "Sin resultados" y se puede limpiar', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.getByPlaceholder('Buscar por nombre').fill('mascota-que-no-existe');
    await apply(page);
    await expect(page.getByRole('heading', { name: 'Sin resultados' })).toBeVisible();

    await page.getByRole('button', { name: 'Limpiar filtros' }).first().click();
    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.control)).toBeVisible();
  });

  test('los filtros quedan en la URL y sobreviven a recargar', async ({ page, request }) => {
    const d = await seedFilterData(request);
    await loginUi(page, d.user.email);
    await page.goto('/cuidados');

    await page.getByPlaceholder('Buscar por nombre').fill(d.petA);
    await apply(page);
    await expect(page).toHaveURL(/petName=/);

    await page.reload();
    await expect(page.getByPlaceholder('Buscar por nombre')).toHaveValue(d.petA);
    await expect(careCard(page, d.vacuna)).toBeVisible();
    await expect(careCard(page, d.control)).toHaveCount(0);
  });
});