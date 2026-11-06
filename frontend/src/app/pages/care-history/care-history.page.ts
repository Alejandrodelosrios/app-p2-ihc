import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { CareHistoryService } from '../../core/care/care-history';
import { CareHistoryEntry } from '../../models/care-history.model';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-history-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './care-history.page.html',
  styleUrl: './care-history.page.css',
})
export class CareHistoryPage implements OnInit {
  private readonly historyService = inject(CareHistoryService);
  private readonly fb = inject(FormBuilder);

  readonly entries = signal<CareHistoryEntry[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly pageSize = 8;
  readonly currentPage = signal(1);
  readonly filterForm = this.fb.nonNullable.group({
    petName: '',
    care: '',
    action: '' as '' | CareHistoryEntry['action'],
    fromDate: '',
    toDate: '',
  });
  readonly filteredEntries = computed(() => {
    const filters = this.filterForm.getRawValue();
    return this.entries().filter((entry) => {
      const petMatches = !filters.petName
        || entry.petName.toLocaleLowerCase().includes(filters.petName.trim().toLocaleLowerCase());
      const careMatches = !filters.care
        || entry.care.toLocaleLowerCase().includes(filters.care.trim().toLocaleLowerCase());
      const actionMatches = !filters.action || entry.action === filters.action;
      const fromMatches = !filters.fromDate || entry.occurredAt.slice(0, 10) >= filters.fromDate;
      const toMatches = !filters.toDate || entry.occurredAt.slice(0, 10) <= filters.toDate;
      return petMatches && careMatches && actionMatches && fromMatches && toMatches;
    });
  });
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filteredEntries().length / this.pageSize)));
  readonly paginatedEntries = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredEntries().slice(start, start + this.pageSize);
  });

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.historyService.findAll()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (entries) => {
          this.entries.set(entries);
          this.currentPage.set(1);
        },
        error: () => this.errorMessage.set('No se pudo cargar el historial'),
      });
  }

  applyFilters(): void {
    this.currentPage.set(1);
  }

  clearFilters(): void {
    this.filterForm.reset({
      petName: '',
      care: '',
      action: '',
      fromDate: '',
      toDate: '',
    });
    this.currentPage.set(1);
  }

  previousPage(): void {
    this.currentPage.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.currentPage.update((page) => Math.min(this.totalPages(), page + 1));
  }
}
