import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { switchMap } from 'rxjs';

type Mode = 'login' | 'register';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent {
  mode: Mode = 'login';
  username = '';
  password = '';
  loading = false;
  errorMessage = '';
 
  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  get isLogin(): boolean {
    return this.mode === 'login';
  }
 
  switchMode(mode: Mode): void {
    this.mode = mode;
    this.errorMessage = '';
    this.username = '';
    this.password = '';
  }
 
  submit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Заполните все поля';
      return;
    }
 
    this.loading = true;
    this.errorMessage = '';
 
    if (this.isLogin) {
      this.authService.login(this.username, this.password).subscribe({
        next: () => {
          this.loading = false;
          this.cdr.detectChanges();
          this.router.navigate(['/servers']);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = err.status === 401
            ? 'Неверный логин или пароль'
            : 'Ошибка сервера. Попробуйте позже';
          this.cdr.detectChanges();
        }
      });
    } else {
      const username = this.username;
      const password = this.password;
      this.authService.register(username, password).pipe(
        switchMap(() => this.authService.login(username, password))
      ).subscribe({
        next: () => {
          this.loading = false;
          this.cdr.detectChanges();
          this.router.navigate(['/servers']);
        },
        error: (err) => {
          this.loading = false;
          if (err.status === 409 || err.status === 400) {
            this.errorMessage = 'Пользователь с таким именем уже существует';
          } else if (err.status === 401) {
            this.errorMessage = 'Неверные данные для входа';
          } else {
            this.errorMessage = 'Ошибка при регистрации. Попробуйте позже';
          }
          this.cdr.detectChanges();
        }
      });
    }
  }
}
