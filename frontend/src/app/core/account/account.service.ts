import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DataFormat, DeletionSummary } from '../../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/me`;

  /** Descarga solo los datos de la persona autenticada. */
  download(format: DataFormat): Observable<Blob> {
    const params = new HttpParams().set('formato', format);
    return this.http.get(`${this.url}/datos`, { params, responseType: 'blob' });
  }

  deletionSummary(): Observable<DeletionSummary> {
    return this.http.get<DeletionSummary>(`${this.url}/eliminacion/resumen`);
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(this.url);
  }
}