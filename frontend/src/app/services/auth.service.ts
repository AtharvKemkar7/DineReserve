import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, tap } from 'rxjs';
import { AuthResponse, Role, User } from '../models';

const TOKEN_KEY = 'dining_token';
const USER_KEY = 'dining_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.readUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
      tap(res => this.store(res))
    );
  }

  register(payload: { email: string; password: string; fullName: string; phone?: string }) {
    return this.http.post<AuthResponse>('/api/auth/register', payload).pipe(
      tap(res => this.store(res))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  user(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  hasRole(...roles: Role[]): boolean {
    const user = this.user();
    return !!user && roles.includes(user.role);
  }

  homePath(): string {
    const role = this.user()?.role;
    if (role === 'ADMIN') return '/admin';
    if (role === 'MANAGER') return '/manager';
    return '/';
  }

  getProfile() {
    return this.http.get<User>('/api/profile');
  }

  updateProfile(fullName: string, phone: string) {
    return this.http.put<User>('/api/profile', { fullName, phone });
  }

  private store(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res));
    this.currentUserSubject.next(res);
  }

  private readUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) as AuthResponse : null;
  }
}
