import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AnalyticsService } from '../../core/analytics/analytics.service';
import { ApiError } from '../../models/auth.models';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { ThemeToggleComponent } from '../../shared/ui/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-reset-password-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent, IconComponent, ThemeToggleComponent],
  template: `
    <main class="auth-page">
      <div class="auth-page__theme"><app-theme-toggle /></div>
      <section class="auth-card" aria-labelledby="reset-title">
        <div class="brand"><span class="brand__mark"><app-icon name="pets" [size]="23" /></span><span>Mascotas al Día</span></div>
        <div class="auth-heading">
          <h1 id="reset-title">Nueva contraseña</h1>
          <p>Define una contraseña nueva para recuperar el acceso a tu cuenta.</p>
        </div>
        @if (!token) {
          <div class="form-alert" role="alert">El enlace de recuperación no contiene un token válido.</div>
          <p class="auth-footer"><a routerLink="/recuperar-contrasena">Solicitar otro token</a></p>
        } @else if (completed()) {
          <div class="form-success" role="status">Contraseña actualizada correctamente.</div>
          <p class="auth-footer"><a routerLink="/login">Iniciar sesión con la nueva contraseña</a></p>
        } @else {
          <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <app-form-field fieldId="reset-password" label="Nueva contraseña" type="password" autocomplete="new-password" placeholder="Mínimo 8 caracteres" [control]="form.controls.password" [error]="passwordError('password')" />
            <app-form-field fieldId="reset-confirm" label="Confirmar contraseña" type="password" autocomplete="new-password" placeholder="Repite la contraseña" [control]="form.controls.confirmPassword" [error]="passwordError('confirmPassword')" />
            @if (serverError()) { <div class="form-alert" role="alert">{{ serverError() }}</div> }
            <app-button type="submit" [loading]="submitting()">Guardar nueva contraseña</app-button>
          </form>
        }
      </section>
    </main>
  `,
  styleUrl: '../auth-pages.css',
})
export class ResetPasswordPage {
  private readonly auth = inject(AuthService);
  private readonly analytics = inject(AnalyticsService);
  private readonly route = inject(ActivatedRoute);
  readonly token = this.route.snapshot.queryParamMap.get('token');
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly completed = signal(false);

  readonly form = new FormGroup({
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
    confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.form.controls.password.value !== this.form.controls.confirmPassword.value) {
      this.form.controls.confirmPassword.setErrors({ mismatch: true });
      this.form.controls.confirmPassword.markAsTouched();
      return;
    }
    if (!this.token) return;

    this.submitting.set(true);
    this.auth.confirmPasswordReset({ token: this.token, newPassword: this.form.controls.password.value })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.analytics.track('contrasena_restablecida');
          this.completed.set(true);
        },
        error: (error: HttpErrorResponse) => this.serverError.set((error.error as ApiError)?.message || 'No se pudo restablecer la contraseña.'),
      });
  }

  passwordError(controlName: 'password' | 'confirmPassword'): string | null {
    const control = this.form.controls[controlName];
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['minlength']) return 'La contraseña debe tener al menos 8 caracteres.';
    if (control.errors['mismatch']) return 'Las contraseñas no coinciden.';
    return 'Revisa este campo.';
  }
}
