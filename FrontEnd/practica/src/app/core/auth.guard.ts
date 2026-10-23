import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
export const authGuard: CanActivateFn = () => {
  const hasToken = typeof localStorage !== 'undefined' && !!localStorage.getItem('barber-token');
  return hasToken || inject(Router).createUrlTree(['/login']);
};
