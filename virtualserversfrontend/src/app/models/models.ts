export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface Server {
  id: number;
  name: string;
  ipAddress: string;
  status: ServerStatus;
  cpu: number;
  ram: number;
}

export interface Metric {
  cpuUsage: number;
  ramUsage: number;   
  networkLoad: number;
  timestamp: string;
}

export interface Log {
  id: number;
  createdAt: string;
  level: LogLevel;
  message: string;
}

export interface UserInfo {
  id: number;
  username: string;
  role: string;
}

export type ServerStatus = 'STARTING' | 'RUNNING' | 'STOPPED' | 'ERROR' | 'MAINTENANCE';

export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';