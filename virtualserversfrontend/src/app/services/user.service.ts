import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserInfo } from '../models/models';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  getMe(): Observable<UserInfo> {
    return this.http.get<UserInfo>('/api/users/me');
  }

  getAllUsers(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>('/api/users');
  }
 
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`/api/users/${id}`);
  }
 
  getCurrentRole(): string | null {
    const token = sessionStorage.getItem('accessToken');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[0]));
      return payload.roles?.[0] ?? null;
    } catch {
      return null;
    }
  }
 
  isAdmin(): boolean {
    return this.getCurrentRole() === 'ADMIN';
  }

}
