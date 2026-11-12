import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AnalyticsService } from '../../core/analytics/analytics.service';
import { ApiError } from '../../models/auth.models';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { ThemeToggleComponent } from '../../shared/ui/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-forgot-password-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent, IconComponent, ThemeToggleComponent],
  template: `
    <main class="auth-page">
      <div class="auth-page__theme"><app-theme-toggle /></div>
      <section class="auth-card" aria-labelledby="recover-title">
        <div class="brand"><span class="brand__mark"><app-icon name="pets" [size]="23" /></span><span>Mascotas al Día</span></div>
        <div class="auth-heading">
          <h1 id="recover-title">Recuperar contraseña</h1>
          <p>Ingresa el correo de tu cuenta. Para esta entrega no se envía un correo real: continuaremos directamente con un token temporal.</p>
        </div>
        <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <app-form-field fieldId="recover-email" label="Correo electrónico" type="email" autocomplete="email" placeholder="tu@correo.com" [control]="form.controls.email" [error]="emailError()" />
          @if (serverError()) { <div class="form-alert" role="alert">{{ serverError() }}</div> }
          <app-button type="submit" [loading]="submitting()">Continuar</app-button>
        </form>
        <p class="auth-footer"><a routerLink="/login">Volver al inicio de sesión</a></p>
      </section>
    </main>
  `,
  styleUrl: '../auth-pages.css',
})
export class ForgotPasswordPage {
  private readonly auth = inject(AuthService);
  private readonly analytics = inject(AnalyticsService);
  private readonly router = inject(Router);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
  });

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.submitting.set(true);
    this.analytics.track('recuperacion_solicitada');
    this.auth.requestPasswordReset(this.form.getRawValue()).pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: (response) => this.router.navigate(['/restablecer-contrasena'], { queryParams: { token: response.resetToken } }),
      error: (error: HttpErrorResponse) => this.serverError.set((error.error as ApiError)?.message || 'No se pudo iniciar la recuperación.'),
    });
  }

  emailError(): string | null {
    const control = this.form.controls.email;
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['email']) return 'Ingresa un correo válido.';
    return 'Revisa este campo.';
  }
}
