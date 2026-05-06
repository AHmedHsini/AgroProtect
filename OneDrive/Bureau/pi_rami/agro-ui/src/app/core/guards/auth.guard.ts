import { inject }           from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }      from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  // Lighthouse bypass : autorise l'accès si l'agent est Lighthouse
  if (typeof window !== 'undefined' && navigator.userAgent.includes('Lighthouse')) return true;

  if (auth.isLogged()) return true;

  return router.createUrlTree(['/login']);
};
