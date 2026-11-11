import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnalyticsEventName, AnalyticsSummary } from '../../models/analytics.model';

const SESSION_KEY = 'analytics-session';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl;

  /**
   * Registra la acción, no a la persona: solo nombre del evento, propiedades mínimas
   * y una sesión anónima. Nunca rompe la app si falla.
   */
  track(event: AnalyticsEventName, props: Record<string, string> = {}): void {
    const record = { event, props, sessionId: this.sessionId() };
    console.log('[evento]', record);
    this.http.post<void>(`${this.url}/events`, record).subscribe({ error: () => {} });
  }

  summary(): Observable<AnalyticsSummary> {
    return this.http.get<AnalyticsSummary>(`${this.url}/analytics`);
  }

  private sessionId(): string {
    try {
      let id = sessionStorage.getItem(SESSION_KEY);
      if (!id) {
        id = Math.random().toString(16).slice(2, 8);
        sessionStorage.setItem(SESSION_KEY, id);
      }
      return id;
    } catch {
      return 'anon';
    }
  }
}
