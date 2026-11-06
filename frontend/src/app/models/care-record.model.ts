export type CareRecordStatus = 'PENDING' | 'COMPLETED';
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
