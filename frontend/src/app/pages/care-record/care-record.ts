import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, distinctUntilChanged, finalize, of, switchMap } from 'rxjs';

import { CareRecordService } from '../../core/care/care-record';
import { UserDirectoryService } from '../../core/auth/user-directory.service';
import { CareRecord as CareRecordModel, CreateCareRecordRequest } from '../../models/care-record.model';
import { UserDirectoryEntry } from '../../models/user-directory.model';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-record',
  imports: [ReactiveFormsModule, ButtonComponent, FormFieldComponent, IconComponent],
  templateUrl: './care-record.html',
  styleUrl: './care-record.css',
})
export class CareRecord {
  private readonly fb = inject(FormBuilder);
  private readonly careRecordService = inject(CareRecordService);
  private readonly userDirectoryService = inject(UserDirectoryService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly assignToOther = signal(false);
  readonly selectedAssignee = signal<UserDirectoryEntry | null>(null);
  readonly assigneeResults = signal<UserDirectoryEntry[]>([]);
  readonly searchingAssignees = signal(false);
  readonly assigneeSearchError = signal('');
  readonly assigneeSearch = new FormControl('', { nonNullable: true });
  readonly editId = this.route.snapshot.paramMap.get('id');
  readonly editing = this.editId !== null;
  private readonly currentDate = new Date();
  readonly today = `${this.currentDate.getFullYear()}-${String(this.currentDate.getMonth() + 1).padStart(2, '0')}-${String(this.currentDate.getDate()).padStart(2, '0')}`;
  readonly animalTypes = ['Perro', 'Gato', 'Loro', 'Conejo', 'Hámster', 'Pez', 'Tortuga', 'Otro'];

  readonly form = this.fb.nonNullable.group({
    petName: ['', Validators.required],
    care: ['', Validators.required],
    animalType: ['', Validators.required],
    careDate: ['', Validators.required],
  });

  constructor() {
    const record = history.state['record'] as CareRecordModel | undefined;

    if (this.editing && record) {
      if (!this.animalTypes.includes(record.animalType)) {
        this.animalTypes.push(record.animalType);
      }

      this.form.patchValue({
        petName: record.petName,
        care: record.care,
        animalType: record.animalType,
        careDate: record.careDate,
      });

      if (record.assignedToUserId) {
        this.assignToOther.set(true);
        this.userDirectoryService.findById(record.assignedToUserId).subscribe({
          next: (user) => {
            this.selectedAssignee.set(user);
            this.assigneeSearch.setValue(user.email, { emitEvent: false });
          },
          error: () => this.assigneeSearchError.set('No se pudo cargar el responsable actual.'),
        });
      }
    }

    this.assigneeSearch.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((value) => {
        const email = value.trim();
        this.assigneeSearchError.set('');
        if (email.length < 3) {
          this.searchingAssignees.set(false);
          this.assigneeResults.set([]);
          return of([] as UserDirectoryEntry[]);
        }
        this.searchingAssignees.set(true);
        return this.userDirectoryService.searchByEmail(email).pipe(
          catchError(() => {
            this.assigneeSearchError.set('No se pudo buscar usuarios. Inténtalo de nuevo.');
            return of([] as UserDirectoryEntry[]);
          }),
        );
      }),
      takeUntilDestroyed(),
    ).subscribe((users) => {
      this.assigneeResults.set(users);
      this.searchingAssignees.set(false);
    });
  }

  toggleAssignment(event: Event): void {
    const enabled = (event.target as HTMLInputElement).checked;
    this.assignToOther.set(enabled);
    this.assigneeSearchError.set('');
    if (!enabled) {
      this.selectedAssignee.set(null);
      this.assigneeSearch.setValue('', { emitEvent: false });
      this.assigneeResults.set([]);
    }
  }

  selectAssignee(user: UserDirectoryEntry): void {
    this.selectedAssignee.set(user);
    this.assigneeSearch.setValue(user.email, { emitEvent: false });
    this.assigneeResults.set([]);
    this.assigneeSearchError.set('');
  }

  clearAssignee(): void {
    this.selectedAssignee.set(null);
    this.assigneeSearch.setValue('', { emitEvent: false });
  }

  canReschedule(): boolean {
    const record = history.state['record'] as CareRecordModel | undefined;
    return record?.status !== 'COMPLETED';
  }

  cancel(): void {
    this.router.navigate(['/cuidados'], { queryParamsHandling: 'preserve' });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.assignToOther() && !this.selectedAssignee()) {
      this.assigneeSearchError.set('Selecciona un usuario de la lista para asignar el cuidado.');
      return;
    }

    this.loading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const request: CreateCareRecordRequest = {
      ...this.form.getRawValue(),
      assignedToUserId: this.assignToOther() ? this.selectedAssignee()?.email ?? null : null,
    };
    const operation = this.editId
      ? this.careRecordService.update(this.editId, request)
      : this.careRecordService.create(request);

    operation.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: () => {
        if (this.editing) {
          this.router.navigate(['/cuidados'], { queryParamsHandling: 'preserve' });
          return;
        }
        this.successMessage.set('Cuidado registrado correctamente');
        this.router.navigate(['/cuidados']);
      },
      error: () => {
        this.errorMessage.set(this.editing ? 'No se pudo actualizar el cuidado' : 'No se pudo registrar el cuidado');
      },
    });
  }

  petNameError(): string | null {
    const control = this.form.controls.petName;
    return control.touched && control.hasError('required') ? 'Ingresa el nombre de la mascota' : null;
  }

  careError(): string | null {
    const control = this.form.controls.care;
    return control.touched && control.hasError('required') ? 'Ingresa el cuidado realizado' : null;
  }

  animalTypeError(): string | null {
    const control = this.form.controls.animalType;
    return control.touched && control.hasError('required') ? 'Ingresa el tipo de animal' : null;
  }

  careDateError(): string | null {
    const control = this.form.controls.careDate;
    return control.touched && control.hasError('required') ? 'Selecciona una fecha' : null;
  }
}
