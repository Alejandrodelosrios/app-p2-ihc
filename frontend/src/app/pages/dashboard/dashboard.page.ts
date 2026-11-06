import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';

import { CareRecordService } from '../../core/care/care-record';
import { PetService } from '../../core/pets/pet.service';
import { CareRecord } from '../../models/care-record.model';
import { Pet, PetSpecies } from '../../models/pet.model';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [RouterLink, IconComponent],
  template: `
    <section class="dashboard">
      <header class="dashboard__heading">
        <div><p class="eyebrow">Resumen</p><h1>Tu dashboard</h1><p>Consulta rápidamente el estado de tus mascotas y cuidados.</p></div>
        <div class="dashboard__actions"><a routerLink="/mis-mascotas">Mis mascotas</a><a routerLink="/cuidados/nuevo">Registrar cuidado</a></div>
      </header>
      @if (loading()) {
        <div class="state-card"><app-icon name="progress_activity" [size]="32" /><p>Cargando resumen...</p></div>
      } @else if (errorMessage()) {
        <div class="state-card state-card--error"><app-icon name="error" [size]="32" /><p>{{ errorMessage() }}</p><button type="button" (click)="loadDashboard()">Reintentar</button></div>
      } @else {
        <section class="summary-grid" aria-label="Resumen de cuidados">
          <article><span>Mascotas</span><strong>{{ pets().length }}</strong></article>
          <article><span>Cuidados pendientes</span><strong>{{ pendingCount() }}</strong></article>
          <article><span>Próximos</span><strong>{{ upcomingCount() }}</strong></article>
          <article><span>Vencidos</span><strong>{{ overdueCount() }}</strong></article>
        </section>
        <section class="pets-section" aria-labelledby="dashboard-pets-title">
          <div class="section-heading"><div><p class="eyebrow">Tus compañeras</p><h2 id="dashboard-pets-title">Mis mascotas</h2></div><a routerLink="/mis-mascotas">Ver todas</a></div>
          @if (pets().length === 0) {
            <div class="empty-card"><app-icon name="pets" [size]="34" /><p>Aún no tienes mascotas registradas.</p><a routerLink="/mis-mascotas">Registrar la primera</a></div>
          } @else {
            <div class="pets-list">@for (pet of pets(); track pet.id) { <a class="pet-item" [routerLink]="['/cuidados']" [queryParams]="{ petId: pet.id }"><span class="pet-item__icon"><app-icon name="pets" [size]="23" /></span><span><strong>{{ pet.name }}</strong><small>{{ speciesLabel(pet.species) }}</small></span><app-icon name="chevron_right" [size]="20" /></a> }</div>
          }
        </section>
      }
    </section>
  `,
  styles: [`
    .dashboard { display: grid; gap: 1.5rem; }
    .dashboard__heading, .section-heading { display: flex; justify-content: space-between; align-items: end; gap: 1rem; }
    h1 { margin: .15rem 0 0; font-size: clamp(1.8rem, 8vw, 2.5rem); } h2 { margin: .15rem 0 0; }
    .dashboard__heading p:not(.eyebrow) { margin: .55rem 0 0; color: var(--color-text-muted); }
    .eyebrow { margin: 0; color: var(--color-primary); font-weight: 750; font-size: .85rem; text-transform: uppercase; letter-spacing: .08em; }
    .dashboard__actions { display: flex; flex-wrap: wrap; gap: .5rem; } .dashboard a { color: var(--color-primary-strong); font-weight: 700; text-decoration: none; }
    .dashboard__actions a { padding: .7rem .85rem; border-radius: var(--radius-md); background: var(--color-primary); color: var(--color-on-primary); }
    .summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: .75rem; } .summary-grid article { display: grid; gap: .35rem; padding: 1rem; border: 1px solid var(--color-border); border-radius: var(--radius-lg); background: var(--color-surface); } .summary-grid span { color: var(--color-text-muted); font-size: .85rem; } .summary-grid strong { font-size: 1.7rem; }
    .pets-section { display: grid; gap: 1rem; } .section-heading a { font-size: .9rem; } .pets-list { display: grid; gap: .65rem; } .pet-item { display: flex; align-items: center; gap: .75rem; padding: .85rem 1rem; border: 1px solid var(--color-border); border-radius: var(--radius-lg); background: var(--color-surface); } .pet-item span:nth-child(2) { display: grid; flex: 1; gap: .2rem; } .pet-item small { color: var(--color-text-muted); } .pet-item__icon { display: grid; place-items: center; width: 2.5rem; height: 2.5rem; border-radius: .8rem; color: var(--color-primary); background: var(--color-primary-soft); }
    .empty-card, .state-card { display: grid; place-items: center; gap: .5rem; padding: 2rem 1rem; border: 1px dashed var(--color-border-strong); border-radius: var(--radius-xl); color: var(--color-text-muted); text-align: center; } .empty-card p { margin: 0; } .state-card { min-height: 220px; } .state-card--error { color: var(--color-danger); } .state-card button, .empty-card a { border: 0; border-radius: var(--radius-md); padding: .65rem .9rem; background: var(--color-primary); color: var(--color-on-primary); cursor: pointer; }
    @media (min-width: 42rem) { .summary-grid { grid-template-columns: repeat(4, 1fr); } }
  `],
})
export class DashboardPage implements OnInit {
  private readonly petService = inject(PetService);
  private readonly careService = inject(CareRecordService);
  readonly pets = signal<Pet[]>([]);
  readonly records = signal<CareRecord[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  private readonly today = new Date().toISOString().slice(0, 10);
  readonly pendingCount = computed(() => this.records().filter((record) => record.status === 'PENDING').length);
  readonly upcomingCount = computed(() => this.records().filter((record) => record.status === 'PENDING' && record.careDate >= this.today && record.careDate <= this.addDays(7)).length);
  readonly overdueCount = computed(() => this.records().filter((record) => record.status === 'PENDING' && record.careDate < this.today).length);

  ngOnInit(): void { this.loadDashboard(); }
  loadDashboard(): void {
    this.loading.set(true); this.errorMessage.set('');
    forkJoin({ pets: this.petService.findAll(), records: this.careService.findAll() }).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (result) => { this.pets.set(result.pets); this.records.set(result.records); },
      error: () => this.errorMessage.set('No se pudo cargar el resumen.'),
    });
  }
  speciesLabel(species: PetSpecies): string { return ({ DOG: 'Perro', CAT: 'Gato', BIRD: 'Ave', RABBIT: 'Conejo', RODENT: 'Roedor', REPTILE: 'Reptil', FISH: 'Pez', OTHER: 'Otro' } satisfies Record<PetSpecies, string>)[species] ?? 'Otro'; }
  private addDays(days: number): string { const date = new Date(); date.setDate(date.getDate() + days); return date.toISOString().slice(0, 10); }
}
