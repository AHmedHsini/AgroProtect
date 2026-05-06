import { Component, OnInit, inject, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MarketService } from '../../core/services/market.service';
import { TerrainService } from '../../core/services/terrain.service';
import { CropRecommendationDTO, MarketCommodityDTO } from '../../core/models/market.model';
import { TerrainAgricole } from '../../core/models/terrain.model';
import { forkJoin, finalize } from 'rxjs';

@Component({
  selector: 'app-market-example',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="p-6 bg-gray-900 min-h-screen text-white">
      <!-- Header Section -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8 bg-gray-800/40 p-6 rounded-2xl border border-gray-700/50 backdrop-blur-md">
        <div class="flex items-center gap-4">
          <a routerLink="/dashboard" class="p-2.5 bg-gray-800 hover:bg-gray-700 rounded-xl transition-all border border-gray-700 hover:border-green-500/50 group">
            <span class="text-gray-400 group-hover:text-green-400 transition-colors">←</span>
          </a>
          <div>
            <h2 class="text-2xl font-black tracking-tight bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">Analyse du Marché Local</h2>
            <p class="text-xs text-gray-400 mt-1 uppercase tracking-widest font-semibold">Tendances & Opportunités</p>
          </div>
        </div>

        <!-- Terrain Selector -->
        <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3">
          <label for="terrainSelect" class="text-xs font-bold text-gray-400 uppercase tracking-tighter">Sélectionner un terrain :</label>
          <div class="relative min-w-[240px]">
            <select 
              id="terrainSelect"
              [(ngModel)]="selectedTerrainId" 
              (change)="onTerrainChange()"
              class="w-full bg-gray-900 border border-gray-700 text-white text-sm rounded-xl focus:ring-green-500 focus:border-green-500 p-3 appearance-none cursor-pointer hover:border-green-500/50 transition-all outline-none"
            >
              <option [ngValue]="null" disabled>— Choisir un terrain —</option>
              <option *ngFor="let t of terrains" [ngValue]="t.id">
                Terrain #{{ t.id }} - {{ t.region }} ({{ t.surface }} ha)
              </option>
            </select>
            <div class="absolute inset-y-0 right-3 flex items-center pointer-events-none text-gray-500">
              <span class="text-[10px]">▼</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div *ngIf="errorMessage" class="bg-red-500/10 border border-red-500/50 text-red-200 px-6 py-4 rounded-2xl mb-8 flex justify-between items-center backdrop-blur-sm">
        <div class="flex items-center gap-3">
          <span class="p-2 bg-red-500/20 rounded-lg text-red-500 font-bold">⚠️</span>
          <span>{{ errorMessage }}</span>
        </div>
        <button (click)="loadAllMarketData()" class="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-500/30 rounded-xl text-sm font-bold transition-all">
          Réessayer
        </button>
      </div>

      <!-- Initial/Empty State -->
      <div *ngIf="!selectedTerrainId && !isLoading" class="flex flex-col items-center justify-center my-20 p-12 bg-gray-800/20 rounded-3xl border border-dashed border-gray-700">
        <div class="w-20 h-20 bg-green-500/10 rounded-full flex items-center justify-center mb-6 border border-green-500/20">
          <span class="text-3xl">🚜</span>
        </div>
        <h3 class="text-xl font-bold mb-2">Prêt pour l'analyse ?</h3>
        <p class="text-gray-400 text-center max-w-md">Sélectionnez l'un de vos terrains ci-dessus pour obtenir des recommandations de culture basées sur le sol, le climat et les besoins du marché.</p>
      </div>

      <!-- Loading State -->
      <div *ngIf="isLoading" class="flex flex-col items-center justify-center my-24">
        <div class="relative">
          <div class="absolute inset-0 blur-2xl bg-green-500/20 rounded-full"></div>
          <div class="animate-spin rounded-full h-20 w-20 border-t-4 border-b-4 border-green-500 relative"></div>
        </div>
        <p class="text-gray-400 mt-8 font-medium animate-pulse tracking-wide uppercase text-xs">Analyse en cours...</p>
      </div>

      <div *ngIf="!isLoading && selectedTerrainId">
        
        <!-- Opportunities Section -->
        <div class="mb-12">
          <h3 class="text-xl font-bold mb-8 text-white flex items-center gap-3">
            <span class="w-10 h-10 bg-green-500/20 rounded-xl flex items-center justify-center text-xl shadow-lg shadow-green-500/10 border border-green-500/20">🌱</span>
            Recommandations Stratégiques
          </h3>
          
          <div *ngIf="opportunities.length === 0 && !errorMessage" class="bg-gray-800/50 rounded-2xl p-12 text-center border border-dashed border-gray-700">
            <p class="text-gray-400 italic">Aucune opportunité spécifique identifiée pour ce terrain pour le moment.</p>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div *ngFor="let opp of opportunities" class="bg-gray-800/80 backdrop-blur-sm rounded-3xl p-7 shadow-2xl border border-gray-700/50 hover:border-green-500/40 transition-all hover:scale-[1.03] group">
              <div class="flex justify-between items-start mb-6">
                <div>
                  <h4 class="font-black text-2xl text-white group-hover:text-green-400 transition-colors">{{ opp.cropName }}</h4>
                  <span class="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Culture Recommandée</span>
                </div>
                <div class="flex flex-col items-end">
                  <div class="relative">
                    <div class="absolute inset-0 blur-md bg-green-500/30 rounded-full"></div>
                    <span class="relative px-4 py-1.5 text-sm font-black rounded-full bg-green-500 text-white shadow-xl shadow-green-500/20">
                      {{ opp.finalOpportunityScore }}%
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="bg-gray-900/60 rounded-2xl p-4 mb-8 border border-gray-700/30">
                <p class="text-xs text-gray-300 leading-relaxed italic">
                  "{{ opp.justification }}"
                </p>
              </div>
              
              <div class="space-y-5">
                <!-- Besoin Local -->
                <div class="group/bar">
                  <div class="flex justify-between text-[10px] mb-2 font-black uppercase tracking-widest text-gray-500 group-hover/bar:text-blue-400 transition-colors">
                    <span>Besoin Marché</span>
                    <span class="text-white">{{ opp.localNeedScore }}%</span>
                  </div>
                  <div class="h-2.5 bg-gray-900 rounded-full overflow-hidden p-0.5">
                    <div class="h-full bg-gradient-to-r from-blue-600 to-blue-400 rounded-full shadow-[0_0_12px_rgba(59,130,246,0.3)] transition-all duration-1000" [style.width]="opp.localNeedScore + '%'"></div>
                  </div>
                </div>

                <!-- Compatibilité Sol -->
                <div class="group/bar">
                  <div class="flex justify-between text-[10px] mb-2 font-black uppercase tracking-widest text-gray-500 group-hover/bar:text-yellow-400 transition-colors">
                    <span>Score du Sol</span>
                    <span class="text-white">{{ opp.soilCompatibilityScore }}%</span>
                  </div>
                  <div class="h-2.5 bg-gray-900 rounded-full overflow-hidden p-0.5">
                    <div class="h-full bg-gradient-to-r from-yellow-600 to-yellow-400 rounded-full shadow-[0_0_12px_rgba(234,179,8,0.3)] transition-all duration-1000" [style.width]="opp.soilCompatibilityScore + '%'"></div>
                  </div>
                </div>

                <!-- Compatibilité Climat -->
                <div class="group/bar">
                  <div class="flex justify-between text-[10px] mb-2 font-black uppercase tracking-widest text-gray-500 group-hover/bar:text-teal-400 transition-colors">
                    <span>Facteur Climat</span>
                    <span class="text-white">{{ opp.climateCompatibilityScore }}%</span>
                  </div>
                  <div class="h-2.5 bg-gray-900 rounded-full overflow-hidden p-0.5">
                    <div class="h-full bg-gradient-to-r from-teal-600 to-teal-400 rounded-full shadow-[0_0_12px_rgba(20,184,166,0.3)] transition-all duration-1000" [style.width]="opp.climateCompatibilityScore + '%'"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Dependencies Section -->
        <div>
          <h3 class="text-xl font-bold mb-8 text-white flex items-center gap-3">
            <span class="w-10 h-10 bg-yellow-500/20 rounded-xl flex items-center justify-center text-xl shadow-lg shadow-yellow-500/10 border border-yellow-500/20">🔄</span>
            Analyse des Dépendances Locales
          </h3>
          
          <div *ngIf="dependencies.length === 0 && !errorMessage" class="bg-gray-800/50 rounded-2xl p-12 text-center border border-dashed border-gray-700">
            <p class="text-gray-400 italic">Aucune donnée de marché disponible.</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div *ngFor="let dep of dependencies" class="bg-gray-800/80 backdrop-blur-sm rounded-3xl p-7 shadow-2xl border border-gray-700/50">
              <div class="flex justify-between items-center mb-8">
                <h4 class="font-black text-2xl">{{ dep.name }}</h4>
                <div *ngIf="dep.importDependencyRatio > 0.6" class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-500 text-[9px] font-black uppercase tracking-widest">
                  <span class="relative flex h-2 w-2">
                    <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                    <span class="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
                  </span>
                  Alerte Souveraineté
                </div>
              </div>
              
              <div class="space-y-4">
                <div class="flex justify-between items-center p-4 bg-gray-900/60 rounded-2xl border border-gray-700/30">
                  <span class="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Récolte Locale</span>
                  <span class="font-black text-sm">{{ formatNumber(dep.production) }} T</span>
                </div>
                
                <div class="flex justify-between items-center p-4 bg-gray-900/60 rounded-2xl border border-gray-700/30">
                  <span class="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Volume Imports</span>
                  <span class="font-black text-sm text-yellow-500">{{ formatNumber(dep.imports) }} T</span>
                </div>
                
                <div class="pt-4 px-2">
                  <div class="flex justify-between text-[9px] mb-3 font-black uppercase tracking-widest text-gray-500">
                    <span>Taux de Dépendance Extérieure</span>
                    <span class="font-black text-xs" [ngClass]="getDependencyColorClass(dep.importDependencyRatio)">
                      {{ (dep.importDependencyRatio * 100).toFixed(1) }}%
                    </span>
                  </div>
                  <div class="h-4 bg-gray-900 rounded-full overflow-hidden p-1">
                    <div class="h-full rounded-full transition-all duration-1000 shadow-lg" 
                         [ngClass]="getDependencyBgClass(dep.importDependencyRatio)" 
                         [style.width]="(dep.importDependencyRatio * 100) + '%'"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class MarketExampleComponent implements OnInit {
  private marketService = inject(MarketService);
  private terrainService = inject(TerrainService);
  private cdr = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);

  terrains: TerrainAgricole[] = [];
  selectedTerrainId: number | null = null;

  opportunities: CropRecommendationDTO[] = [];
  dependencies: MarketCommodityDTO[] = [];

  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadTerrains();
  }

  loadTerrains(): void {
    this.terrainService.getAll().subscribe({
      next: (data) => {
        this.terrains = data;
        console.log('Terrains chargés :', this.terrains);

        if (this.terrains.length > 0) {
          this.selectedTerrainId = this.terrains[0].id || null;
          if (this.selectedTerrainId) this.loadAllMarketData();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur terrains :', err);
        this.errorMessage = "Impossible de récupérer la liste des terrains.";
        this.cdr.detectChanges();
      }
    });
  }

  onTerrainChange(): void {
    if (this.selectedTerrainId) {
      console.log('Changement de terrain vers ID:', this.selectedTerrainId);
      this.loadAllMarketData();
    }
  }

  loadAllMarketData(): void {
    if (!this.selectedTerrainId) return;

    console.log(`--- Analyse du marché pour le terrain #${this.selectedTerrainId} ---`);
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    forkJoin({
      opportunities: this.marketService.getMarketOpportunities(this.selectedTerrainId),
      dependencies: this.marketService.getMarketDependencies()
    }).pipe(
      finalize(() => {
        this.ngZone.run(() => {
          this.isLoading = false;
          console.log('--- Fin de l\'analyse stratégique ---');
          this.cdr.detectChanges();
        });
      })
    ).subscribe({
      next: (result) => {
        this.ngZone.run(() => {
          console.log('Résultats de l\'analyse :', result);
          this.opportunities = Array.isArray(result.opportunities) ? result.opportunities : [];
          this.dependencies = Array.isArray(result.dependencies) ? result.dependencies : [];
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          console.error('Erreur analyse :', err);
          this.errorMessage = "Une erreur est survenue lors de l'analyse stratégique.";
          this.cdr.detectChanges();
        });
      }
    });
  }

  formatNumber(value: number): string {
    if (!value && value !== 0) return '0';
    if (value >= 1000000) return (value / 1000000).toFixed(1) + ' M';
    if (value >= 1000) return (value / 1000).toFixed(0) + ' k';
    return value.toLocaleString();
  }

  getDependencyColorClass(ratio: number): string {
    if (ratio > 0.6) return 'text-red-500';
    if (ratio > 0.3) return 'text-yellow-500';
    return 'text-green-500';
  }

  getDependencyBgClass(ratio: number): string {
    if (ratio > 0.6) return 'bg-gradient-to-r from-red-600 to-red-400';
    if (ratio > 0.3) return 'bg-gradient-to-r from-yellow-600 to-yellow-400';
    return 'bg-gradient-to-r from-green-600 to-green-400';
  }
}
