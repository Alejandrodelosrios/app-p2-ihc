import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { CareRecordService } from '../../core/care/care-record';
import { CareRecord as CareRecordModel, CareRecordStatus, CreateCareRecordRequest } from '../../models/care-record.model';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-record',
  imports: [
    ReactiveFormsModule,
    ButtonComponent,
    FormFieldComponent,
    IconComponent,
  ],
  templateUrl: './care-record.html',
  styleUrl: './care-record.css',
})
export class CareRecord {
  private readonly fb = inject(FormBuilder);
  private readonly careRecordService = inject(CareRecordService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly editId = this.route.snapshot.paramMap.get('id');
  readonly editing = this.editId !== null;
  private readonly currentDate = new Date();
  readonly today = `${this.currentDate.getFullYear()}-${String(this.currentDate.getMonth() + 1).padStart(2, '0')}-${String(this.currentDate.getDate()).padStart(2, '0')}`;

  readonly form = this.fb.nonNullable.group({
    petName: ['', Validators.required],
    care: ['', Validators.required],
    animalType: ['', Validators.required],
    careDate: ['', Validators.required],
  });

  constructor() {
    const record = history.state['record'] as CareRecordModel | undefined;

    if (this.editing && record) {
      this.form.patchValue({
        petName: record.petName,
        care: record.care,
        animalType: record.animalType,
        careDate: record.careDate,
      });
    }
  }

  canReschedule(): boolean {
      const record = history.state['record'] as CareRecordModel | undefined;

      if (record?.status === "COMPLETED") {
        return false;
      }
      return true;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const request: CreateCareRecordRequest = this.form.getRawValue();
    const operation = this.editId
      ? this.careRecordService.update(this.editId, request)
      : this.careRecordService.create(request);

    operation
      .pipe(
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: () => {
          if (this.editing) {
            this.router.navigate(['/cuidados']);
            return;
          }

          this.successMessage.set('Cuidado registrado correctamente');
          this.form.reset();
        },
        error: () => {
          this.errorMessage.set(
            this.editing
              ? 'No se pudo actualizar el cuidado'
              : 'No se pudo registrar el cuidado',
          );
        },
      });
  }

  petNameError(): string | null {
    const control = this.form.controls.petName;

    if (control.touched && control.hasError('required')) {
      return 'Ingresa el nombre de la mascota';
    }

    return null;
  }

  careError(): string | null {
    const control = this.form.controls.care;

    if (control.touched && control.hasError('required')) {
      return 'Ingresa el cuidado realizado';
    }

    return null;
  }

  animalTypeError(): string | null {
    const control = this.form.controls.animalType;

    if (control.touched && control.hasError('required')) {
      return 'Ingresa el tipo de animal';
    }

    return null;
  }

  careDateError(): string | null {
    const control = this.form.controls.careDate;

    if (control.touched && control.hasError('required')) {
      return 'Selecciona una fecha';
    }

    return null;
  }
}
