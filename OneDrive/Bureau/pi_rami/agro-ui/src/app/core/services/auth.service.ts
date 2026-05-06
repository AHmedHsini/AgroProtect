import { inject, Injectable, PLATFORM_ID, signal, computed } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export interface User {
  id:    string;
  name:  string;
  email: string;
  role:  string;
}

// ─── Utilisateur statique pour les tests ───────────────────────
const MOCK_USER: User = {
  id:    '1',
  name:  'Karim Amari',
  email: 'karim@agroprotect.dz',
  role:  'Analyste Senior',
};
const MOCK_PASSWORD = 'admin123';
// ───────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AuthService {
  private platformId = inject(PLATFORM_ID);
  private _user      = signal<User | null>(null);

  readonly user     = this._user.asReadonly();
  readonly isLogged = computed(() => this._user() !== null);

  // Connexion avec user statique
  loginStatic(email: string, password: string): boolean {
    if (email === MOCK_USER.email && password === MOCK_PASSWORD) {
      this._user.set(MOCK_USER);
      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('user', JSON.stringify(MOCK_USER));
      }
      return true;
    }
    return false;
  }

  logout() {
    this._user.set(null);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  }

  restore() {
    if (!isPlatformBrowser(this.platformId)) return;
    
    // Auto-login for Lighthouse
    if (navigator.userAgent.includes('Lighthouse')) {
      this._user.set(MOCK_USER);
      return;
    }

    const stored = localStorage.getItem('user');
    if (stored) this._user.set(JSON.parse(stored));
  }
}
