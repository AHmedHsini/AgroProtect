import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, AlertTriangle, ArrowLeft } from 'lucide-angular';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-bg-base text-foreground flex flex-col items-center justify-center p-8 relative overflow-hidden">
      <!-- Background effect -->
      <div class="absolute inset-0 z-0">
        <div class="absolute -top-24 -left-24 w-96 h-96 bg-primary/10 blur-[100px] rounded-full"></div>
        <div class="absolute bottom-10 right-10 w-64 h-64 bg-secondary/10 blur-[80px] rounded-full"></div>
      </div>

      <div class="glass-card z-10 flex flex-col items-center text-center p-12 max-w-lg">
        <div class="w-20 h-20 bg-red-500/10 rounded-full flex items-center justify-center mb-6">
          <lucide-icon name="alert-triangle" class="w-10 h-10 text-red-500"></lucide-icon>
        </div>
        <h1 class="text-6xl font-black mb-4 tracking-tighter">404</h1>
        <h2 class="text-2xl font-bold mb-4">Page non trouvée</h2>
        <p class="text-muted-foreground mb-8">
          La page que vous recherchez n'existe pas ou a été déplacée.
        </p>
        
        <a routerLink="/dashboard" class="flex items-center gap-2 px-6 py-3 bg-primary text-black font-bold rounded-xl hover:scale-105 transition-transform">
          <lucide-icon name="arrow-left" class="w-5 h-5"></lucide-icon>
          Retour au Tableau de bord
        </a>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
  `]
})
export class NotFoundComponent {}
