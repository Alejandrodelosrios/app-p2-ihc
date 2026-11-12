export type CareRecordStatus =
  | 'PENDING'
  | 'COMPLETED'
  | 'RESCHEDULED'
  | 'CANCELLED'
  | 'COMPLETED_LATE'
  | 'SKIPPED';
export type CareRecordCareType = 'VACCINE' | 'MEDICATION' | 'CONTROL' | 'OTHER';

export interface CareRecord {
  id: string;
  userId: string;
  petId: string;
  petName: string;
  petSpecies: import('./pet.model').PetSpecies;
  care: string;
  careDate: string;
  createdAt: string;
  status: CareRecordStatus;
  assignedToUserId?: string | null;
  recurrenceIntervalDays?: number | null;
  originalCareDate?: string | null;
  changeReason?: string | null;
}

export interface CreateCareRecordRequest {
  petId: string;
  care: string;
  careDate: string;
  assigneeEmail?: string | null;
  recurrenceIntervalDays?: number | null;
}

export interface CareRecordFilters {
  petId?: string;
  petName?: string;
  status?: CareRecordStatus;
  careType?: CareRecordCareType;
  fromDate?: string;
  toDate?: string;
}


/** Motivos fijos para omitir un cuidado vencido (lista cerrada: no se guarda texto libre). */
export const SKIP_REASONS = [
  'Mascota enferma o en tratamiento',
  'No tenía el medicamento o insumo',
  'Viaje o ausencia',
  'Ya no era necesario',
  'Otro motivo',
] as const;

export function todayLocal(now = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

/** Vencido = abierto (pendiente o reprogramado) con fecha anterior a hoy. */
export function isCareOverdue(record: Pick<CareRecord, 'status' | 'careDate'>, today = todayLocal()): boolean {
  return (record.status === 'PENDING' || record.status === 'RESCHEDULED') && record.careDate < today;
}

export function isCareDone(status: CareRecordStatus): boolean {
  return status === 'COMPLETED' || status === 'COMPLETED_LATE';
}

export function careStatusLabel(status: CareRecordStatus): string {
  return ({
    PENDING: 'Pendiente',
    COMPLETED: 'Realizado',
    RESCHEDULED: 'Reprogramado',
    CANCELLED: 'Cancelado',
    COMPLETED_LATE: 'Realizado tarde',
    SKIPPED: 'Omitido',
  } satisfies Record<CareRecordStatus, string>)[status];
}
