import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TerrainService } from '../../core/services/terrain.service';
import { ScoringService } from '../../core/services/scoring.service';
import { SoilService } from '../../core/services/soil.service';
import { SatelliteService } from '../../core/services/satellite.service';
import { MeteoService } from '../../core/services/meteo.service';
import { TerrainAgricole } from '../../core/models/terrain.model';
import { DecisionDTO, ScoreAgricole, ScoreBreakdownDTO, RecommandationDTO, EvolutionScoreDTO, CropScoreComparisonDTO } from '../../core/models/score.model';
import { SoilData } from '../../core/models/soil.model';
import { SatelliteIndexDTO, SatelliteBiomasseDTO } from '../../core/models/satellite.model';
import { MeteoDTO, MeteoHistoriqueDTO } from '../../core/models/meteo.model';
import { LucideAngularModule } from 'lucide-angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-simulation',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, BaseChartDirective],
  template: `
    <div class="p-6 md:p-8 animate-fade-in max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-slate-100 flex items-center gap-3 mb-2">
        <lucide-icon name="zap" class="w-8 h-8 text-emerald-400"></lucide-icon>
        Simulation & Analyse Avancée
      </h1>
      <p class="text-slate-400 mb-8">Évaluez un terrain et récupérez l'ensemble des données d'analyse réelles du backend.</p>

      <div *ngIf="errorMessage()" class="p-4 mb-6 bg-red-500/10 border border-red-500/50 text-red-400 rounded-xl flex items-center gap-3">
        <lucide-icon name="alert-triangle" class="w-5 h-5"></lucide-icon>
        <span>{{ errorMessage() }}</span>
      </div>

      <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-4 items-end">
          <div class="flex-1 w-full">
            <label class="block text-sm font-medium text-slate-300 mb-2">Sélectionnez un terrain pour l'analyse</label>
            <select [(ngModel)]="selectedTerrainId" class="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors">
              <option [ngValue]="null" disabled>Choisir un terrain...</option>
              <option *ngFor="let t of terrains()" [ngValue]="t.id">{{ t.region }} - {{ t.surface }} Ha</option>
            </select>
          </div>
          <button (click)="lancerSimulation()" [disabled]="!selectedTerrainId || isLoading()" class="flex-1 md:flex-none flex justify-center items-center gap-2 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white px-8 py-3 rounded-xl font-medium transition-all shadow-lg shadow-emerald-500/20">
            <lucide-icon *ngIf="isLoading()" name="loader" class="w-5 h-5 animate-spin"></lucide-icon>
            <lucide-icon *ngIf="!isLoading()" name="cpu" class="w-5 h-5"></lucide-icon>
            {{ isLoading() ? 'Calcul en cours...' : 'Lancer l\\'analyse' }}
          </button>
        </div>

        <div class="mt-4 text-right">
          <button (click)="showNewTerrainForm = !showNewTerrainForm" class="text-sm text-emerald-400 hover:text-emerald-300 transition-colors underline flex items-center justify-end w-full gap-1">
            <lucide-icon [name]="showNewTerrainForm ? 'x-circle' : 'check-circle'" class="w-4 h-4"></lucide-icon>
            {{ showNewTerrainForm ? 'Annuler la création' : 'Ajouter un nouveau terrain' }}
          </button>
        </div>
        
        <div *ngIf="showNewTerrainForm" class="mt-4 p-4 bg-slate-900 rounded-xl border border-slate-700 animate-fade-in">
          <h3 class="text-white font-medium mb-4">Créer un nouveau terrain (POST /api/terrain)</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-xs text-slate-400 mb-1 block">Région</label>
              <input type="text" [(ngModel)]="newTerrain.region" placeholder="ex: Beja" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white outline-none focus:border-emerald-500">
            </div>
            <div>
              <label class="text-xs text-slate-400 mb-1 block">Surface (Hectares)</label>
              <input type="number" [(ngModel)]="newTerrain.surface" placeholder="ex: 12" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white outline-none focus:border-emerald-500">
            </div>
            <div>
              <label class="text-xs text-slate-400 mb-1 block">Type de sol</label>
              <input type="text" [(ngModel)]="newTerrain.typeSol" placeholder="ex: Argileux" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white outline-none focus:border-emerald-500">
            </div>
            <div class="flex gap-2">
              <div class="w-1/2">
                <label class="text-xs text-slate-400 mb-1 block">Latitude</label>
                <input type="number" [(ngModel)]="newTerrain.latitude" placeholder="36.7" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white outline-none focus:border-emerald-500">
              </div>
              <div class="w-1/2">
                <label class="text-xs text-slate-400 mb-1 block">Longitude</label>
                <input type="number" [(ngModel)]="newTerrain.longitude" placeholder="9.1" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white outline-none focus:border-emerald-500">
              </div>
            </div>
          </div>
          <div class="mt-4 flex justify-end">
            <button (click)="creerTerrain()" [disabled]="isCreating || !newTerrain.region || !newTerrain.surface" class="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white px-4 py-2 rounded-lg text-sm font-medium flex items-center gap-2">
              <lucide-icon *ngIf="isCreating" name="loader" class="w-4 h-4 animate-spin"></lucide-icon>
              Enregistrer le terrain
            </button>
          </div>
        </div>
      </div>

      <!-- JSON DEBUG VIEW -->
      <div *ngIf="result() && !isLoading()" class="mb-8">
         <details class="bg-slate-900 rounded-xl border border-slate-700 group">
           <summary class="p-4 cursor-pointer text-sm font-medium text-slate-400 flex items-center justify-between">
             Afficher les données brutes (Debug JSON)
             <lucide-icon name="chevron-right" class="w-4 h-4 transition-transform group-open:rotate-90"></lucide-icon>
           </summary>
           <div class="p-4 pt-0 overflow-auto max-h-64 border-t border-slate-800">
             <pre class="text-xs text-emerald-400">{{ result() | json }}</pre>
           </div>
         </details>
      </div>

      <!-- RESULTS DASHBOARD -->
      <div *ngIf="score() && !isLoading()" class="grid grid-cols-1 lg:grid-cols-3 gap-6 animate-fade-in">
        
        <!-- MAIN SCORE & DECISION -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 lg:col-span-1 flex flex-col items-center justify-center">
          <h2 class="text-lg font-semibold text-white mb-6 w-full text-left">Score Agricole Global</h2>
          <div class="relative w-48 h-48 flex items-center justify-center">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="45" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="10" />
              <circle cx="50" cy="50" r="45" fill="none" [attr.stroke]="getScoreColor((score()?.score ?? 0))" stroke-width="10" stroke-linecap="round"
                [attr.stroke-dasharray]="2 * 3.14159 * 45"
                [attr.stroke-dashoffset]="(2 * 3.14159 * 45) * (1 - (score()?.score ?? 0) / 100)" 
                class="transition-all duration-1000 ease-out" />
            </svg>
            <div class="absolute flex flex-col items-center">
              <span class="text-5xl font-bold text-white">{{ (score()?.score ?? 0).toFixed(0) }}</span>
              <span class="text-sm text-slate-400 mt-1">/ 100</span>
            </div>
          </div>
          
          <div *ngIf="decision()" class="mt-8 text-center w-full">
            <div class="inline-block w-full px-6 py-4 rounded-xl border-2 text-xl font-bold uppercase tracking-wider text-center"
              [ngClass]="{
                'border-emerald-500 text-emerald-400 bg-emerald-500/10': decision()!.decision === 'FINANCER',
                'border-orange-500 text-orange-400 bg-orange-500/10': decision()!.decision === 'SURVEILLER',
                'border-red-500 text-red-400 bg-red-500/10': decision()!.decision === 'REFUSER'
              }">
              {{ decision()?.decision }}
            </div>
            <p class="text-sm text-slate-300 mt-4">{{ decision()?.raisonPrincipale }}</p>
            <div class="mt-2 text-xs text-slate-400">Indice de confiance : <span class="text-white">{{ (decision()?.confiance ?? 0).toFixed(0) }}%</span></div>
          </div>
        </div>

        <!-- BREAKDOWN -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 lg:col-span-2">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="pie-chart" class="w-5 h-5 text-indigo-400"></lucide-icon>
            Analyse Détaillée (Breakdown)
          </h2>
          <div *ngIf="breakdown(); else noBreakdown" class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
             <div *ngFor="let item of getBreakdownKeys()" class="w-full">
               <div class="flex justify-between text-sm mb-1">
                 <span class="text-slate-300 capitalize">{{ formatKey(item) }}</span>
                 <span class="text-slate-100 font-medium">{{ (breakdown()?.[item] ?? 0).toFixed(0) }}/100</span>
               </div>
               <div class="w-full bg-slate-700/50 rounded-full h-2">
                 <div class="h-2 rounded-full" [ngClass]="getBarColor(breakdown()![item])" [style.width.%]="breakdown()?.[item] || 0"></div>
               </div>
             </div>
          </div>
          <ng-template #noBreakdown><p class="text-sm text-slate-400">Aucun détail disponible.</p></ng-template>
        </div>

        <!-- SATELLITE -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="satellite" class="w-5 h-5 text-cyan-400"></lucide-icon>
            Données Satellite
          </h2>
          <div *ngIf="satelliteNdvi(); else noData" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50 text-center">
                <div class="text-xs text-slate-400 uppercase">NDVI</div>
                <div class="text-xl font-bold text-cyan-400">{{ (satelliteNdvi()?.ndvi ?? 0).toFixed(2) }}</div>
              </div>
              <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50 text-center">
                <div class="text-xs text-slate-400 uppercase">EVI</div>
                <div class="text-xl font-bold text-blue-400">{{ (satelliteNdvi()?.evi ?? 0).toFixed(2) }}</div>
              </div>
            </div>
            <div class="text-sm font-medium text-emerald-400">Santé: {{ satelliteNdvi()?.niveauSante }}</div>
            <div *ngIf="satelliteBiomasse()" class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50 flex justify-between items-center">
              <span class="text-sm text-slate-400">Biomasse</span>
              <span class="text-sm font-bold text-white">{{ (satelliteBiomasse()?.biomasseEstimee ?? 0).toFixed(0) }} kg/ha</span>
            </div>
          </div>
        </div>

        <!-- SOIL -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="mountain" class="w-5 h-5 text-amber-600"></lucide-icon>
            Analyse du Sol
          </h2>
          <div *ngIf="soil(); else noData" class="space-y-4">
            <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50">
              <div class="text-xs text-slate-400">Classe WRB</div>
              <div class="text-md font-bold text-amber-500">{{ soil()?.wrbClass || 'N/A' }}</div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50">
                <div class="text-xs text-slate-400">pH</div>
                <div class="text-lg font-bold text-white">{{ (soil()?.ph ?? 0).toFixed(2) }}</div>
              </div>
              <div class="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50">
                <div class="text-xs text-slate-400">Carbone Org.</div>
                <div class="text-lg font-bold text-white">{{ (soil()?.organicCarbon ?? 0).toFixed(2) }}%</div>
              </div>
            </div>
          </div>
        </div>

        <!-- METEO -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="cloud-rain" class="w-5 h-5 text-sky-400"></lucide-icon>
            Météo
          </h2>
          <div *ngIf="meteo(); else noData" class="space-y-4">
            <div class="flex items-center justify-between">
              <div class="text-3xl font-bold text-white">{{ (meteo()?.temperature ?? 0).toFixed(1) }}°C</div>
              <div class="text-sm text-slate-400 capitalize">{{ meteo()?.description }}</div>
            </div>
            <div class="grid grid-cols-2 gap-2 mt-4">
              <div class="bg-slate-900/50 p-2 rounded-lg border border-slate-700/50">
                <div class="text-xs text-slate-400">Humidité</div>
                <div class="text-sm text-white">{{ (meteo()?.humidity ?? 0).toFixed(0) }}%</div>
              </div>
              <div class="bg-slate-900/50 p-2 rounded-lg border border-slate-700/50">
                <div class="text-xs text-slate-400">Vent</div>
                <div class="text-sm text-white">{{ (meteo()?.windSpeed ?? 0).toFixed(1) }} m/s</div>
              </div>
            </div>
          </div>
        </div>

        <!-- RECOMMENDATIONS & CROPS -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 lg:col-span-3">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="lightbulb" class="w-5 h-5 text-yellow-400"></lucide-icon>
            Recommandations & Cultures
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Recos -->
            <div *ngIf="recommandations() && recommandations()!.length > 0">
              <h3 class="text-sm font-medium text-slate-400 mb-3">Actions requises</h3>
              <div class="space-y-3">
                <div *ngFor="let rec of recommandations()" class="bg-slate-900/50 p-3 rounded-lg border border-slate-700/50 flex gap-3">
                  <lucide-icon [name]="rec.priorite === 'HAUTE' ? 'alert-triangle' : 'info'" [class]="rec.priorite === 'HAUTE' ? 'text-red-400' : 'text-blue-400'" class="w-5 h-5 flex-shrink-0"></lucide-icon>
                  <div>
                    <div class="text-sm font-bold text-slate-200">{{ rec.type }}</div>
                    <div class="text-xs text-slate-400">{{ rec.message }}</div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Crop -->
            <div *ngIf="cropData()">
              <h3 class="text-sm font-medium text-slate-400 mb-3">Simulation Culture : {{ cropData()?.culture }}</h3>
              <div class="bg-emerald-900/20 p-4 rounded-xl border border-emerald-700/30">
                <div class="flex justify-between items-center mb-2">
                  <span class="text-sm text-slate-300">Score de compatibilité</span>
                  <span class="text-lg font-bold text-emerald-400">{{ (cropData()?.scoreMatch ?? 0).toFixed(0) }}%</span>
                </div>
                <ul class="text-xs text-slate-400 list-disc pl-4 space-y-1">
                  <li *ngFor="let r of cropData()?.recommandationsSpecifiques">{{ r }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- EVOLUTION CHART -->
        <div class="bg-slate-800/50 backdrop-blur-xl border border-slate-700/50 rounded-2xl p-6 lg:col-span-3 mb-8">
          <h2 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <lucide-icon name="line-chart" class="w-5 h-5 text-emerald-400"></lucide-icon>
            Évolution
          </h2>
          <div class="h-64" *ngIf="chartData">
            <canvas baseChart [data]="chartData" [options]="chartOptions" [type]="'line'"></canvas>
          </div>
        </div>

      </div>

      <ng-template #noData>
        <p class="text-sm text-slate-500 italic mt-2">Données non disponibles dans l'API.</p>
      </ng-template>

    </div>
  `
})
export class SimulationComponent implements OnInit {
  private terrainService = inject(TerrainService);
  private scoringService = inject(ScoringService);
  private soilService = inject(SoilService);
  private satelliteService = inject(SatelliteService);
  private meteoService = inject(MeteoService);

  terrains = signal<TerrainAgricole[]>([]);
  selectedTerrainId: number | null = null;
  
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Form creation
  showNewTerrainForm = false;
  isCreating = false;
  newTerrain: TerrainAgricole = { region: '', surface: 0, typeSol: '', latitude: 0, longitude: 0 };

  // Result Signals
  score = signal<ScoreAgricole | null>(null);
  decision = signal<DecisionDTO | null>(null);
  breakdown = signal<ScoreBreakdownDTO | null>(null);
  recommandations = signal<RecommandationDTO[] | null>(null);
  soil = signal<SoilData | null>(null);
  satelliteNdvi = signal<SatelliteIndexDTO | null>(null);
  satelliteBiomasse = signal<SatelliteBiomasseDTO | null>(null);
  meteo = signal<MeteoDTO | null>(null);
  cropData = signal<CropScoreComparisonDTO | null>(null);
  result = signal<any | null>(null);

  // Chart configuration
  chartData: ChartConfiguration<'line'>['data'] | undefined;
  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, max: 100, grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } },
      x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } }
    }
  };

  ngOnInit() {
    this.loadTerrains();
  }

  loadTerrains() {
    this.terrainService.getAll().subscribe({
      next: data => this.terrains.set(data),
      error: err => console.error("Erreur lors du chargement des terrains", err)
    });
  }

  creerTerrain() {
    if (!this.newTerrain.region || !this.newTerrain.surface) return;
    
    this.isCreating = true;
    this.errorMessage.set(null);
    
    this.terrainService.create(this.newTerrain).subscribe({
      next: (created) => {
        console.log("✅ Terrain créé avec succès :", created);
        this.loadTerrains(); // Rafraichir la liste
        this.selectedTerrainId = created.id!; // Auto-sélection
        this.showNewTerrainForm = false; // Fermer le form
        this.isCreating = false;
        // Réinitialiser form
        this.newTerrain = { region: '', surface: 0, typeSol: '', latitude: 0, longitude: 0 };
      },
      error: (err) => {
        console.error("❌ Erreur lors de la création du terrain :", err);
        this.errorMessage.set("Impossible de créer le terrain (POST /api/terrain a échoué).");
        this.isCreating = false;
      }
    });
  }

  lancerSimulation() {
    if (!this.selectedTerrainId) return;
    
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.resetData();

    const id = this.selectedTerrainId;
    console.log("🚀 Lancement de l'analyse backend globale pour le terrain ID :", id);

    // 1. Calculer le score
    this.scoringService.calculerScore(id).subscribe({
      next: (scoreResult) => {
        console.log("✅ Calcul POST réussi :", scoreResult);
        this.score.set(scoreResult);
        
        // 2. Récupérer toutes les autres APIs existantes via forkJoin
        forkJoin({
          decision: this.scoringService.getDecision(id).pipe(catchError(() => of(null))),
          breakdown: this.scoringService.getBreakdown(id).pipe(catchError(() => of(null))),
          evolution: this.scoringService.getEvolution(id).pipe(catchError(() => of(null))),
          recommandations: this.scoringService.getRecommandations(id).pipe(catchError(() => of(null))),
          crop: this.scoringService.comparerScore(id, 'Blé').pipe(catchError(() => of(null))),
          ndvi: this.satelliteService.getNdvi(id).pipe(catchError(() => of(null))),
          biomasse: this.satelliteService.getBiomasse(id).pipe(catchError(() => of(null))),
          meteo: this.meteoService.getMeteo(id).pipe(catchError(() => of(null))),
          soil: this.soilService.getSoilData(id).pipe(catchError(() => of(null)))
        }).subscribe({
          next: (res) => {
            console.log("✅ Données complémentaires récupérées :", res);
            this.decision.set(res.decision);
            this.breakdown.set(res.breakdown);
            this.recommandations.set(res.recommandations);
            this.cropData.set(res.crop);
            this.satelliteNdvi.set(res.ndvi);
            this.satelliteBiomasse.set(res.biomasse);
            this.meteo.set(res.meteo);
            this.soil.set(res.soil);

            // Populer le graphe d'évolution
            if (res.evolution && res.evolution.length > 0) {
              this.chartData = {
                labels: res.evolution.map(e => e.date),
                datasets: [{
                  data: res.evolution.map(e => e.score),
                  label: 'Score',
                  borderColor: '#00ff88',
                  backgroundColor: 'rgba(0, 255, 136, 0.1)',
                  fill: true,
                  tension: 0.4
                }]
              };
            }

            // Mettre tout dans le result pour le JSON Debug
            this.result.set({
              score: scoreResult,
              ...res
            });

            this.isLoading.set(false);
          },
          error: (err) => {
            console.error("❌ Erreur lors du chargement des données annexes :", err);
            this.errorMessage.set("Certaines données n'ont pas pu être chargées correctement depuis l'API.");
            this.isLoading.set(false);
          }
        });
      },
      error: (err) => {
        console.error("❌ Erreur lors du POST /calculer :", err);
        this.errorMessage.set("Le serveur a refusé le calcul du score. L'API est injoignable ou en erreur.");
        this.isLoading.set(false);
      }
    });
  }

  private resetData() {
    this.score.set(null);
    this.decision.set(null);
    this.breakdown.set(null);
    this.recommandations.set(null);
    this.soil.set(null);
    this.satelliteNdvi.set(null);
    this.satelliteBiomasse.set(null);
    this.meteo.set(null);
    this.cropData.set(null);
    this.result.set(null);
    this.chartData = undefined;
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
    if (score >= 70) return '#10b981';
    if (score >= 40) return '#f97316';
    return '#ef4444';
  }

  getBarColor(value: number): string {
    if (value >= 70) return 'bg-emerald-500';
    if (value >= 40) return 'bg-orange-500';
    return 'bg-red-500';
  }
}
