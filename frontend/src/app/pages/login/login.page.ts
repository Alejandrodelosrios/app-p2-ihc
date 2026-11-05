import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ApiError } from '../../models/auth.models';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { ThemeToggleComponent } from '../../shared/ui/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent, IconComponent, ThemeToggleComponent],
  templateUrl: './login.page.html',
  styleUrl: '../auth-pages.css',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly passwordChanged = this.route.snapshot.queryParamMap.get('passwordChanged') === '1';

  readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
  });

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.submitting.set(true);
    this.auth.login(this.form.getRawValue()).pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('redirect') || '/dashboard'),
      error: (error: HttpErrorResponse) => this.serverError.set((error.error as ApiError)?.message || 'No se pudo iniciar sesión. Intenta nuevamente.'),
    });
  }

  errorFor(controlName: 'email' | 'password'): string | null {
    const control = this.form.controls[controlName];
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['email']) return 'Ingresa un correo válido.';
    if (control.errors['minlength']) return 'La contraseña debe tener al menos 8 caracteres.';
    return 'Revisa este campo.';
  }
}
