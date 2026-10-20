export type CareHistoryAction = 'COMPLETED' | 'RESCHEDULED';

export interface CareHistoryEntry {
  id: string;
  careRecordId: string;
  petName: string;
  care: string;
  action: CareHistoryAction;
  previousCareDate: string | null;
  currentCareDate: string;
  occurredAt: string;
}
