import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { CareHistoryService } from '../../core/care/care-history';
import { CareHistoryEntry } from '../../models/care-history.model';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-history-page',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent],
  templateUrl: './care-history.page.html',
  styleUrl: './care-history.page.css',
})
export class CareHistoryPage implements OnInit {
  private readonly historyService = inject(CareHistoryService);

  readonly entries = signal<CareHistoryEntry[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.historyService.findAll()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (entries) => this.entries.set(entries),
        error: () => this.errorMessage.set('No se pudo cargar el historial'),
      });
  }
}
