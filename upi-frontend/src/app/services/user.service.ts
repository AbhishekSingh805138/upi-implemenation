import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { User, UserRegistrationRequest, UserLoginRequest, UserUpdateRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apiService: ApiService) {
    // Load user from localStorage on service initialization
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  // Register new user
  register(userData: UserRegistrationRequest): Observable<User> {
    return this.apiService.post<User>('/api/users/register', userData);
  }

  // Login user
  login(loginData: UserLoginRequest): Observable<User> {
    return this.apiService.post<User>('/api/users/login', loginData)
      .pipe(
        tap(user => {
          this.setCurrentUser(user);
        })
      );
  }

  // Get user by ID
  getUserById(id: number): Observable<User> {
    return this.apiService.get<User>(`/api/users/${id}`);
  }

  // Get user by username
  getUserByUsername(username: string): Observable<User> {
    return this.apiService.get<User>(`/api/users/username/${username}`);
  }

  // Update user profile
  updateUser(id: number, userData: UserUpdateRequest): Observable<User> {
    return this.apiService.put<User>(`/api/users/${id}`, userData)
      .pipe(
        tap(user => {
          this.setCurrentUser(user);
        })
      );
  }

  // Validate user exists
  validateUserExists(id: number): Observable<boolean> {
    return this.apiService.get<boolean>(`/api/users/${id}/validate`);
  }

  // Get all users (admin)
  getAllUsers(): Observable<User[]> {
    return this.apiService.get<User[]>('/api/users');
  }

  // Delete user
  deleteUser(id: number): Observable<void> {
    return this.apiService.delete<void>(`/api/users/${id}`);
  }

  // Set current user
  setCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  // Get current user
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Logout
  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }
}