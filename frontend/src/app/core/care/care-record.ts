import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { CareRecord, CareRecordFilters, CreateCareRecordRequest } from '../../models/care-record.model';

@Injectable({
  providedIn: 'root',
})
export class CareRecordService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/care-records`

  create(careRecord: CreateCareRecordRequest): Observable<CareRecord> {
    return this.http.post<CareRecord>(this.url, careRecord);
  }

  findAll(filters: CareRecordFilters = {}): Observable<CareRecord[]> {
    let params = new HttpParams();

    if (filters.petName) {
      params = params.set('petName', filters.petName);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.careType) {
      params = params.set('careType', filters.careType);
    }
    if (filters.fromDate) {
      params = params.set('fromDate', filters.fromDate);
    }
    if (filters.toDate) {
      params = params.set('toDate', filters.toDate);
    }

    return this.http.get<CareRecord[]>(this.url, { params });
  }

  complete(id: string): Observable<CareRecord> {
    return this.http.patch<CareRecord>(`${this.url}/${id}/complete`, {});
  }

  update(id: string, careRecord: CreateCareRecordRequest): Observable<CareRecord> {
    return this.http.put<CareRecord>(`${this.url}/${id}`, careRecord);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
