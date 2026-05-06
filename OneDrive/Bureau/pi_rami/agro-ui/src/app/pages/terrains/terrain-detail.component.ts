import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TerrainService } from '../../core/services/terrain.service';
import { ScoringService } from '../../core/services/scoring.service';
import { SoilService } from '../../core/services/soil.service';
import { SatelliteService } from '../../core/services/satellite.service';
import { MeteoService } from '../../core/services/meteo.service';
import { TerrainAgricole } from '../../core/models/terrain.model';
import { ScoreAgricole, ScoreBreakdownDTO, DecisionDTO, RecommandationDTO } from '../../core/models/score.model';
import { SoilData } from '../../core/models/soil.model';
import { SatelliteIndexDTO, SatelliteBiomasseDTO } from '../../core/models/satellite.model';
import { MeteoDTO } from '../../core/models/meteo.model';
import { LucideAngularModule } from 'lucide-angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { forkJoin, catchError, of, tap } from 'rxjs';

@Component({
  selector: 'app-terrain-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule, BaseChartDirective],
  template: `
    <div class="p-6 md:p-8 animate-fade-in max-w-7xl mx-auto">
      
      <!-- Loading State -->
      <div *ngIf="loading()" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/80 backdrop-blur-sm">
        <div class="flex flex-col items-center gap-4">
          <lucide-icon name="loader" class="w-12 h-12 text-emerald-400 animate-spin"></lucide-icon>
          <span class="text-white text-lg font-medium">Chargement des données...</span>
        </div>
      </div>

      <div *ngIf="terrain()">
        <!-- Header -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <div class="flex items-center gap-2 text-slate-400 text-sm mb-2">
              <a routerLink="/terrains" class="hover:text-emerald-400 transition-colors">Terrains</a>
              <lucide-icon name="chevron-right" class="w-4 h-4"></lucide-icon>
              <span class="text-slate-200">Détails de la parcelle</span>
            </div>
            <h1 class="text-3xl font-bold text-slate-100 flex items-center gap-3">
              {{ terrain()?.region }} 
              <span class="text-sm font-normal px-3 py-1 bg-slate-800 text-slate-300 rounded-full border border-slate-700">
                ID: {{ terrain()?.eosFieldId || 'N/A' }}
              </span>
            </h1>
          </div>
          <button class="flex items-center justify-center gap-2 bg-emerald-500 hover:bg-emerald-600 text-white px-5 py-2.5 rounded-xl font-medium transition-all shadow-lg shadow-emerald-500/20" (click)="recalculerScore()" [disabled]="calculating()">
            <lucide-icon [name]="calculating() ? 'loader' : 'refresh-cw'" [class.animate-spin]="calculating()" class="w-5 h-5"></lucide-icon>
            {{ calculating() ? 'Analyse en cours...' : 'Lancer Analyse' }}
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          <!-- Informations Terrain -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="info" class="w-5 h-5 text-emerald-400"></lucide-icon>
              Informations
            </h2>
            <div class="space-y-4">
              <div class="flex justify-between items-center py-2 border-b border-slate-700/50">
                <span class="text-slate-400">Surface</span>
                <span class="text-slate-100 font-medium">{{ terrain()?.surface }} Ha</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-700/50">
                <span class="text-slate-400">Type de Sol (Déclaré)</span>
                <span class="text-slate-100 font-medium">{{ terrain()?.typeSol }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-700/50">
                <span class="text-slate-400">Latitude</span>
                <span class="text-slate-100 font-medium">{{ terrain()?.latitude }}</span>
              </div>
              <div class="flex justify-between items-center py-2">
                <span class="text-slate-400">Longitude</span>
                <span class="text-slate-100 font-medium">{{ terrain()?.longitude }}</span>
              </div>
            </div>
          </div>

          <!-- Score Actuel -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 flex flex-col items-center justify-center relative">
            <h2 class="text-lg font-semibold text-white mb-6 w-full text-left">Score Agricole Global</h2>
            <div *ngIf="dernierScore(); else noScore" class="relative w-48 h-48 flex items-center justify-center">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="45" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="10" />
                <circle cx="50" cy="50" r="45" fill="none" [attr.stroke]="getScoreColor(dernierScore()!.score)" stroke-width="10" stroke-linecap="round"
                  [attr.stroke-dasharray]="2 * 3.14159 * 45"
                  [attr.stroke-dashoffset]="(2 * 3.14159 * 45) * (1 - dernierScore()!.score / 100)" 
                  class="transition-all duration-1000 ease-out" />
              </svg>
              <div class="absolute flex flex-col items-center">
                <span class="text-5xl font-bold text-white">{{ dernierScore()?.score | number:'1.0-0' }}</span>
                <span class="text-sm text-slate-400 mt-1">/ 100</span>
              </div>
            </div>
            <div *ngIf="dernierScore()" class="mt-6 text-center">
              <span class="px-5 py-2 rounded-full text-sm font-bold shadow-lg" 
                [ngClass]="{
                  'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 shadow-emerald-500/20': dernierScore()!.niveau === 'FAIBLE',
                  'bg-orange-500/20 text-orange-400 border border-orange-500/30 shadow-orange-500/20': dernierScore()!.niveau === 'MOYEN',
                  'bg-red-500/20 text-red-400 border border-red-500/30 shadow-red-500/20': dernierScore()!.niveau === 'ELEVE'
                }">
                Risque {{ dernierScore()?.niveau }}
              </span>
            </div>
            <ng-template #noScore>
              <div class="text-slate-400 text-center">
                <lucide-icon name="bar-chart-2" class="w-12 h-12 mx-auto mb-3 opacity-50"></lucide-icon>
                <p>Aucun score calculé pour le moment.</p>
              </div>
            </ng-template>
          </div>

          <!-- Décision -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="shield-check" class="w-5 h-5 text-blue-400"></lucide-icon>
              Décision de Financement
            </h2>
            <div *ngIf="decision(); else noDecision" class="flex flex-col h-full justify-center pb-8">
              <div class="text-center mb-6">
                <div class="inline-block px-6 py-3 rounded-2xl border-2 text-xl font-bold uppercase tracking-wider"
                  [ngClass]="{
                    'border-emerald-500 text-emerald-400 bg-emerald-500/10': decision()!.decision === 'FINANCER',
                    'border-orange-500 text-orange-400 bg-orange-500/10': decision()!.decision === 'SURVEILLER',
                    'border-red-500 text-red-400 bg-red-500/10': decision()!.decision === 'REFUSER'
                  }">
                  {{ decision()?.decision }}
                </div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                <p class="text-sm text-slate-300 italic">"{{ decision()?.raisonPrincipale }}"</p>
                <div class="mt-3 flex items-center justify-between text-xs">
                  <span class="text-slate-400">Indice de Confiance</span>
                  <span class="text-blue-400 font-bold">{{ decision()?.confiance | number:'1.0-0' }}%</span>
                </div>
              </div>
            </div>
            <ng-template #noDecision>
              <p class="text-sm text-slate-400">Décision indisponible.</p>
            </ng-template>
          </div>

          <!-- Breakdown du Score -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="pie-chart" class="w-5 h-5 text-indigo-400"></lucide-icon>
              Détail du Score
            </h2>
            <div *ngIf="breakdown(); else noBreakdown" class="space-y-4">
               <div *ngFor="let item of getBreakdownKeys()" class="mb-3">
                 <div class="flex justify-between text-sm mb-1">
                   <span class="text-slate-300 capitalize">{{ formatKey(item) }}</span>
                   <span class="text-slate-100 font-medium">{{ breakdown()?.[item] | number:'1.0-0' }}/100</span>
                 </div>
                 <div class="w-full bg-slate-700/50 rounded-full h-2">
                   <div class="h-2 rounded-full" [ngClass]="getBarColor(breakdown()![item])" [style.width.%]="breakdown()?.[item] || 0"></div>
                 </div>
               </div>
            </div>
            <ng-template #noBreakdown>
              <p class="text-sm text-slate-400">Détails indisponibles.</p>
            </ng-template>
          </div>

          <!-- Satellite Data -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="satellite" class="w-5 h-5 text-cyan-400"></lucide-icon>
              Données Satellite
            </h2>
            <div *ngIf="satelliteNdvi(); else noSatellite" class="space-y-4">
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50 text-center">
                  <div class="text-xs text-slate-400 uppercase tracking-wider mb-1">NDVI</div>
                  <div class="text-2xl font-bold text-cyan-400">{{ satelliteNdvi()?.ndvi | number:'1.2-2' }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50 text-center">
                  <div class="text-xs text-slate-400 uppercase tracking-wider mb-1">EVI</div>
                  <div class="text-2xl font-bold text-blue-400">{{ satelliteNdvi()?.evi | number:'1.2-2' }}</div>
                </div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                <div class="flex justify-between items-center mb-2">
                  <span class="text-sm text-slate-400">Santé Végétation</span>
                  <span class="text-sm font-bold text-emerald-400">{{ satelliteNdvi()?.niveauSante }}</span>
                </div>
                <p class="text-xs text-slate-300 leading-relaxed">{{ satelliteNdvi()?.interpretation }}</p>
              </div>
              <div *ngIf="satelliteBiomasse()" class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50 flex justify-between items-center">
                <span class="text-sm text-slate-400">Biomasse Est.</span>
                <span class="text-sm font-bold text-white">{{ satelliteBiomasse()?.biomasseEstimee | number:'1.0-0' }} kg/ha</span>
              </div>
            </div>
            <ng-template #noSatellite>
              <p class="text-sm text-slate-400">Données satellite indisponibles.</p>
            </ng-template>
          </div>

          <!-- Météo Data -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="cloud-rain" class="w-5 h-5 text-sky-400"></lucide-icon>
              Météo Actuelle
            </h2>
            <div *ngIf="meteo(); else noMeteo" class="h-full flex flex-col justify-between">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <div class="text-3xl font-bold text-white">{{ meteo()?.temperature | number:'1.0-0' }}°C</div>
                  <div class="text-sm text-slate-400 capitalize">{{ meteo()?.description }}</div>
                </div>
                <lucide-icon name="sun" class="w-12 h-12 text-yellow-400"></lucide-icon>
              </div>
              <div class="grid grid-cols-2 gap-3 mb-4">
                <div class="flex items-center gap-2 bg-slate-900/50 p-3 rounded-lg border border-slate-700/50">
                  <lucide-icon name="droplets" class="w-4 h-4 text-blue-400"></lucide-icon>
                  <div>
                    <div class="text-xs text-slate-400">Humidité</div>
                    <div class="text-sm font-medium text-white">{{ meteo()?.humidity }}%</div>
                  </div>
                </div>
                <div class="flex items-center gap-2 bg-slate-900/50 p-3 rounded-lg border border-slate-700/50">
                  <lucide-icon name="wind" class="w-4 h-4 text-slate-300"></lucide-icon>
                  <div>
                    <div class="text-xs text-slate-400">Vent</div>
                    <div class="text-sm font-medium text-white">{{ meteo()?.windSpeed }} m/s</div>
                  </div>
                </div>
              </div>
              <div class="bg-sky-500/10 p-3 rounded-lg border border-sky-500/20 text-xs text-sky-200">
                {{ meteo()?.resume }}
              </div>
            </div>
            <ng-template #noMeteo>
              <p class="text-sm text-slate-400">Données météo indisponibles.</p>
            </ng-template>
          </div>

          <!-- Soil Data -->
          <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
            <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="mountain" class="w-5 h-5 text-amber-600"></lucide-icon>
              Analyse du Sol
            </h2>
            <div *ngIf="soil(); else noSoil" class="space-y-4">
              <div class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                <div class="text-xs text-slate-400 uppercase tracking-wider mb-1">Classe WRB</div>
                <div class="text-lg font-bold text-amber-500">{{ soil()?.wrbClass || 'Inconnue' }}</div>
                <div class="text-xs text-slate-500 mt-1">Probabilité: {{ (soil()?.probability || 0) * 100 | number:'1.0-0' }}%</div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50 flex flex-col justify-center items-center">
                  <div class="text-xs text-slate-400 mb-1">pH</div>
                  <div class="text-xl font-bold text-white">{{ soil()?.ph | number:'1.1-1' }}</div>
                </div>
                <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50 flex flex-col justify-center items-center">
                  <div class="text-xs text-slate-400 mb-1">Carbone Org.</div>
                  <div class="text-xl font-bold text-white">{{ soil()?.organicCarbon | number:'1.1-1' }}%</div>
                </div>
              </div>
              <div class="flex items-center gap-2 mt-2">
                <div class="flex-1 h-2 bg-slate-700 rounded-full overflow-hidden flex">
                  <div class="bg-yellow-600 h-full" [style.width.%]="soil()?.sand"></div>
                  <div class="bg-amber-800 h-full" [style.width.%]="soil()?.clay"></div>
                </div>
                <div class="text-xs text-slate-400 flex gap-2">
                  <span class="text-yellow-500">Sable {{ soil()?.sand }}%</span>
                  <span class="text-amber-600">Argile {{ soil()?.clay }}%</span>
                </div>
              </div>
            </div>
            <ng-template #noSoil>
              <p class="text-sm text-slate-400">Données sol indisponibles.</p>
            </ng-template>
          </div>

          <!-- Recommendations -->
          <div class="lg:col-span-2 bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
             <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="lightbulb" class="w-5 h-5 text-yellow-400"></lucide-icon>
              Recommandations & Actions
            </h2>
            <div *ngIf="recommandations() && recommandations()!.length > 0; else noReco" class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div *ngFor="let rec of recommandations()" 
                   class="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50 flex gap-4 items-start">
                <div class="mt-1" [ngSwitch]="rec.priorite">
                  <lucide-icon *ngSwitchCase="'HAUTE'" name="alert-triangle" class="w-5 h-5 text-red-400"></lucide-icon>
                  <lucide-icon *ngSwitchCase="'MOYENNE'" name="alert-circle" class="w-5 h-5 text-orange-400"></lucide-icon>
                  <lucide-icon *ngSwitchDefault name="info" class="w-5 h-5 text-blue-400"></lucide-icon>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-200 mb-1">{{ rec.type }}</div>
                  <p class="text-xs text-slate-400 leading-relaxed">{{ rec.message }}</p>
                </div>
              </div>
            </div>
            <ng-template #noReco>
               <p class="text-sm text-slate-400">Aucune recommandation disponible.</p>
            </ng-template>
          </div>

        </div> <!-- End Grid -->
        
        <!-- Graphique Historique -->
        <div class="mt-6 bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 mb-8">
           <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <lucide-icon name="line-chart" class="w-5 h-5 text-emerald-400"></lucide-icon>
              Évolution du Score
            </h2>
            <div class="h-72" *ngIf="chartData">
              <canvas baseChart
                [data]="chartData"
                [options]="chartOptions"
                [type]="'line'">
              </canvas>
            </div>
            <div *ngIf="!chartData" class="h-72 flex items-center justify-center text-slate-400">
              <lucide-icon name="activity" class="w-8 h-8 mr-3 opacity-50"></lucide-icon>
              Données insuffisantes pour l'historique.
            </div>
        </div>

      </div> <!-- End *ngIf="terrain()" -->

    </div>
  `
})
export class TerrainDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private terrainService = inject(TerrainService);
  private scoringService = inject(ScoringService);
  private soilService = inject(SoilService);
  private satelliteService = inject(SatelliteService);
  private meteoService = inject(MeteoService);

  loading = signal<boolean>(true);
  calculating = signal<boolean>(false);

  terrain = signal<TerrainAgricole | null>(null);
  dernierScore = signal<ScoreAgricole | null>(null);
  breakdown = signal<ScoreBreakdownDTO | null>(null);
  decision = signal<DecisionDTO | null>(null);
  recommandations = signal<RecommandationDTO[] | null>(null);
  
  soil = signal<SoilData | null>(null);
  satelliteNdvi = signal<SatelliteIndexDTO | null>(null);
  satelliteBiomasse = signal<SatelliteBiomasseDTO | null>(null);
  meteo = signal<MeteoDTO | null>(null);

  // Chart
  chartData: ChartConfiguration<'line'>['data'] | undefined;
  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        titleColor: '#fff',
        bodyColor: '#cbd5e1',
        borderColor: 'rgba(51, 65, 85, 0.5)',
        borderWidth: 1,
        padding: 10,
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#94a3b8' }
      },
      x: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#94a3b8' }
      }
    }
  };

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadAllData(+id);
    }
  }

  loadAllData(id: number) {
    this.loading.set(true);

    // 1. Load Terrain first to display basic info
    this.terrainService.getById(id).pipe(
      tap(t => this.terrain.set(t)),
      catchError(err => {
        console.error("Error loading terrain", err);
        return of(null);
      })
    ).subscribe(t => {
      if(t) {
        // 2. Load all other dashboard metrics concurrently
        forkJoin({
          score: this.scoringService.getDernierScore(id).pipe(catchError(() => of(null))),
          breakdown: this.scoringService.getBreakdown(id).pipe(catchError(() => of(null))),
          decision: this.scoringService.getDecision(id).pipe(catchError(() => of(null))),
          recommandations: this.scoringService.getRecommandations(id).pipe(catchError(() => of(null))),
          evolution: this.scoringService.getEvolution(id).pipe(catchError(() => of(null))),
          soil: this.soilService.getSoilData(id).pipe(catchError(() => of(null))),
          ndvi: this.satelliteService.getNdvi(id).pipe(catchError(() => of(null))),
          biomasse: this.satelliteService.getBiomasse(id).pipe(catchError(() => of(null))),
          meteo: this.meteoService.getMeteo(id).pipe(catchError(() => of(null)))
        }).subscribe({
          next: (res) => {
            this.dernierScore.set(res.score);
            this.breakdown.set(res.breakdown);
            this.decision.set(res.decision);
            this.recommandations.set(res.recommandations);
            this.soil.set(res.soil);
            this.satelliteNdvi.set(res.ndvi);
            this.satelliteBiomasse.set(res.biomasse);
            this.meteo.set(res.meteo);

            if (res.evolution && res.evolution.length > 0) {
              this.chartData = {
                labels: res.evolution.map(e => e.date),
                datasets: [
                  {
                    data: res.evolution.map(e => e.score),
                    label: 'Score',
                    borderColor: '#00ff88',
                    backgroundColor: 'rgba(0, 255, 136, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#00ff88',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: '#00ff88'
                  }
                ]
              };
            } else {
              this.chartData = undefined;
            }
            this.loading.set(false);
          },
          error: (err) => {
            console.error("Error loading dashboard data", err);
            this.loading.set(false);
          }
        });
      } else {
        this.loading.set(false);
      }
    });
  }

  recalculerScore() {
    const t = this.terrain();
    if(t && t.id) {
      this.calculating.set(true);
      this.scoringService.calculerScore(t.id).subscribe({
        next: () => {
          this.calculating.set(false);
          this.loadAllData(t.id!);
        },
        error: (err) => {
          console.error("Error calculating score", err);
          this.calculating.set(false);
        }
      });
    }
  }

  getBreakdownKeys(): string[] {
    const b = this.breakdown();
    if (!b) return [];
    return ['agronomique', 'climatique', 'productivite', 'stabilite', 'marketScore'];
  }

  formatKey(key: string): string {
    return key.replace(/([A-Z])/g, ' $1').trim();
  }

  getScoreColor(score: number): string {
    if (score >= 70) return '#10b981'; // emerald-500
    if (score >= 40) return '#f97316'; // orange-500
    return '#ef4444'; // red-500
  }

  getBarColor(value: number): string {
    if (value >= 70) return 'bg-emerald-500';
    if (value >= 40) return 'bg-orange-500';
    return 'bg-red-500';
  }
}
