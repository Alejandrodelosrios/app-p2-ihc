import { DeletionSummary } from '../../models/account.model';

function plural(count: number, one: string, many: string): string {
  return count === 1 ? one : many;
}

/**
 * Consecuencias de eliminar la cuenta, en lenguaje claro y con los números reales de la
 * persona. Solo aparecen las que le aplican. Sin culpa y sin dramatismo: se dice qué pasa.
 */
export function deletionConsequences(summary: DeletionSummary): string[] {
  const lines = [
    'Tu cuenta se elimina y no podrás volver a iniciar sesión con ella.',
    'Tu correo y tu contraseña se borran. Tu nombre se conserva solo como autor de lo que ya registraste.',
  ];

  if (summary.activePets > 0) {
    lines.push(
      summary.activePets === 1
        ? 'Tu mascota se desactiva y deja de aparecer en la app.'
        : `Tus ${summary.activePets} mascotas se desactivan y dejan de aparecer en la app.`,
    );
  }

  if (summary.assignedToMePending > 0) {
    const n = summary.assignedToMePending;
    lines.push(
      `Pierdes el acceso a ${n} ${plural(n, 'cuidado pendiente que te asignaron', 'cuidados pendientes que te asignaron')}: ` +
        `${plural(n, 'sigue', 'siguen')} con ${plural(n, 'la persona que lo creó', 'quienes los crearon')}.`,
    );
  }

  if (summary.sharedWithOthersPending > 0) {
    const n = summary.sharedWithOthersPending;
    lines.push(
      `${n} ${plural(n, 'cuidado pendiente que creaste y asignaste a otra persona sigue visible para ella', 'cuidados pendientes que creaste y asignaste a otras personas siguen visibles para ellas')}. ` +
        `Podrán marcar${plural(n, 'lo', 'los')} como realizado${plural(n, '', 's')}, pero nadie podrá editar${plural(n, 'lo', 'los')} ni reprogramar${plural(n, 'lo', 'los')}.`,
    );
  }

  if (summary.unsharedPendingDeleted > 0) {
    const n = summary.unsharedPendingDeleted;
    lines.push(
      `${n} ${plural(n, 'cuidado pendiente que creaste sin asignar a nadie se elimina', 'cuidados pendientes que creaste sin asignar a nadie se eliminan')}.`,
    );
  }

  if (summary.historyEntries > 0) {
    const n = summary.historyEntries;
    lines.push(
      n === 1
        ? 'Tu registro del historial se conserva con tu nombre como autor.'
        : `Tus ${n} registros del historial se conservan con tu nombre como autor.`,
    );
  }

  lines.push('Esta acción no se puede deshacer.');
  return lines;
}