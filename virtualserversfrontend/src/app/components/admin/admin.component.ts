import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Log, Metric, Server, ServerStatus, UserInfo } from '../../models/models';
import { interval, Subscription } from 'rxjs';
import { UserService } from '../../services/user.service';
import { ServerService } from '../../services/server.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServerCardComponent } from '../server-card/server-card.component';

type Modal = 'none' | 'servers' | 'edit' | 'logs' | 'metrics';

@Component({
  selector: 'app-admin',
  imports: [CommonModule, FormsModule, ServerCardComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css',
})
export class AdminComponent implements OnInit, OnDestroy {
  users: UserInfo[] = [];
  loading = true;

  selectedUser: UserInfo | null = null;
  userServers: Server[] = [];
  serversLoading = false;
 
  activeModal: Modal = 'none';
  selectedServer: Server | null = null;
 
  editCpu = 1;
  editRam = 512;
  editLoading = false;
  editError = '';
 
  logs: Log[] = [];
  logsLoading = false;
 
  metrics: Metric[] = [];
  metricsLoading = false;
  private metricsInterval?: Subscription;
 
  private subs = new Subscription();
 
  readonly adminStatuses: ServerStatus[] = ['RUNNING', 'STOPPED', 'STARTING', 'MAINTENANCE', 'ERROR'];
 
  constructor(
    private userService: UserService,
    private serverService: ServerService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    if (!this.userService.isAdmin()) {
      this.router.navigate(['/servers']);
      return;
    }
    this.loadUsers();
  }
 
  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.metricsInterval?.unsubscribe();
  }
 
  loadUsers(): void {
    this.loading = true;
    this.subs.add(
      this.userService.getAllUsers().subscribe({
        next: (users) => { this.users = users; this.loading = false; this.cdr.detectChanges(); },
        error: ()      => { this.loading = false; this.cdr.detectChanges(); }
      })
    );
  }
 
  goBack(): void {
    this.router.navigate(['/servers']);
  }
 
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth']);
  }
 
 
  deleteUser(user: UserInfo): void {
    if (!confirm(`Удалить пользователя «${user.username}»?`)) return;
    this.subs.add(
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== user.id);
          if (this.selectedUser?.id === user.id) this.selectedUser = null;
          this.cdr.detectChanges();
        }
      })
    );
  }
 
  viewServers(user: UserInfo): void {
    this.selectedUser = user;
    this.userServers = [];
    this.serversLoading = true;
    this.activeModal = 'servers';
    this.subs.add(
      this.serverService.getUserServers(user.id).subscribe({
        next: (servers) => { this.userServers = servers; this.serversLoading = false; this.cdr.detectChanges(); },
        error: ()        => { this.serversLoading = false; this.cdr.detectChanges(); }
      })
    );
  }
 
 
  onStart(server: Server): void   { this.changeStatus(server, 'RUNNING'); }
  onStop(server: Server): void    { this.changeStatus(server, 'STOPPED'); }
  onRestart(server: Server): void { this.changeStatus(server, 'STARTING'); }
 
  changeStatus(server: Server, status: ServerStatus): void {
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
        next: () => {
          this.userServers = this.userServers.filter(s => s.id !== server.id);
          this.cdr.detectChanges();
        }
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
 
  setStatus(server: Server, event: Event): void {
    const status = (event.target as HTMLSelectElement).value as ServerStatus;
    this.changeStatus(server, status);
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
        this.activeModal = 'servers';
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
    const canvas = document.getElementById('adminMetricsChart') as HTMLCanvasElement;
    if (!canvas || this.metrics.length === 0) return;
 
    import('chart.js/auto').then(({ default: Chart }) => {
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
            { label: 'CPU %',     data: sorted.map(m => m.cpuUsage), borderColor: '#4ade80', backgroundColor: 'rgba(74,222,128,0.08)', tension: 0.3, fill: true, pointRadius: 3 },
            { label: 'RAM %',     data: sorted.map(m => Math.round((m.ramUsage / serverRam) * 100)), borderColor: '#60a5fa', backgroundColor: 'rgba(96,165,250,0.08)', tension: 0.3, fill: true, pointRadius: 3 },
            { label: 'Network %', data: sorted.map(m => m.networkLoad), borderColor: '#f59e0b', backgroundColor: 'rgba(245,158,11,0.08)', tension: 0.3, fill: true, pointRadius: 3 }
          ]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: { legend: { labels: { color: '#9ca3af', font: { family: 'JetBrains Mono', size: 11 } } } },
          scales: {
            x: { ticks: { color: '#6b7280', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } },
            y: { min: 0, max: 100, ticks: { color: '#6b7280', font: { family: 'JetBrains Mono', size: 10 } }, grid: { color: 'rgba(255,255,255,0.05)' } }
          }
        }
      });
    });
  }
 
  closeModal(): void {
    if (this.activeModal === 'edit' || this.activeModal === 'logs' || this.activeModal === 'metrics') {
      this.activeModal = 'servers';
      this.selectedServer = null;
      this.metricsInterval?.unsubscribe();
    } else {
      this.activeModal = 'none';
      this.selectedUser = null;
      this.selectedServer = null;
      this.metricsInterval?.unsubscribe();
    }
  }
 
  closeAll(): void {
    this.activeModal = 'none';
    this.selectedUser = null;
    this.selectedServer = null;
    this.metricsInterval?.unsubscribe();
  }
 
  logLevelClass(level: string): string {
    return { ERROR: 'log-error', WARN: 'log-warn', INFO: 'log-info', DEBUG: 'log-debug' }[level] ?? '';
  }
 
  statusLabel(status: ServerStatus): string {
    return { RUNNING: 'Работает', STOPPED: 'Остановлен', STARTING: 'Запускается', ERROR: 'Ошибка', MAINTENANCE: 'Обслуживание' }[status] ?? status;
  }
}
