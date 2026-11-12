export type CareHistoryAction = 'COMPLETED' | 'RESCHEDULED' | 'COMPLETED_LATE' | 'SKIPPED';

export interface CareHistoryEntry {
  id: string;
  careRecordId: string;
  petName: string;
  care: string;
  action: CareHistoryAction;
  previousCareDate: string | null;
  currentCareDate: string;
  occurredAt: string;
  performedByName?: string | null;
  changeReason?: string | null;
}
