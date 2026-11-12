import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import {
  CareRecord,
  SKIP_REASONS,
  careStatusLabel,
  isCareDone,
  isCareOverdue,
  todayLocal,
} from '../../../models/care-record.model';
import { PetSpecies } from '../../../models/pet.model';
import { IconComponent } from '../../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-record-card',
  standalone: true,
  imports: [IconComponent, RouterLink, FormsModule],
  templateUrl: './care-record-card.component.html',
  styleUrl: './care-record-card.component.css',
})
export class CareRecordCardComponent {
  readonly record = input.required<CareRecord>();
  readonly completing = input(false);
  readonly deleting = input(false);
  readonly assignedToMe = input(false);
  readonly personName = input<string | null>(null);
  readonly complete = output<string>();
  readonly remove = output<string>();

  readonly completeLate = output<{ id: string; completedOn: string }>();
  readonly skip = output<{ id: string; reason: string }>();
  readonly resolving = input(false);

  readonly skipReasons = SKIP_REASONS;
  readonly panel = signal<'late' | 'skip' | null>(null);
  readonly lateDate = signal(todayLocal());
  readonly skipReason = signal<string>('');

  readonly overdue = computed(() => isCareOverdue(this.record()));
  readonly done = computed(() => isCareDone(this.record().status));
  readonly statusLabel = computed(() =>
    this.overdue() ? 'Vencido' : careStatusLabel(this.record().status),
  );
  readonly today = todayLocal();

  openPanel(kind: 'late' | 'skip'): void {
    this.lateDate.set(todayLocal());
    this.skipReason.set('');
    this.panel.set(this.panel() === kind ? null : kind);
  }

  confirmLate(): void {
    const date = this.lateDate();
    if (!date || date > this.today || date < this.record().careDate) {
      return;
    }
    this.completeLate.emit({ id: this.record().id, completedOn: date });
    this.panel.set(null);
  }

  confirmSkip(): void {
    const reason = this.skipReason();
    if (!reason) {
      return;
    }
    this.skip.emit({ id: this.record().id, reason });
    this.panel.set(null);
  }

  confirmDelete(): void {
    if (confirm('¿Eliminar este cuidado? Esta acción no se puede deshacer.')) {
      this.remove.emit(this.record().id);
    }
  }

  speciesLabel(species: PetSpecies): string {
    return ({
      DOG: 'Perro',
      CAT: 'Gato',
      BIRD: 'Ave',
      RABBIT: 'Conejo',
      RODENT: 'Roedor',
      REPTILE: 'Reptil',
      FISH: 'Pez',
      OTHER: 'Otro',
    } satisfies Record<PetSpecies, string>)[species] ?? 'Otro';
  }
}
