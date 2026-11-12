import { careStatusLabel, isCareDone, isCareOverdue } from './care-record.model';

describe('care-record model', () => {
  const today = '2026-11-12';

  it('un pendiente con fecha pasada está vencido', () => {
    expect(isCareOverdue({ status: 'PENDING', careDate: '2026-11-11' }, today)).toBe(true);
  });

  it('un reprogramado con fecha pasada también está vencido', () => {
    expect(isCareOverdue({ status: 'RESCHEDULED', careDate: '2026-11-01' }, today)).toBe(true);
  });

  it('hoy y el futuro no están vencidos', () => {
    expect(isCareOverdue({ status: 'PENDING', careDate: today }, today)).toBe(false);
    expect(isCareOverdue({ status: 'PENDING', careDate: '2026-11-13' }, today)).toBe(false);
  });

  it('realizados, realizados tarde y omitidos nunca están vencidos', () => {
    for (const status of ['COMPLETED', 'COMPLETED_LATE', 'SKIPPED'] as const) {
      expect(isCareOverdue({ status, careDate: '2026-01-01' }, today)).toBe(false);
    }
  });

  it('realizado tarde cuenta como hecho y tiene su etiqueta', () => {
    expect(isCareDone('COMPLETED_LATE')).toBe(true);
    expect(isCareDone('SKIPPED')).toBe(false);
    expect(careStatusLabel('COMPLETED_LATE')).toBe('Realizado tarde');
    expect(careStatusLabel('SKIPPED')).toBe('Omitido');
  });
});
