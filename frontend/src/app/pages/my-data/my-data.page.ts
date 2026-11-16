import { Location } from '@angular/common'
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AccountService } from '../../core/account/account.service';
import { saveFile } from '../../core/account/save-file';
import { AnalyticsService } from '../../core/analytics/analytics.service';
import { DataFormat } from '../../models/account.model';

@Component({
  selector: 'app-my-data-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="data-page" aria-labelledby="data-title">
      <header>
        <p class="eyebrow">Tu cuenta</p>
        <h1 id="data-title">Mis datos</h1>
        <p class="muted">Descarga una copia de lo que Mascotas al Día guarda sobre ti.</p>
      </header>

      <div class="card">
        <h2>Qué incluye</h2>
        <ul>
          <li>Tu nombre, tu correo y la fecha en que creaste la cuenta.</li>
          <li>Tus mascotas.</li>
          <li>Los cuidados que creaste o que te asignaron.</li>
          <li>Tu historial de cuidados realizados o reprogramados.</li>
        </ul>
        <p class="muted">No incluye datos de otras personas. El CSV es una sola tabla; el JSON trae lo mismo con más estructura.</p>
      </div>

      @if (error()) { <div class="alert" role="alert">{{ error() }}</div> }
      @if (message()) { <div class="success" role="status">{{ message() }}</div> }

      <div class="actions">
        <button type="button" class="choice" (click)="download('json')" [disabled]="downloading() !== null">
          {{ downloading() === 'json' ? 'Preparando…' : 'Descargar JSON' }}
        </button>
        <button type="button" class="choice" (click)="download('csv')" [disabled]="downloading() !== null">
          {{ downloading() === 'csv' ? 'Preparando…' : 'Descargar CSV' }}
        </button>
      </div>

      <button type="button" class="choice choice--back" (click)="goBack()">Volver</button>
    </section>
  `,
  styles: [`
    .data-page { display: grid; gap: 1rem; max-width: 40rem; margin: 0 auto; padding: 1.25rem 1rem 6rem; }
    h1 { margin: 0 0 .35rem; font-size: clamp(1.6rem, 6vw, 2rem); letter-spacing: -.02em; }
    h2 { margin: 0 0 .5rem; font-size: 1.05rem; }
    .eyebrow { margin: 0 0 .25rem; color: var(--color-primary-strong); font-weight: 700; font-size: .85rem; }
    .muted { margin: 0; color: var(--color-text-muted); line-height: 1.5; }
    .muted a { color: var(--color-primary-strong); font-weight: 700; }
    .card { padding: 1rem 1.1rem; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); display: grid; gap: .5rem; }
    .card ul { margin: 0; padding-left: 1.2rem; display: grid; gap: .3rem; line-height: 1.5; }
    .alert { border-radius: var(--radius-md); padding: .75rem; color: var(--color-danger); background: var(--color-danger-soft); }
    .success { border-radius: var(--radius-md); padding: .75rem; color: var(--color-primary-strong); background: var(--color-primary-soft); }
    .actions { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
    .choice { box-sizing: border-box; min-height: 52px; padding: .75rem 1rem; border: 1px solid var(--color-border-strong); border-radius: var(--radius-md);
      background: var(--color-surface-2); color: var(--color-text); font: inherit; font-weight: 700; cursor: pointer; }
    .choice:hover:not(:disabled) { background: var(--color-surface-3); }
    .choice:focus-visible { outline: 3px solid var(--color-focus); outline-offset: 2px; }
    .choice:disabled { cursor: wait; opacity: .6; }
    .choice--back { width: 100%; }
    @media (max-width: 26rem) { .actions { grid-template-columns: 1fr; } }
  `],
})
export class MyDataPage {
  private readonly account = inject(AccountService);
  private readonly analytics = inject(AnalyticsService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  
  readonly downloading = signal<DataFormat | null>(null);
  readonly error = signal('');
  readonly message = signal('');

  download(format: DataFormat): void {
    this.error.set('');
    this.message.set('');
    this.downloading.set(format);
    this.account.download(format).pipe(finalize(() => this.downloading.set(null))).subscribe({
      next: (blob) => {
        saveFile(blob, `mis-datos-mascotas-al-dia.${format}`);
        this.analytics.track('datos_descargados', { formato: format });
        this.message.set(`Listo: se descargó tu archivo ${format.toUpperCase()}.`);
      },
      error: () => this.error.set('No pudimos preparar la descarga. Intenta nuevamente.'),
    });
  }

  /** Vuelve a la pantalla de la que llegó (p. ej. Eliminar cuenta); si abrió esta página directo, va al inicio. */
  goBack(): void {
    const cameFromApp = (history.state?.navigationId ?? 0) > 1;
    if (cameFromApp) {
      this.location.back();
    } else {
      void this.router.navigateByUrl('/dashboard');
    }
  }
}
