import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AccountService } from '../../core/account/account.service';
import { AnalyticsService } from '../../core/analytics/analytics.service';
import { AuthService } from '../../core/auth/auth.service';
import { DeletionSummary } from '../../models/account.model';
import { deletionConsequences } from './deletion-consequences';

const START_KEY = 'eliminar_cuenta_inicio_enviado';

@Component({
  selector: 'app-delete-account-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="delete-page" aria-labelledby="delete-title">
      <header>
        <p class="eyebrow">Tu cuenta</p>
        <h1 id="delete-title">Eliminar cuenta</h1>
      </header>

      @if (loading()) {
        <p class="muted" role="status">Calculando qué pasaría con tus datos…</p>
      } @else if (loadError()) {
        <div class="alert" role="alert">
          No pudimos calcular qué pasaría con tus datos, y no queremos que decidas sin saberlo.
          <button type="button" class="link" (click)="load()">Reintentar</button>
        </div>
        <div class="actions">
          <a class="choice" routerLink="/dashboard">Volver</a>
        </div>
      } @else {
        <p class="lead">Esto es lo que pasará si eliminas tu cuenta:</p>
        <ul class="consequences">
          @for (line of consequences(); track line) {
            <li>{{ line }}</li>
          }
        </ul>
        <p class="muted">
          ¿Quieres guardar una copia antes?
          <a routerLink="/mis-datos">Descargar mis datos</a>
        </p>

        @if (deleteError()) {
          <div class="alert" role="alert">{{ deleteError() }}</div>
        }

        <div class="actions">
          <button type="button" class="choice" (click)="keep()" [disabled]="deleting()">Conservar mi cuenta</button>
          <button type="button" class="choice choice--delete" (click)="confirmDelete()" [disabled]="deleting()">
            {{ deleting() ? 'Eliminando…' : 'Eliminar mi cuenta' }}
          </button>
        </div>
      }
    </section>
  `,
  styles: [`
    .delete-page { display: grid; gap: 1rem; max-width: 40rem; margin: 0 auto; padding: 1.25rem 1rem 6rem; }
    h1 { margin: 0; font-size: clamp(1.6rem, 6vw, 2rem); letter-spacing: -.02em; }
    .eyebrow { margin: 0 0 .25rem; color: var(--color-primary-strong); font-weight: 700; font-size: .85rem; }
    .lead { margin: 0; font-weight: 700; }
    .muted { margin: 0; color: var(--color-text-muted); }
    .muted a, .link { color: var(--color-primary-strong); font-weight: 700; }
    .consequences { margin: 0; padding: 1rem 1.1rem 1rem 2rem; display: grid; gap: .6rem; line-height: 1.5;
      background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); }
    .alert { border-radius: var(--radius-md); padding: .75rem; color: var(--color-danger); background: var(--color-danger-soft); }
    .link { margin-left: .5rem; padding: 0; border: 0; background: transparent; font: inherit; cursor: pointer; text-decoration: underline; }
    /* Mismo tamaño, misma tipografía y mismo peso visual: ninguna opción empuja a la otra. */
    .actions { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
    .choice { display: inline-flex; align-items: center; justify-content: center; box-sizing: border-box;
      min-height: 52px; padding: .75rem 1rem; border: 1px solid var(--color-border-strong); border-radius: var(--radius-md);
      background: var(--color-surface-2); color: var(--color-text); font: inherit; font-weight: 700; text-decoration: none; cursor: pointer; }
    .choice:hover:not(:disabled) { background: var(--color-surface-3); }
    .choice:focus-visible { outline: 3px solid var(--color-focus); outline-offset: 2px; }
    .choice:disabled { cursor: wait; opacity: .6; }
    .choice--delete { color: var(--color-danger); border-color: var(--color-danger); }
    @media (max-width: 26rem) { .actions { grid-template-columns: 1fr; } }
  `],
})
export class DeleteAccountPage implements OnInit {
  private readonly account = inject(AccountService);
  private readonly analytics = inject(AnalyticsService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly loadError = signal(false);
  readonly deleting = signal(false);
  readonly deleteError = signal('');
  readonly consequences = signal<string[]>([]);

  ngOnInit(): void {
    if(!this.startAlreadyTracked()){
      this.analytics.track('eliminar_cuenta_inicio');
    }
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.loadError.set(false);
    this.account.deletionSummary().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (summary: DeletionSummary) => this.consequences.set(deletionConsequences(summary)),
      error: () => this.loadError.set(true),
    });
  }

  keep(): void {
    this.clearStart();
    void this.router.navigateByUrl('/dashboard');
  }

  confirmDelete(): void {
    this.deleteError.set('');
    this.deleting.set(true);
    this.account.deleteAccount().subscribe({
      next: () => {
        // El evento de éxito lo registra el servidor: después de esto el token ya no sirve.
        this.clearStart();
        this.auth.logout();
        void this.router.navigate(['/login'], { queryParams: { cuentaEliminada: '1' } });
      },
      error: () => {
        this.deleting.set(false);
        this.deleteError.set('No pudimos eliminar tu cuenta. No se borró nada; puedes intentarlo de nuevo.');
      },
    });
  }

  private startAlreadyTracked(): boolean {
    try {
      if (sessionStorage.getItem(START_KEY) === '1') {
        return true;
      }
      sessionStorage.setItem(START_KEY, '1');
    } catch {
      // Sin sessionStorage se cuenta cada visita: es preferible a perder el evento.
    }
    return false;
  }

  private clearStart(): void {
    try {
      sessionStorage.removeItem(START_KEY);
    } catch {
      // Nada que limpiar.
    }
  }

}