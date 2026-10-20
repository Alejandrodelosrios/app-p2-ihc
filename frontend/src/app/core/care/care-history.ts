import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CareHistoryEntry } from '../../models/care-history.model';

@Injectable({ providedIn: 'root' })
export class CareHistoryService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/care-history`;

  findAll(): Observable<CareHistoryEntry[]> {
    return this.http.get<CareHistoryEntry[]>(this.url);
  }
}
