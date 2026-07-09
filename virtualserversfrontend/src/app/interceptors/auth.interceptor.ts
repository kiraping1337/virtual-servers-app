import { HttpInterceptorFn, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const addToken = (request: typeof req) => {
    const token = authService.getAccessToken();
    if (!token) return request;
    return request.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  };

  return next(addToken(req)).pipe(
    catchError(err => {
      if (err.status === HttpStatusCode.Unauthorized && req.url.includes('/api/auth/refresh')) {
        authService.logout();
        router.navigate(['/auth']);
        return throwError(() => err);
      }

      if (err.status === HttpStatusCode.Unauthorized) {
        const refreshToken = authService.getRefreshToken();

        if (!refreshToken) {
          authService.logout();
          router.navigate(['/auth']);
          return throwError(() => err);
        }

        return authService.refresh(refreshToken).pipe(
          switchMap(tokens => {
            authService.saveTokens(tokens);
            return next(addToken(req));
          }),
          catchError(refreshErr => {
            authService.logout();
            router.navigate(['/auth']);
            return throwError(() => refreshErr);
          })
        );
      }

      return throwError(() => err);
    })
  );
};
