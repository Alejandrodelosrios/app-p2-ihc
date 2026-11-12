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
  selector: 'app-register-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent, IconComponent, ThemeToggleComponent],
  templateUrl: './register.page.html',
  styleUrl: '../auth-pages.css',
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly analytics = inject(AnalyticsService);
  private readonly router = inject(Router);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email, Validators.maxLength(180)] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)] }),
    confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.analytics.track('registro_abierto');
  }

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid || this.form.controls.password.value !== this.form.controls.confirmPassword.value) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { fullName, email, password } = this.form.getRawValue();
    this.auth.register({ fullName, email, password }).pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => {
        this.analytics.track('registro_completado');
        this.router.navigateByUrl('/mis-mascotas');
      },
      error: (error: HttpErrorResponse) => this.serverError.set((error.error as ApiError)?.message || 'No se pudo crear la cuenta. Intenta nuevamente.'),
    });
  }

  errorFor(controlName: 'fullName' | 'email' | 'password' | 'confirmPassword'): string | null {
    const control = this.form.controls[controlName];
    if (!control.touched) return null;
    if (controlName === 'confirmPassword' && control.value !== this.form.controls.password.value) return 'Las contraseñas no coinciden.';
    if (!control.errors) return null;
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['email']) return 'Ingresa un correo válido.';
    if (control.errors['minlength']) return controlName === 'fullName' ? 'Ingresa al menos 2 caracteres.' : 'La contraseña debe tener al menos 8 caracteres.';
    if (control.errors['maxlength']) return 'El valor es demasiado largo.';
    return 'Revisa este campo.';
  }
}
