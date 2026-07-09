import {
  ChangeDetectorRef, Component, OnDestroy, OnInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';


import { ServerService } from '../../services/server.service';
import { UserService} from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Log, Metric, Server, ServerStatus, UserInfo } from '../../models/models';
import { ServerCardComponent } from '../server-card/server-card.component';

type Modal = 'none' | 'create' | 'edit' | 'logs' | 'metrics';

@Component({
  selector: 'app-servers',
  standalone: true,
  imports: [CommonModule, FormsModule, ServerCardComponent],
  templateUrl: './servers.component.html',
  styleUrl: './servers.component.css'
})
export class ServersComponent implements OnInit, OnDestroy {
  servers: Server[] = [];
  currentUser: UserInfo | null = null;
  loading = true;
  isAdmin = false;
 
  activeModal: Modal = 'none';
  selectedServer: Server | null = null;
 
  createName = '';
  createCpu = 1;
  createRam = 512;
  createLoading = false;
  createError = '';
 
  editCpu = 1;
  editRam = 512;
  editLoading = false;
  editError = '';
 
  logs: Log[] = [];
  logsLoading = false;
 
  metrics: Metric[] = [];
  metricsLoading = false;
  private metricsInterval?: Subscription;
  private startingInterval?: Subscription;
 
  private subs = new Subscription();
 
  constructor(
    private serverService: ServerService,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.loadUser();
    this.loadServers();
    this.startAutoStarting();
  }
 
  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.metricsInterval?.unsubscribe();
    this.startingInterval?.unsubscribe();
  }
 
  private startAutoStarting(): void {
    this.startingInterval = interval(15000).subscribe(() => {
      this.servers
        .filter(s => s.status === 'STARTING')
        .forEach(server => {
          this.serverService.updateStatus(server.id, 'RUNNING').subscribe({
            next: () => { server.status = 'RUNNING'; this.cdr.detectChanges(); }
          });
        });
    });
  }
 
  loadUser(): void {
    this.subs.add(
      this.userService.getMe().subscribe({
        next: (user) => { 
          this.currentUser = user; 
          this.isAdmin = this.userService.isAdmin();
          this.cdr.detectChanges(); 
        }
      })
    );
  }
 
  loadServers(): void {
    this.loading = true;
    this.subs.add(
      this.serverService.getMyServers().subscribe({
        next: (servers) => { this.servers = servers; this.loading = false; this.cdr.detectChanges(); },
        error: ()        => { this.loading = false; this.cdr.detectChanges(); }
      })
    );
  }
 
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth']);
  }

  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }
 
  onStart(server: Server): void   { this.changeStatus(server, 'RUNNING'); }
  onStop(server: Server): void    { this.changeStatus(server, 'STOPPED'); }
  onRestart(server: Server): void { this.changeStatus(server, 'STARTING'); }
 
  private changeStatus(server: Server, status: ServerStatus): void {
    this.subs.add(
      this.serverService.updateStatus(server.id, status).subscribe({
        next: () => { server.status = status; this.cdr.detectChanges(); }
      })
    );
  }
 
  onDelete(server: Server): void {
    if (!confirm(`Удалить сервер «${server.name}»?`)) return;
    this.subs.add(
      this.serverService.deleteServer(server.id).subscribe({
        next: () => { this.servers = this.servers.filter(s => s.id !== server.id); this.cdr.detectChanges(); }
      })
    );
  }
 
  onEdit(server: Server): void {
    this.selectedServer = server;
    this.editCpu = server.cpu;
    this.editRam = server.ram;
    this.editError = '';
    this.activeModal = 'edit';
  }
 
  onMetrics(server: Server): void { this.openMetrics(server); }
  onLogs(server: Server): void    { this.openLogs(server); }
 
  openCreate(): void {
    this.createName = '';
    this.createCpu = 1;
    this.createRam = 512;
    this.createError = '';
    this.activeModal = 'create';
  }
 
  submitCreate(): void {
    if (!this.createName.trim()) { this.createError = 'Введите имя сервера'; return; }
    this.createLoading = true;
    this.createError = '';
    this.serverService.createServer(this.createName, this.createCpu, this.createRam).subscribe({
      next: (server) => {
        this.servers.push(server);
        this.createLoading = false;
        this.activeModal = 'none';
        this.cdr.detectChanges();
      },
      error: () => {
        this.createLoading = false;
        this.createError = 'Ошибка при создании сервера';
        this.cdr.detectChanges();
      }
    });
  }
 
  submitEdit(): void {
    if (!this.selectedServer) return;
    this.editLoading = true;
    this.editError = '';
    this.serverService.updateConfig(this.selectedServer.id, this.editCpu, this.editRam).subscribe({
      next: () => {
        this.selectedServer!.cpu = this.editCpu;
        this.selectedServer!.ram = this.editRam;
        this.editLoading = false;
        this.activeModal = 'none';
        this.cdr.detectChanges();
      },
      error: () => {
        this.editLoading = false;
        this.editError = 'Ошибка при обновлении конфигурации';
        this.cdr.detectChanges();
      }
    });
  }
 
  openLogs(server: Server): void {
    this.selectedServer = server;
    this.logs = [];
    this.logsLoading = true;
    this.activeModal = 'logs';
    this.serverService.getLogs(server.id).subscribe({
      next: (logs) => { this.logs = logs; this.logsLoading = false; this.cdr.detectChanges(); },
      error: ()     => { this.logsLoading = false; this.cdr.detectChanges(); }
    });
  }
 
  openMetrics(server: Server): void {
    this.selectedServer = server;
    this.metrics = [];
    this.metricsLoading = true;
    this.activeModal = 'metrics';
    this.metricsInterval?.unsubscribe();
 
    const load = () => {
      this.serverService.getMetrics(server.id).subscribe({
        next: (metrics) => {
          this.metrics = metrics;
          this.metricsLoading = false;
          this.cdr.detectChanges();
          setTimeout(() => this.renderChart(), 50);
        },
        error: () => { this.metricsLoading = false; this.cdr.detectChanges(); }
      });
    };
 
    load();
    this.metricsInterval = interval(15000).subscribe(() => load());
  }
 
  private renderChart(): void {
    if (typeof window === 'undefined') return;
    const canvas = document.getElementById('metricsChart') as HTMLCanvasElement;
    if (!canvas || this.metrics.length === 0) return;
 
    import('chart.js/auto').then(({ default: Chart }) => {
      Chart.defaults.font.family ="'JetBrains Mono', monospace";
      const existing = Chart.getChart(canvas);
      if (existing) existing.destroy();

      const sorted = [...this.metrics].reverse();
      const serverRam = this.selectedServer?.ram ?? 1;
 
      const labels = sorted.map(m =>
        new Date(m.timestamp).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
      );

      new Chart(canvas, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: 'CPU %',
              data: sorted.map(m => m.cpuUsage),
              borderColor: '#4ade80',
              backgroundColor: 'rgba(74,222,128,0.08)',
              tension: 0.3, fill: true, pointRadius: 3,
            },
            {
              label: 'RAM %',
              data: sorted.map(m => Math.round((m.ramUsage / serverRam) * 100)),
              borderColor: '#60a5fa',
              backgroundColor: 'rgba(96,165,250,0.08)',
              tension: 0.3, fill: true, pointRadius: 3,
            },
            {
              label: 'Network %',
              data: sorted.map(m => m.networkLoad),
              borderColor: '#f59e0b',
              backgroundColor: 'rgba(245,158,11,0.08)',
              tension: 0.3, fill: true, pointRadius: 3,
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { labels: { color: '#9ca3af', font: { family: 'JetBrains Mono', size: 11 } } }
          },
          scales: {
            x: {
              ticks: { color: '#6b7280', font: { family: 'JetBrains Mono', size: 10 } },
              grid: { color: 'rgba(0, 0, 0, 0.05)' }
            },
            y: {
              min: 0, max: 100,
              ticks: { color: '#6b7280', font: { family: 'JetBrains Mono', size: 10 } },
              grid: { color: 'rgba(0, 0, 0, 0.05)' }
            }
          }
        }
      });
    });
  }
 
  closeModal(): void {
    this.activeModal = 'none';
    this.selectedServer = null;
    this.metricsInterval?.unsubscribe();
  }
  
  logLevelClass(level: string): string {
    const map: Record<string, string> = {
      ERROR: 'log-error', WARN: 'log-warn', INFO: 'log-info', DEBUG: 'log-debug'
    };
    return map[level] ?? '';
  }
}