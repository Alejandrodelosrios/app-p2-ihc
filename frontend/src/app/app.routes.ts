import { Routes } from '@angular/router';
import { authGuard, publicOnlyGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'inicio',
    loadComponent: () =>
      import('./pages/home/home.page').then((m) => m.HomePage),
  },
  {
    path: 'login',
    canActivate: [publicOnlyGuard],
    loadComponent: () =>
      import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'registro',
    canActivate: [publicOnlyGuard],
    loadComponent: () =>
      import('./pages/register/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'recuperar-contrasena',
    canActivate: [publicOnlyGuard],
    loadComponent: () =>
      import('./pages/forgot-password/forgot-password.page').then(
        (m) => m.ForgotPasswordPage,
      ),
  },
  {
    path: 'restablecer-contrasena',
    canActivate: [publicOnlyGuard],
    loadComponent: () =>
      import('./pages/reset-password/reset-password.page').then(
        (m) => m.ResetPasswordPage,
      ),
  },

  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'inicio',
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/app-shell/app-shell.component').then(
        (m) => m.AppShellComponent,
      ),
    children: [
      {
        path: 'mis-mascotas',
        loadComponent: () =>
          import('./pages/my-pets/my-pets.page').then((m) => m.MyPetsPage),
      },

      {
        path: 'cuidados',
        children: [
          {
            path: '',
            pathMatch: 'full',
            loadComponent: () =>
              import('./pages/care-record-list/care-record-list.page').then(
                (m) => m.CareRecordListPage,
              ),
          },
          {
            path: 'nuevo',
            loadComponent: () =>
              import('./pages/care-record/care-record').then(
                (m) => m.CareRecord,
              ),
          },
          {
            path: ':id/editar',
            loadComponent: () =>
              import('./pages/care-record/care-record').then(
                (m) => m.CareRecord,
              ),
          },
        ],
      },

      {
        path: 'historial',
        loadComponent: () =>
          import('./pages/care-history/care-history.page').then(
            (m) => m.CareHistoryPage,
          ),
      },

      {
        path: 'cambiar-contrasena',
        loadComponent: () =>
          import('./pages/change-password/change-password.page').then(
            (m) => m.ChangePasswordPage,
          ),
      },
    ],
  },

  {
    path: '**',
    redirectTo: 'inicio',
  },
];