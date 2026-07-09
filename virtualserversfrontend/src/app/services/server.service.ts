import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Log, Metric, Server, ServerStatus } from '../models/models';


@Injectable({ providedIn: 'root' })
export class ServerService {
  constructor(private http: HttpClient) {}

  getMyServers(): Observable<Server[]> {
    return this.http.get<Server[]>('/api/servers/user/my');
  }

  getUserServers(userId: number): Observable<Server[]> {
    return this.http.get<Server[]>(`/api/servers/user/${userId}`);
  }

  createServer(name: string, cpu: number, ram: number): Observable<Server> {
    return this.http.post<Server>('/api/servers', { name, cpu, ram });
  }

  updateStatus(id: number, status: ServerStatus): Observable<void> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<void>(`/api/servers/${id}/status`, null, { params });
  }

  updateConfig(id: number, cpu: number, ram: number): Observable<void> {
    const params = new HttpParams().set('cpu', cpu).set('ram', ram);
    return this.http.put<void>(`/api/servers/${id}/config`, null, { params });
  }

  deleteServer(id: number): Observable<void> {
    return this.http.delete<void>(`/api/servers/${id}`);
  }

  getMetrics(serverId: number): Observable<Metric[]> {
    return this.http.get<Metric[]>(`/api/monitoring/servers/${serverId}/metrics`);
  }

  getLogs(serverId: number): Observable<Log[]> {
    return this.http.get<Log[]>(`/api/monitoring/servers/${serverId}/logs`);
  }
}
