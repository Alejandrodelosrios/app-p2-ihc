import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { AnalyticsService } from '../../core/analytics/analytics.service';
import { AnalyticsSummary } from '../../models/analytics.model';

@Component({
  selector: 'app-analytics-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './analytics.page.html',
  styleUrl: './analytics.page.css',
})
export class AnalyticsPage implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);

  readonly summary = signal<AnalyticsSummary | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.analyticsService
      .summary()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (summary) => this.summary.set(summary),
        error: () => this.errorMessage.set('No se pudo cargar la analítica'),
      });
  }

  propsText(props: Record<string, string>): string {
    const entries = Object.entries(props);
    return entries.length ? entries.map(([key, value]) => `${key}: ${value}`).join(', ') : '—';
  }
}
