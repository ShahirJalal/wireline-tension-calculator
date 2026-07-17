import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(
    private apiService: ApiService,
    private router: Router
  ) {
    this.loadUserFromLocalStorage();
  }

  // Register a new user
  register(username: string, email: string, password: string): Observable<AuthResponse> {
    return this.apiService
      .post<AuthResponse>('auth/register', { username, email, password })
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  // Log in a user
  login(username: string, password: string): Observable<AuthResponse> {
    return this.apiService
      .post<AuthResponse>('auth/login', { username, password })
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  // Log out the current user
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  // Handle authentication response
  private handleAuthResponse(response: AuthResponse): void {
    if (response && response.token) {
      localStorage.setItem('token', response.token);
      
      const user: User = {
        id: response.id,
        username: response.username,
        email: response.email
      };
      
      localStorage.setItem('user', JSON.stringify(user));
      this.currentUserSubject.next(user);
    }
  }

  // Load user data from localStorage on app initialization
  private loadUserFromLocalStorage(): void {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      const user = JSON.parse(userJson) as User;
      this.currentUserSubject.next(user);
    }
  }
}