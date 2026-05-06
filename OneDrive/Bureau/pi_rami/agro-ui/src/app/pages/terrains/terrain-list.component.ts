import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TerrainService } from '../../core/services/terrain.service';
import { TerrainAgricole } from '../../core/models/terrain.model';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-terrain-list',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  template: `
    <div class="p-6 md:p-8 animate-fade-in max-w-7xl mx-auto">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold text-slate-100 flex items-center gap-3">
            <lucide-icon name="map" class="w-8 h-8 text-emerald-400"></lucide-icon>
            Mes Terrains
          </h1>
          <p class="text-slate-400 mt-2">Gérez et analysez vos parcelles agricoles</p>
        </div>
        <button class="flex items-center gap-2 bg-emerald-500 hover:bg-emerald-600 text-white px-5 py-2.5 rounded-xl font-medium transition-all shadow-lg shadow-emerald-500/20" routerLink="/simulation">
          <lucide-icon name="zap" class="w-5 h-5"></lucide-icon>
          Nouvelle Analyse
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div *ngFor="let terrain of terrains()" class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 hover:border-emerald-500/50 transition-all cursor-pointer group" [routerLink]="['/terrains', terrain.id]">
          <div class="flex justify-between items-start mb-4">
            <div class="p-3 bg-emerald-500/10 rounded-xl text-emerald-400 group-hover:scale-110 transition-transform">
              <lucide-icon name="leaf" class="w-6 h-6"></lucide-icon>
            </div>
            <span class="text-xs font-medium px-3 py-1 rounded-full bg-slate-700/50 text-slate-300">
              ID: {{ terrain.eosFieldId || 'N/A' }}
            </span>
          </div>
          
          <h3 class="text-xl font-semibold text-white mb-2">{{ terrain.region }}</h3>
          
          <div class="space-y-3 mt-4">
            <div class="flex items-center text-slate-400 text-sm">
              <lucide-icon name="layout" class="w-4 h-4 mr-2"></lucide-icon>
              <span>{{ terrain.surface }} Hectares</span>
            </div>
            <div class="flex items-center text-slate-400 text-sm">
              <lucide-icon name="cloud-sun" class="w-4 h-4 mr-2"></lucide-icon>
              <span>Sol: {{ terrain.typeSol }}</span>
            </div>
          </div>
          
          <div class="mt-6 pt-4 border-t border-slate-700/50 flex justify-between items-center text-emerald-400 text-sm font-medium group-hover:text-emerald-300">
            <span>Voir l'historique de scoring</span>
            <lucide-icon name="arrow-right" class="w-4 h-4 group-hover:translate-x-1 transition-transform"></lucide-icon>
          </div>
        </div>

        <!-- Empty state -->
        <div *ngIf="terrains().length === 0" class="col-span-full py-12 flex flex-col items-center justify-center text-slate-400 bg-slate-800/30 rounded-2xl border border-slate-700/50 border-dashed">
          <lucide-icon name="map" class="w-12 h-12 mb-4 opacity-50"></lucide-icon>
          <h3 class="text-lg font-medium text-slate-300 mb-1">Aucun terrain trouvé</h3>
          <p class="text-sm">Vous n'avez pas encore ajouté de terrain agricole.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class TerrainListComponent implements OnInit {
  private terrainService = inject(TerrainService);
  terrains = signal<TerrainAgricole[]>([]);

  ngOnInit() {
    this.loadTerrains();
  }

  loadTerrains() {
    this.terrainService.getAll().subscribe({
      next: (data) => this.terrains.set(data),
      error: (err) => console.error('Erreur chargement terrains:', err)
    });
  }
}
