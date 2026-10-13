export type CareRecordStatus = 'PENDING' | 'COMPLETED';
export type CareRecordCareType = 'VACCINE' | 'MEDICATION' | 'CONTROL' | 'OTHER';

export interface CareRecord {
  id: string;
  userId: string;
  petName: string;
  care: string;
  animalType: string;
  careDate: string;
  createdAt: string;
  status: CareRecordStatus;
}

export interface CreateCareRecordRequest {
  petName: string;
  care: string;
  animalType: string;
  careDate: string;
}
export interface CareRecordFilters {
  petName?: string;
  status?: CareRecordStatus;
  careType?: CareRecordCareType;
  fromDate?: string;
  toDate?: string;
}
