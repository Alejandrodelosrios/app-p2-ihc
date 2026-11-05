import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, of, switchMap, tap } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { CareRecordService } from '../../core/care/care-record';
import { AuthUser } from '../../models/auth.models';
import { CareRecord as CareRecordModel, CreateCareRecordRequest } from '../../models/care-record.model';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { FormFieldComponent } from '../../shared/ui/form-field/form-field.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';

const MIN_SEARCH_LENGTH = 3;
const MAX_RECURRENCE_DAYS = 365;

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
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly editId = this.route.snapshot.paramMap.get('id');
  readonly editing = this.editId !== null;
  private readonly currentDate = new Date();
  readonly today = `${this.currentDate.getFullYear()}-${String(this.currentDate.getMonth() + 1).padStart(2, '0')}-${String(this.currentDate.getDate()).padStart(2, '0')}`;
  readonly animalTypes = ['Perro', 'Gato', 'Loro', 'Conejo', 'Hámster', 'Pez', 'Tortuga', 'Otro'];
  readonly maxRecurrenceDays = MAX_RECURRENCE_DAYS;

  readonly form = this.fb.nonNullable.group({
    petName: ['', Validators.required],
    care: ['', Validators.required],
    animalType: ['', Validators.required],
    careDate: ['', Validators.required],
  });

  // Recurrencia
  readonly repeats = signal(false);
  readonly recurrenceDays = new FormControl<number | null>(7, {
    validators: [Validators.required, Validators.min(1), Validators.max(MAX_RECURRENCE_DAYS), Validators.pattern(/^\d+$/)],
  });

  // Asignación
  readonly assignToOther = signal(false);
  readonly selectedAssignee = signal<AuthUser | null>(null);
  readonly assigneeSearch = new FormControl('', { nonNullable: true });
  readonly assigneeResults = signal<AuthUser[]>([]);
  readonly searchingAssignees = signal(false);
  readonly assigneeSearchError = signal('');
  /** false mientras se carga el responsable actual al editar (evita borrarlo sin querer). */
  readonly assigneeReady = signal(true);

  constructor() {
    const record = this.recordFromState();

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

      if (record.recurrenceIntervalDays) {
        this.repeats.set(true);
        this.recurrenceDays.setValue(record.recurrenceIntervalDays);
      }

      if (record.assignedToUserId) {
        this.loadCurrentAssignee(record.assignedToUserId);
      }
    }

    this.setupAssigneeSearch();
  }

  /** `history.state` puede ser null (recarga de página, pruebas), por eso se lee de forma segura. */
  private recordFromState(): CareRecordModel | undefined {
    return (history.state as { record?: CareRecordModel } | null)?.record;
  }

  canReschedule(): boolean {
    const record = this.recordFromState();
    return record?.status !== 'COMPLETED';
  }

  toggleRecurrence(event: Event): void {
    this.repeats.set((event.target as HTMLInputElement).checked);
  }

  toggleAssignment(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.assignToOther.set(checked);

    if (!checked) {
      this.clearAssignee();
      this.assigneeSearch.setValue('');
      this.assigneeResults.set([]);
      this.assigneeSearchError.set('');
    }
  }

  selectAssignee(user: AuthUser): void {
    this.selectedAssignee.set(user);
    this.assigneeResults.set([]);
    this.assigneeSearch.setValue('', { emitEvent: false });
    this.assigneeSearchError.set('');
  }

  clearAssignee(): void {
    this.selectedAssignee.set(null);
  }

  cancel(): void {
    this.router.navigate(['/cuidados'], { queryParamsHandling: 'preserve' });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.assigneeReady()) {
      return;
    }

    if (this.assignToOther() && !this.selectedAssignee()) {
      this.errorMessage.set('Selecciona a la persona responsable de la lista o desactiva la asignación.');
      return;
    }

    const recurrenceActive = this.repeats() && this.canReschedule();
    if (recurrenceActive && this.recurrenceDays.invalid) {
      this.recurrenceDays.markAsTouched();
      return;
    }

    this.loading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    // Importante: el backend reemplaza asignado y recurrencia en cada PUT, por eso siempre se envían.
    const request: CreateCareRecordRequest = {
      ...this.form.getRawValue(),
      assigneeEmail: this.assignToOther() ? this.selectedAssignee()?.email ?? null : null,
      recurrenceIntervalDays: recurrenceActive ? Number(this.recurrenceDays.value) : this.completedRecurrence(),
    };
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
            this.router.navigate(['/cuidados'], { queryParamsHandling: 'preserve' });
            return;
          }

          this.successMessage.set('Cuidado registrado correctamente');
          this.router.navigate(['/cuidados']);
        },
        error: (error: HttpErrorResponse) => {
          const fallback = this.editing
            ? 'No se pudo actualizar el cuidado'
            : 'No se pudo registrar el cuidado';
          this.errorMessage.set(error.error?.message || fallback);
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

  recurrenceError(): string | null {
    const control = this.recurrenceDays;

    if (!control.touched || control.valid) {
      return null;
    }

    return `Ingresa un número entero de días entre 1 y ${MAX_RECURRENCE_DAYS}`;
  }

  /** Un cuidado ya realizado no admite cambios de recurrencia: se reenvía el valor original. */
  private completedRecurrence(): number | null {
    if (this.canReschedule()) {
      return null;
    }
    const record = this.recordFromState();
    return record?.recurrenceIntervalDays ?? null;
  }

  private loadCurrentAssignee(userId: string): void {
    this.assignToOther.set(true);
    this.assigneeReady.set(false);

    this.authService.findUser(userId).subscribe({
      next: (user) => {
        this.selectedAssignee.set(user);
        this.assigneeReady.set(true);
      },
      error: () => {
        this.errorMessage.set('No se pudo cargar el responsable actual. Vuelve a la lista e inténtalo de nuevo.');
      },
    });
  }

  private setupAssigneeSearch(): void {
    this.assigneeSearch.valueChanges
      .pipe(
        map((value) => value.trim()),
        distinctUntilChanged(),
        debounceTime(300),
        tap((query) => {
          this.assigneeSearchError.set('');
          this.searchingAssignees.set(query.length >= MIN_SEARCH_LENGTH);
          if (query.length < MIN_SEARCH_LENGTH) {
            this.assigneeResults.set([]);
          }
        }),
        switchMap((query) =>
          query.length < MIN_SEARCH_LENGTH
            ? of<AuthUser[]>([])
            : this.authService.searchUsers(query).pipe(
                catchError(() => {
                  this.assigneeSearchError.set('No se pudo buscar usuarios. Inténtalo de nuevo.');
                  return of<AuthUser[]>([]);
                }),
              ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((users) => {
        this.assigneeResults.set(users);
        this.searchingAssignees.set(false);
      });
  }
}