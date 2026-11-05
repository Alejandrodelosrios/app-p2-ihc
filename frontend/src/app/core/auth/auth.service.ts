import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthSession,
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  PasswordResetResponse,
  RegisterRequest,
} from '../../models/auth.models';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(TokenStorageService);

  readonly user = computed(() => this.storage.session()?.user ?? null);

  isAuthenticated(): boolean {
    return this.storage.read() !== null;
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => this.persist(response)),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, request).pipe(
      tap((response) => this.persist(response)),
    );
  }

  requestPasswordReset(request: PasswordResetRequest): Observable<PasswordResetResponse> {
    return this.http.post<PasswordResetResponse>(`${environment.apiUrl}/auth/password-reset/request`, request);
  }

  confirmPasswordReset(request: PasswordResetConfirmRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/password-reset/confirm`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/password/change`, request);
  }

  refreshCurrentUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => {
        const current = this.storage.read();
        if (current) this.storage.save({ ...current, user });
      }),
    );
  }

  searchUsers(email: string): Observable<AuthUser[]> {
    return this.http.get<AuthUser[]>(`${environment.apiUrl}/auth/users/search`, { params: { email } });
  }

  findUser(id: string): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${environment.apiUrl}/auth/users/${id}`);
  }

  logout(): void {
    this.storage.clear();
  }

  private persist(response: AuthResponse): void {
    const session: AuthSession = { accessToken: response.accessToken, user: response.user };
    this.storage.save(session);
  }
}