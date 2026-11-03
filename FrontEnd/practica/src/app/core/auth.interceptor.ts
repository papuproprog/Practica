import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = typeof localStorage === 'undefined' ? null : localStorage.getItem('barber-token');
  const branch = typeof localStorage === 'undefined' ? 'centro' : (localStorage.getItem('barber-branch') || 'centro');

  let headers = request.headers.set('X-Branch-Id', branch);
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  return next(request.clone({ headers }));
};
