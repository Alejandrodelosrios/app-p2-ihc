import { test, expect } from '@playwright/test';
import { PASSWORD, createUserViaApi, fillLogin, loginUi, logoutUi, uid } from './helpers';

test.describe('Registro', () => {
  test('un usuario nuevo se registra y entra a Mis mascotas', async ({ page }) => {
    const id = uid();
    await page.goto('/registro');
    await page.getByLabel('Nombre completo').fill(`Usuario QA ${id}`);
    await page.getByLabel('Correo electrónico').fill(`qa.${id}@example.com`);
    await page.getByLabel('Contraseña', { exact: true }).fill(PASSWORD);
    await page.getByLabel('Confirmar contraseña').fill(PASSWORD);
    await page.getByRole('button', { name: 'Crear cuenta' }).click();

    await expect(page).toHaveURL(/\/mis-mascotas/);
    await expect(page.getByRole('heading', { name: 'Mis mascotas' })).toBeVisible();
  });

  test('muestra errores de validación con el formulario vacío', async ({ page }) => {
    await page.goto('/registro');
    await page.getByRole('button', { name: 'Crear cuenta' }).click();
    await expect(page.getByText('Este campo es obligatorio.').first()).toBeVisible();
    await expect(page).toHaveURL(/\/registro/);
  });

  test('rechaza contraseñas que no coinciden', async ({ page }) => {
    const id = uid();
    await page.goto('/registro');
    await page.getByLabel('Nombre completo').fill('Usuario QA');
    await page.getByLabel('Correo electrónico').fill(`qa.${id}@example.com`);
    await page.getByLabel('Contraseña', { exact: true }).fill(PASSWORD);
    await page.getByLabel('Confirmar contraseña').fill('otra-clave-123');
    await page.getByRole('button', { name: 'Crear cuenta' }).click();
    await expect(page.getByText('Las contraseñas no coinciden.')).toBeVisible();
    await expect(page).toHaveURL(/\/registro/);
  });

  test('rechaza contraseña de menos de 8 caracteres', async ({ page }) => {
    await page.goto('/registro');
    await page.getByLabel('Nombre completo').fill('Usuario QA');
    await page.getByLabel('Correo electrónico').fill(`qa.${uid()}@example.com`);
    await page.getByLabel('Contraseña', { exact: true }).fill('corta');
    await page.getByLabel('Confirmar contraseña').fill('corta');
    await page.getByRole('button', { name: 'Crear cuenta' }).click();
    await expect(page.getByText('La contraseña debe tener al menos 8 caracteres.')).toBeVisible();
  });

  test('no permite registrar un correo que ya existe', async ({ page, request }) => {
    const existing = await createUserViaApi(request);
    await page.goto('/registro');
    await page.getByLabel('Nombre completo').fill('Otro Usuario');
    await page.getByLabel('Correo electrónico').fill(existing.email);
    await page.getByLabel('Contraseña', { exact: true }).fill(PASSWORD);
    await page.getByLabel('Confirmar contraseña').fill(PASSWORD);
    await page.getByRole('button', { name: 'Crear cuenta' }).click();
    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/registro/);
  });
});

test.describe('Login y sesión', () => {
  test('login correcto lleva al dashboard', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email, user.password);
    await expect(page.getByRole('link', { name: 'Cuidados' }).first()).toBeVisible();
  });

  test('login con contraseña incorrecta muestra error y no entra', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await fillLogin(page, user.email, 'incorrecta-123');
    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('login con campos vacíos muestra validación', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    await expect(page.getByText('Este campo es obligatorio.').first()).toBeVisible();
  });

  test('una ruta privada sin sesión redirige fuera de la app', async ({ page }) => {
    await page.goto('/cuidados');
    await expect(page).not.toHaveURL(/\/cuidados/);
  });

  test('cerrar sesión devuelve a la zona pública y bloquea rutas privadas', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await logoutUi(page);
    await page.goto('/dashboard');
    await expect(page).not.toHaveURL(/\/dashboard/);
  });

  test('la sesión persiste al recargar', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await loginUi(page, user.email);
    await page.reload();
    await expect(page).toHaveURL(/\/dashboard/);
  });
});

test.describe('Olvidaste tu contraseña', () => {
  test('flujo completo: pedir token, restablecer y entrar con la nueva clave', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    const nueva = 'NuevaClave456';

    await page.goto('/login');
    await page.getByRole('link', { name: '¿Olvidaste tu contraseña?' }).click();
    await expect(page.getByRole('heading', { name: 'Recuperar contraseña' })).toBeVisible();

    await page.getByLabel('Correo electrónico').fill(user.email);
    await page.getByRole('button', { name: 'Continuar' }).click();

    // La app avanza directo con un token temporal (no se envía correo real).
    await expect(page).toHaveURL(/\/restablecer-contrasena\?token=/);
    await page.getByLabel('Nueva contraseña').fill(nueva);
    await page.getByLabel('Confirmar contraseña').fill(nueva);
    await page.getByRole('button', { name: 'Guardar nueva contraseña' }).click();
    await expect(page.getByText('Contraseña actualizada correctamente.')).toBeVisible();

    await page.getByRole('link', { name: 'Iniciar sesión con la nueva contraseña' }).click();
    await loginUi(page, user.email, nueva);
  });

  test('la contraseña vieja deja de funcionar tras restablecerla', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await page.goto('/recuperar-contrasena');
    await page.getByLabel('Correo electrónico').fill(user.email);
    await page.getByRole('button', { name: 'Continuar' }).click();
    await page.getByLabel('Nueva contraseña').fill('NuevaClave456');
    await page.getByLabel('Confirmar contraseña').fill('NuevaClave456');
    await page.getByRole('button', { name: 'Guardar nueva contraseña' }).click();
    await expect(page.getByText('Contraseña actualizada correctamente.')).toBeVisible();

    await fillLogin(page, user.email, user.password);
    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('correo con formato inválido muestra validación', async ({ page }) => {
    await page.goto('/recuperar-contrasena');
    await page.getByLabel('Correo electrónico').fill('no-es-correo');
    await page.getByRole('button', { name: 'Continuar' }).click();
    await expect(page.getByText('Ingresa un correo válido.')).toBeVisible();
  });

  test('restablecer sin token muestra aviso', async ({ page }) => {
    await page.goto('/restablecer-contrasena');
    await expect(page.getByText('El enlace de recuperación no contiene un token válido.')).toBeVisible();
  });

  test('contraseñas distintas al restablecer muestran error', async ({ page, request }) => {
    const user = await createUserViaApi(request);
    await page.goto('/recuperar-contrasena');
    await page.getByLabel('Correo electrónico').fill(user.email);
    await page.getByRole('button', { name: 'Continuar' }).click();
    await page.getByLabel('Nueva contraseña').fill('NuevaClave456');
    await page.getByLabel('Confirmar contraseña').fill('Distinta12345');
    await page.getByRole('button', { name: 'Guardar nueva contraseña' }).click();
    await expect(page.getByText('Las contraseñas no coinciden.')).toBeVisible();
  });
});