import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Settings, ArrowLeft } from 'lucide-angular';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-bg-base p-8 relative overflow-hidden">
      <div class="flex items-center gap-4 mb-8">
        <a routerLink="/dashboard" class="p-2 hover:bg-white/5 rounded-lg transition-colors">
          <lucide-icon name="arrow-left" class="w-6 h-6"></lucide-icon>
        </a>
        <h1 class="text-3xl font-black">Paramètres</h1>
      </div>
      
      <div class="glass-card flex flex-col items-center justify-center p-20 text-center">
        <div class="w-24 h-24 bg-primary/10 rounded-full flex items-center justify-center mb-6">
          <lucide-icon name="settings" class="w-12 h-12 text-primary"></lucide-icon>
        </div>
        <h2 class="text-2xl font-bold mb-4">Configuration du système</h2>
        <p class="text-muted-foreground max-w-md">
          Préférences d'affichage, intégrations API (Sentinel-2, météo) et alertes personnalisées.
        </p>
      </div>
    </div>
  `
})
export class SettingsComponent {}
