import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { PetService } from '../../core/pets/pet.service';
import { Pet, PetSpecies } from '../../models/pet.model';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { IconComponent } from '../../shared/ui/icon/icon.component';

const SPECIES: Array<{ value: PetSpecies; label: string }> = [
  { value: 'DOG', label: 'Perro' },
  { value: 'CAT', label: 'Gato' },
  { value: 'BIRD', label: 'Ave' },
  { value: 'RABBIT', label: 'Conejo' },
  { value: 'RODENT', label: 'Roedor' },
  { value: 'REPTILE', label: 'Reptil' },
  { value: 'FISH', label: 'Pez' },
  { value: 'OTHER', label: 'Otro' },
];

@Component({
  selector: 'app-my-pets-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, IconComponent],
  template: `
    <section class="pets-page">
      <header class="pets-page__heading">
        <div>
          <p class="eyebrow">Tu espacio</p>
          <h1>Mis mascotas</h1>
          <p class="welcome">Administra tus mascotas y consulta sus cuidados.</p>
        </div>
        <app-button type="button" (click)="showForm.set(!showForm())">
          <app-icon name="add" [size]="18" />
          {{ showForm() ? 'Cerrar' : 'Registrar mascota' }}
        </app-button>
      </header>

      @if (showForm()) {
        <form class="pet-form" [formGroup]="form" (ngSubmit)="savePet()">
          <h2>{{ editingId() ? 'Editar mascota' : 'Registrar mascota' }}</h2>
          <div class="pet-form__grid">
            <label><span>Nombre</span><input formControlName="name" placeholder="Ej. Luna" /></label>
            <label><span>Especie</span>
              <select formControlName="species">
                <option value="" disabled>Selecciona una especie</option>
                @for (species of speciesOptions; track species.value) {
                  <option [value]="species.value">{{ species.label }}</option>
                }
              </select>
            </label>
            <label><span>Raza (opcional)</span><input formControlName="breed" placeholder="Ej. Labrador" /></label>
            <label><span>Fecha de nacimiento (opcional)</span><input type="date" formControlName="birthDate" [max]="today" /></label>
          </div>
          @if (form.invalid && form.touched) {
            <p class="form-error" role="alert">Completa el nombre y la especie.</p>
          }
          @if (formError()) { <p class="form-error" role="alert">{{ formError() }}</p> }
          <div class="pet-form__actions">
            <app-button type="button" variant="secondary" (click)="cancelForm()">Cancelar</app-button>
            <app-button type="submit" [loading]="saving()">{{ editingId() ? 'Guardar cambios' : 'Guardar mascota' }}</app-button>
          </div>
        </form>
      }

      @if (loading()) {
        <div class="state-card"><app-icon name="progress_activity" [size]="32" /><p>Cargando mascotas...</p></div>
      } @else if (errorMessage()) {
        <div class="state-card state-card--error"><app-icon name="error" [size]="32" /><p>{{ errorMessage() }}</p><button type="button" (click)="loadPets()">Reintentar</button></div>
      } @else if (pets().length === 0) {
        <div class="state-card">
          <app-icon name="pets" [size]="42" />
          <h2>Aún no tienes mascotas</h2>
          <p>Registra la primera para empezar a organizar sus cuidados.</p>
          <button type="button" class="empty-action" (click)="showForm.set(true)">Registrar mi primera mascota</button>
        </div>
      } @else {
        @if (successMessage()) { <p class="success-message" role="status">{{ successMessage() }}</p> }
        <div class="pets-grid">
          @for (pet of pets(); track pet.id) {
            <article class="pet-card">
              <div class="pet-card__icon"><app-icon name="pets" [size]="28" /></div>
              <div><h2>{{ pet.name }}</h2><p>{{ speciesLabel(pet.species) }}{{ pet.breed ? ' · ' + pet.breed : '' }}</p></div>
              <div class="pet-card__actions">
                <a [routerLink]="['/cuidados']" [queryParams]="{ petName: pet.name }">Ver cuidados</a>
                <button type="button" (click)="editPet(pet)">Editar</button>
                <button type="button" class="pet-card__delete" (click)="deletePet(pet)" [disabled]="deletingId() === pet.id">
                  {{ deletingId() === pet.id ? 'Eliminando...' : 'Eliminar' }}
                </button>
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .pets-page { display: grid; gap: 1.4rem; }
    .pets-page__heading { display: flex; align-items: end; justify-content: space-between; gap: 1rem; }
    .pets-page__heading h1 { margin: .15rem 0 0; font-size: clamp(1.8rem, 8vw, 2.5rem); }
    .eyebrow { margin: 0; color: var(--color-primary); font-weight: 750; font-size: .85rem; text-transform: uppercase; letter-spacing: .08em; }
    .welcome { margin: .55rem 0 0; color: var(--color-text-muted); line-height: 1.5; }
    .pet-form, .pet-card, .state-card { padding: 1.25rem; border: 1px solid var(--color-border); border-radius: var(--radius-xl); background: var(--color-surface); box-shadow: var(--shadow-card); }
    .pet-form h2 { margin: 0 0 1rem; font-size: 1.15rem; }
    .pet-form__grid { display: grid; gap: 1rem; }
    label { display: grid; gap: .4rem; color: var(--color-text-muted); font-size: .9rem; font-weight: 650; }
    input, select { width: 100%; min-height: 44px; padding: .65rem .75rem; border: 1px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-surface); color: var(--color-text); }
    .pet-form__actions { display: flex; justify-content: end; gap: .75rem; margin-top: 1rem; }
    .form-error { color: var(--color-danger); font-size: .9rem; }
    .state-card { display: grid; place-items: center; align-content: center; gap: .5rem; min-height: 220px; text-align: center; color: var(--color-text-muted); }
    .state-card h2, .state-card p { margin: 0; }
    .state-card--error { color: var(--color-danger); }
    .state-card button, .empty-action { border: 0; border-radius: var(--radius-md); padding: .7rem 1rem; background: var(--color-primary); color: var(--color-on-primary); cursor: pointer; font-weight: 700; text-decoration: none; }
    .pets-grid { display: grid; gap: 1rem; }
    .pet-card { display: grid; grid-template-columns: auto 1fr; align-items: center; gap: 1rem; }
    .pet-card__icon { display: grid; place-items: center; width: 3rem; height: 3rem; border-radius: 1rem; color: var(--color-primary); background: var(--color-primary-soft); }
    .pet-card h2 { margin: 0; font-size: 1.1rem; }
    .pet-card p { margin: .25rem 0 0; color: var(--color-text-muted); }
    .pet-card__actions { grid-column: 2; display: flex; flex-wrap: wrap; align-items: center; gap: .75rem; }
    .pet-card__actions a, .pet-card__actions button { color: var(--color-primary-strong); font: inherit; font-size: .9rem; font-weight: 700; text-decoration: none; }
    .pet-card__actions button { padding: 0; border: 0; background: transparent; cursor: pointer; }
    .pet-card__actions button:disabled { cursor: wait; opacity: .6; }
    .pet-card__actions .pet-card__delete { color: var(--color-danger); }
    .success-message { color: var(--color-primary-strong); }
    @media (min-width: 42rem) { .pet-form__grid { grid-template-columns: 1fr 1fr; } .pets-grid { grid-template-columns: 1fr 1fr; } .pet-card__actions { grid-column: auto; } }
  `],
})
export class MyPetsPage implements OnInit {
  private readonly petService = inject(PetService);
  private readonly fb = inject(FormBuilder);

  readonly speciesOptions = SPECIES;
  readonly pets = signal<Pet[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly deletingId = signal<string | null>(null);
  readonly editingId = signal<string | null>(null);
  readonly showForm = signal(false);
  readonly errorMessage = signal('');
  readonly formError = signal('');
  readonly successMessage = signal('');
  readonly today = new Date().toISOString().slice(0, 10);
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    species: ['' as PetSpecies | '', Validators.required],
    breed: ['', Validators.maxLength(100)],
    birthDate: [''],
  });

  ngOnInit(): void { this.loadPets(); }

  loadPets(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.petService.findAll().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (pets) => this.pets.set(pets),
      error: () => this.errorMessage.set('No se pudieron cargar tus mascotas.'),
    });
  }

  savePet(): void {
    this.formError.set('');
    this.successMessage.set('');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    this.saving.set(true);
    const request = {
      name: value.name.trim(),
      species: value.species as PetSpecies,
      breed: value.breed.trim() || null,
      birthDate: value.birthDate || null,
    };
    const operation = this.editingId()
      ? this.petService.update(this.editingId()!, request)
      : this.petService.create(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (pet) => {
        if (this.editingId()) {
          this.pets.update((pets) => pets.map((current) => current.id === pet.id ? pet : current));
          this.successMessage.set('Mascota actualizada correctamente.');
        } else {
          this.pets.update((pets) => [pet, ...pets]);
          this.successMessage.set('Mascota registrada correctamente.');
        }
        this.resetForm();
        this.showForm.set(false);
      },
      error: (error: HttpErrorResponse) => this.formError.set(
        error.error?.message || (this.editingId()
          ? 'No se pudo actualizar la mascota.'
          : 'No se pudo registrar la mascota.'),
      ),
    });
  }

  editPet(pet: Pet): void {
    this.formError.set('');
    this.successMessage.set('');
    this.editingId.set(pet.id);
    this.form.patchValue({
      name: pet.name,
      species: pet.species,
      breed: pet.breed ?? '',
      birthDate: pet.birthDate ?? '',
    });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.resetForm();
    this.showForm.set(false);
  }

  deletePet(pet: Pet): void {
    if (this.deletingId() !== null) {
      return;
    }

    if (!confirm(`¿Eliminar a ${pet.name}? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.formError.set('');
    this.successMessage.set('');
    this.deletingId.set(pet.id);
    this.petService.remove(pet.id).pipe(finalize(() => this.deletingId.set(null))).subscribe({
      next: () => {
        this.pets.update((pets) => pets.filter((current) => current.id !== pet.id));
        if (this.editingId() === pet.id) {
          this.cancelForm();
        }
        this.successMessage.set('Mascota eliminada correctamente.');
      },
      error: (error: HttpErrorResponse) => this.formError.set(
        error.error?.message || 'No se pudo eliminar la mascota.',
      ),
    });
  }

  private resetForm(): void {
    this.editingId.set(null);
    this.form.reset({ name: '', species: '', breed: '', birthDate: '' });
    this.formError.set('');
  }

  speciesLabel(species: PetSpecies): string {
    return SPECIES.find((item) => item.value === species)?.label ?? 'Otro';
  }
}
