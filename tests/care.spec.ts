import { test, expect } from '@playwright/test';
import {
  careCard, createCareUi, createCareViaApi, createPetViaApi, createUserViaApi,
  daysFromToday, loginUi, logoutUi, uid,
} from './helpers';

/** Cada test arranca con un usuario nuevo que ya tiene una mascota. */
async function setup(request: Parameters<typeof createUserViaApi>[0]) {
  const user = await createUserViaApi(request);
  const petName = `Firulais ${uid()}`;
  const pet = await createPetViaApi(request, user, petName, 'DOG');
  return { user, petName, pet };
}

test.describe('Crear cuidado', () => {
  test('crear un cuidado y verlo en "Mis cuidados"', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await createCareUi(page, { petName, care: 'Vacuna de refuerzo', date: daysFromToday(5) });

    const card = careCard(page, petName);
    await expect(card).toBeVisible();
    await expect(card).toContainText('Vacuna de refuerzo');
    await expect(card).toContainText(daysFromToday(5));
    await expect(card).toContainText('Pendiente');
  });

  test('sin mascotas el formulario pide registrar una', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await expect(page.getByText(/No tienes mascotas registradas/)).toBeVisible();
    await expect(page.getByRole('button', { name: 'Guardar cuidado' })).toBeDisabled();
  });

  test('no permite una fecha pasada', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.locator('select.pet-select').selectOption({ label: `${petName} — Perro` });
    await page.getByLabel('Cuidado', { exact: true }).fill('Control');
    await page.getByLabel('Fecha', { exact: true }).fill(daysFromToday(-3));
    await page.getByRole('button', { name: 'Guardar cuidado' }).click();
    await expect(page.getByText('La fecha no puede ser anterior a hoy', { exact: true })).toBeVisible();
    await expect(page).toHaveURL(/\/cuidados\/nuevo/);
  });

  test('campos obligatorios vacíos muestran errores', async ({ page, request }) => {
    const { user } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.getByRole('button', { name: 'Guardar cuidado' }).click();
    await expect(page.getByText('Selecciona una mascota')).toBeVisible();
    await expect(page.getByText('Ingresa el cuidado realizado')).toBeVisible();
    await expect(page.getByText('Selecciona una fecha')).toBeVisible();
  });

  test('"Cancelar" vuelve al listado sin crear nada', async ({ page, request }) => {
    const { user } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.getByRole('button', { name: 'Cancelar' }).click();
    await expect(page).toHaveURL(/\/cuidados$/);
    await expect(page.getByRole('heading', { name: 'Aún no hay cuidados' })).toBeVisible();
  });
});

test.describe('Editar y eliminar cuidado', () => {
  test('editar cambia el texto y reprograma la fecha', async ({ page, request }) => {
    const { user, petName, pet } = await setup(request);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Control inicial', careDate: daysFromToday(7) });
    await loginUi(page, user.email);
    await page.goto('/cuidados');

    const nuevaFecha = daysFromToday(20);
    await page.getByRole('link', { name: `Editar cuidado de ${petName}` }).click();
    await expect(page.getByRole('heading', { name: 'Editar cuidado' })).toBeVisible();
    await page.getByLabel('Cuidado', { exact: true }).fill('Control actualizado');
    await page.getByLabel('Fecha', { exact: true }).fill(nuevaFecha);
    await page.getByRole('button', { name: 'Guardar cambios' }).click();

    await expect(page).toHaveURL(/\/cuidados/);
    const card = careCard(page, petName);
    await expect(card).toContainText('Control actualizado');
    await expect(card).toContainText(nuevaFecha);

    await page.reload();
    await expect(careCard(page, petName)).toContainText(nuevaFecha);
  });

  test('cancelar la eliminación conserva el cuidado', async ({ page, request }) => {
    const { user, petName, pet } = await setup(request);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Control', careDate: daysFromToday(3) });
    await loginUi(page, user.email);
    await page.goto('/cuidados');

    page.once('dialog', (d) => d.dismiss());
    await page.getByRole('button', { name: `Eliminar cuidado de ${petName}` }).click();
    await expect(careCard(page, petName)).toBeVisible();
  });

  test('eliminar quita el cuidado y no vuelve al recargar', async ({ page, request }) => {
    const { user, petName, pet } = await setup(request);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Control', careDate: daysFromToday(3) });
    await loginUi(page, user.email);
    await page.goto('/cuidados');

    page.once('dialog', (d) => d.accept());
    await page.getByRole('button', { name: `Eliminar cuidado de ${petName}` }).click();
    await expect(careCard(page, petName)).toHaveCount(0);

    await page.reload();
    await expect(careCard(page, petName)).toHaveCount(0);
  });
});

test.describe('Marcar como realizado', () => {
  test('queda "Realizado" y persiste al recargar', async ({ page, request }) => {
    const { user, petName, pet } = await setup(request);
    await createCareViaApi(request, user, { petId: pet.id, care: 'Desparasitación', careDate: daysFromToday(2) });
    await loginUi(page, user.email);
    await page.goto('/cuidados');

    await page.getByRole('button', { name: `Marcar cuidado de ${petName} como realizado` }).click();
    await expect(careCard(page, petName).getByText('Realizado', { exact: true })).toBeVisible();
    await expect(careCard(page, petName).getByRole('button', { name: 'Este cuidado ya fue realizado' })).toBeDisabled();

    await page.reload();
    await expect(careCard(page, petName).getByText('Realizado', { exact: true })).toBeVisible();
  });
});

test.describe('Asignar un cuidado a otra persona', () => {
  test('se asigna a otro usuario y esa persona lo ve y lo realiza', async ({ page, request }) => {
    const { user: owner, petName } = await setup(request);
    const assignee = await createUserViaApi(request, 'asignado');

    // 1) El dueño crea el cuidado y lo asigna (búsqueda por correo + selección de la lista).
    await loginUi(page, owner.email);
    await createCareUi(page, {
      petName,
      care: 'Control mensual',
      date: daysFromToday(4),
      assignee: { email: assignee.email, fullName: assignee.fullName },
    });
    await expect(careCard(page, petName)).toContainText(`Asignado a ${assignee.fullName}`);

    // 2) La persona asignada lo ve en "Cuidados asignados", sin editar ni eliminar.
    await logoutUi(page);
    await loginUi(page, assignee.email, assignee.password);
    await page.goto('/cuidados');
    const card = careCard(page, petName);
    await expect(card).toContainText(`Te asignó ${owner.fullName}`);
    await expect(page.getByRole('link', { name: `Editar cuidado de ${petName}` })).toHaveCount(0);
    await expect(page.getByRole('button', { name: `Eliminar cuidado de ${petName}` })).toHaveCount(0);

    // 3) La persona asignada lo marca como realizado.
    await page.getByRole('button', { name: `Marcar cuidado de ${petName} como realizado` }).click();
    await expect(card.getByText('Realizado', { exact: true })).toBeVisible();
  });

  test('si se activa la asignación sin elegir a nadie, no guarda', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.locator('select.pet-select').selectOption({ label: `${petName} — Perro` });
    await page.getByLabel('Cuidado', { exact: true }).fill('Control');
    await page.getByLabel('Fecha', { exact: true }).fill(daysFromToday(3));
    await page.getByLabel('Asignar este cuidado a otra persona').check();
    await page.getByRole('button', { name: 'Guardar cuidado' }).click();
    await expect(page.getByText(/Selecciona a la persona responsable/)).toBeVisible();
    await expect(page).toHaveURL(/\/cuidados\/nuevo/);
  });

  test('buscar un correo inexistente muestra "sin coincidencias"', async ({ page, request }) => {
    const { user } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.getByLabel('Asignar este cuidado a otra persona').check();
    await page.getByLabel('Correo electrónico').fill(`nadie.${uid()}@no-existe.com`);
    await expect(page.getByText(/No se encontraron coincidencias/)).toBeVisible();
  });
});

test.describe('Cuidado recurrente', () => {
  test('se crea con repetición y muestra "Se repite cada N días"', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await createCareUi(page, { petName, care: 'Medicamento diario', date: daysFromToday(2), repeatEveryDays: 3 });
    await expect(careCard(page, petName)).toContainText('Se repite cada 3 días');
  });

  test('al marcarlo realizado se reprograma y sigue pendiente', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await createCareUi(page, { petName, care: 'Medicamento semanal', date: daysFromToday(2), repeatEveryDays: 7 });

    await page.getByRole('button', { name: `Marcar cuidado de ${petName} como realizado` }).click();
    await expect(page.getByText(/Cuidado realizado\. Próxima fecha:/)).toBeVisible();

    const card = careCard(page, petName);
    await expect(card).toContainText('Pendiente');
    await expect(card).toContainText('Se repite cada 7 días');
    // La fecha de la tarjeta ya no es la original.
    await expect(card).not.toContainText(daysFromToday(2));
  });

  test('repetición fuera de rango (0 días) no deja guardar', async ({ page, request }) => {
    const { user, petName } = await setup(request);
    await loginUi(page, user.email);
    await page.goto('/cuidados/nuevo');
    await page.locator('select.pet-select').selectOption({ label: `${petName} — Perro` });
    await page.getByLabel('Cuidado', { exact: true }).fill('Control');
    await page.getByLabel('Fecha', { exact: true }).fill(daysFromToday(3));
    await page.getByLabel('Repetir este cuidado').check();
    await page.getByLabel('Repetir cada (días)').fill('0');
    await page.getByRole('button', { name: 'Guardar cuidado' }).click();
    await expect(page.getByText(/entre 1 y 365/)).toBeVisible();
    await expect(page).toHaveURL(/\/cuidados\/nuevo/);
  });
});