import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserDirectoryEntry } from '../../models/user-directory.model';

@Injectable({ providedIn: 'root' })
export class UserDirectoryService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/auth/users`;

  searchByEmail(email: string): Observable<UserDirectoryEntry[]> {
    const params = new HttpParams().set('email', email);
    return this.http.get<UserDirectoryEntry[]>(`${this.url}/search`, { params });
  }

  findById(id: string): Observable<UserDirectoryEntry> {
    return this.http.get<UserDirectoryEntry>(`${this.url}/${id}`);
  }
}
