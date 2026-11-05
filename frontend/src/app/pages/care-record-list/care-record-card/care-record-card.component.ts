import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CareRecord } from '../../../models/care-record.model';
import { IconComponent } from '../../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-care-record-card',
  standalone: true,
  imports: [IconComponent, RouterLink],
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

  confirmDelete(): void {
    if (confirm('¿Eliminar este cuidado? Esta acción no se puede deshacer.')) {
      this.remove.emit(this.record().id);
    }
  }
}
