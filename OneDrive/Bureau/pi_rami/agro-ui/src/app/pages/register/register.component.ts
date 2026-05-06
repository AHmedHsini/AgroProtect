import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <div class="min-h-screen bg-[#0d0d0d] flex items-center justify-center p-4">
      <div class="text-center">
        <h1 class="font-display text-3xl font-bold text-white mb-4">
          Agro<span class="text-[#00ff88]">Protect</span>
        </h1>
        <p class="text-[#555] mb-8">Page d'inscription — bientôt disponible</p>
        <a routerLink="/login"
           class="px-6 py-3 bg-[#00ff88] text-black font-bold rounded-xl
                  hover:bg-[#00e87a] transition-colors">
          ← Retour connexion
        </a>
      </div>
    </div>
  `,
})
export class RegisterComponent {}
