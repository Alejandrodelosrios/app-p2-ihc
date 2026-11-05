import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pet, SavePetRequest } from '../../models/pet.model';

@Injectable({ providedIn: 'root' })
export class PetService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/pets`;

  findAll(): Observable<Pet[]> {
    return this.http.get<Pet[]>(this.url);
  }

  create(request: SavePetRequest): Observable<Pet> {
    return this.http.post<Pet>(this.url, request);
  }

  update(id: string, request: SavePetRequest): Observable<Pet> {
    return this.http.put<Pet>(`${this.url}/${id}`, request);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
