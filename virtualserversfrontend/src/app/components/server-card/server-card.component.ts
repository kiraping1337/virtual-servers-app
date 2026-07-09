import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Server, ServerStatus } from '../../models/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-server-card',
  imports: [CommonModule],
  templateUrl: './server-card.component.html',
  styleUrl: './server-card.component.css',
})
export class ServerCardComponent {
  @Input({ required: true }) server!: Server;
 
  @Output() start   = new EventEmitter<Server>();
  @Output() stop    = new EventEmitter<Server>();
  @Output() restart = new EventEmitter<Server>();
  @Output() edit    = new EventEmitter<Server>();
  @Output() delete  = new EventEmitter<Server>();
  @Output() metrics = new EventEmitter<Server>();
  @Output() logs    = new EventEmitter<Server>();
 
  statusLabel(status: ServerStatus): string {
    const map: Record<ServerStatus, string> = {
      RUNNING:     'Работает',
      STOPPED:     'Остановлен',
      STARTING:    'Запускается',
      ERROR:       'Ошибка',
      MAINTENANCE: 'Обслуживание'
    };
    return map[status] ?? status;
  }
}
