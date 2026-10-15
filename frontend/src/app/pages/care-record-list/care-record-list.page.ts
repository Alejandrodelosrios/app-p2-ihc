import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { CareRecordService } from '../../core/care/care-record';
import { CareRecord, CareRecordCareType, CareRecordFilters, CareRecordStatus } from '../../models/care-record.model';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { CareRecordCardComponent } from './care-record-card/care-record-card.component';

@Component({
  selector: 'app-care-record-list-page',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    IconComponent,
    CareRecordCardComponent,
  ],
  templateUrl: './care-record-list.page.html',
  styleUrl: './care-record-list.page.css',
})
export class CareRecordListPage implements OnInit {
  private readonly careRecordService = inject(CareRecordService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly records = signal<CareRecord[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly statusErrorMessage = signal('');
  readonly completingId = signal<string | null>(null);
  readonly deletingId = signal<string | null>(null);

  readonly filterForm = this.fb.group({
    petName: this.fb.nonNullable.control(''),
    status: this.fb.nonNullable.control<'' | CareRecordStatus>(''),
    careType: this.fb.nonNullable.control<'' | CareRecordCareType>(''),
    dateFilter: this.fb.nonNullable.control<'' | 'NEXT_7_DAYS' | 'THIS_MONTH' | 'RANGE'>(''),
    fromDate: this.fb.nonNullable.control(''),
    toDate: this.fb.nonNullable.control(''),
  });

  ngOnInit(): void {
    this.restoreFiltersFromUrl();
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.careRecordService
      .findAll(this.buildFilters())
      .pipe(
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (records) => {
          this.records.set(records);
        },
        error: () => {
          this.errorMessage.set(
            'No se pudieron cargar los cuidados',
          );
        },
      });
  }

  applyFilters(): void {
    this.syncFiltersToUrl();
    this.loadRecords();
  }

  clearFilters(): void {
    this.filterForm.reset({
      petName: '',
      status: '',
      careType: '',
      dateFilter: '',
      fromDate: '',
      toDate: '',
    });
    this.syncFiltersToUrl();
    this.loadRecords();
  }

  hasActiveFilters(): boolean {
    return this.activeFilters().length > 0;
  }

  activeFilters(): string[] {
    const value = this.filterForm.getRawValue();
    const active: string[] = [];

    if (value.petName.trim()) {
      active.push(`Mascota: ${value.petName.trim()}`);
    }
    if (value.status) {
      active.push(`Estado: ${value.status === 'PENDING' ? 'Pendiente' : 'Realizado'}`);
    }
    if (value.careType) {
      const labels = {
        VACCINE: 'Vacuna',
        MEDICATION: 'Medicamento',
        CONTROL: 'Control',
        OTHER: 'Otro',
      };
      active.push(`Tipo: ${labels[value.careType]}`);
    }
    if (value.dateFilter === 'NEXT_7_DAYS') {
      active.push('Fecha: Próximos 7 días');
    }
    if (value.dateFilter === 'THIS_MONTH') {
      active.push('Fecha: Este mes');
    }
    if (value.dateFilter === 'RANGE' && (value.fromDate || value.toDate)) {
      active.push(`Fecha: ${value.fromDate || '...'} a ${value.toDate || '...'}`);
    }

    return active;
  }

  private restoreFiltersFromUrl(): void {
    const params = this.route.snapshot.queryParamMap;

    this.filterForm.patchValue({
      petName: params.get('petName') ?? '',
      status: (params.get('status') as CareRecordStatus | null) ?? '',
      careType: (params.get('careType') as CareRecordCareType | null) ?? '',
      dateFilter: (params.get('dateFilter') as '' | 'NEXT_7_DAYS' | 'THIS_MONTH' | 'RANGE' | null) ?? '',
      fromDate: params.get('fromDate') ?? '',
      toDate: params.get('toDate') ?? '',
    });
  }

  private syncFiltersToUrl(): void {
    const value = this.filterForm.getRawValue();

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        petName: value.petName || null,
        status: value.status || null,
        careType: value.careType || null,
        dateFilter: value.dateFilter || null,
        fromDate: value.dateFilter === 'RANGE' ? value.fromDate || null : null,
        toDate: value.dateFilter === 'RANGE' ? value.toDate || null : null,
      },
      replaceUrl: true,
    });
  }

  private buildFilters(): CareRecordFilters {
    const value = this.filterForm.getRawValue();
    const filters: CareRecordFilters = {};

    if (value.petName.trim()) {
      filters.petName = value.petName.trim();
    }
    if (value.status) {
      filters.status = value.status;
    }
    if (value.careType) {
      filters.careType = value.careType;
    }

    if (value.dateFilter === 'NEXT_7_DAYS') {
      const from = new Date();
      const to = new Date(from);
      to.setDate(to.getDate() + 6);
      filters.fromDate = this.formatDate(from);
      filters.toDate = this.formatDate(to);
    }

    if (value.dateFilter === 'THIS_MONTH') {
      const today = new Date();
      filters.fromDate = this.formatDate(new Date(today.getFullYear(), today.getMonth(), 1));
      filters.toDate = this.formatDate(new Date(today.getFullYear(), today.getMonth() + 1, 0));
    }

    if (value.dateFilter === 'RANGE') {
      if (value.fromDate) {
        filters.fromDate = value.fromDate;
      }
      if (value.toDate) {
        filters.toDate = value.toDate;
      }
    }

    return filters;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  completeRecord(id: string): void {
    if (this.completingId() !== null) {
      return;
    }

    this.completingId.set(id);
    this.statusErrorMessage.set('');

    this.careRecordService
      .complete(id)
      .pipe(
        finalize(() => this.completingId.set(null)),
      )
      .subscribe({
        next: (updated) => {
          this.records.update((records) =>
            records.map((record) => record.id === updated.id ? updated : record),
          );
        },
        error: () => {
          this.statusErrorMessage.set('No se pudo marcar el cuidado como realizado');
        },
      });
  }

  deleteRecord(id: string): void {
    if (this.deletingId() !== null) {
      return;
    }

    this.deletingId.set(id);
    this.statusErrorMessage.set('');

    this.careRecordService
      .remove(id)
      .pipe(
        finalize(() => this.deletingId.set(null)),
      )
      .subscribe({
        next: () => {
          this.records.update((records) =>
            records.filter((record) => record.id !== id),
          );
        },
        error: () => {
          this.statusErrorMessage.set('No se pudo eliminar el cuidado');
        },
      });
  }
}
