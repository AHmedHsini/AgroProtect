import {
  Component, OnInit, inject, signal, computed,
  ChangeDetectionStrategy, PLATFORM_ID
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ScoringService } from '../../core/services/scoring.service';
import { TerrainService } from '../../core/services/terrain.service';
import { MarketService } from '../../core/services/market.service';
import { StatistiquesDTO, DecisionDTO, ScoreBreakdownDTO, RecommandationDTO } from '../../core/models/score.model';
import { CropRecommendationDTO } from '../../core/models/market.model';
import { TerrainAgricole } from '../../core/models/terrain.model';
import { LucideAngularModule, Sprout, Grid, Map as MapIcon, BarChart2, Star, Shield, User, Settings, LogOut, Menu, ChevronRight, Search, Bell, MapPin } from 'lucide-angular';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseChartDirective } from 'ng2-charts';
import { AgroMapComponent } from '../../shared/components/agro-map/agro-map.component';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

// ─── MODELS ──────────────────────────────

export type DecisionType = 'FINANCER' | 'SURVEILLER' | 'REFUSER';

export interface ScoreBreakdown {
  key:    string;
  label:  string;
  icon:   string;
  score:  number;
  trend:  number;
  detail: string;
}

export interface CropRecommendation {
  rank:        number;
  name:        string;
  icon:        string;
  matchScore:  number;
  reason:      string;
  season:      string;
}

export interface DecisionFactor {
  label:    string;
  positive: boolean;
}

export interface FinalDecision {
  type:       DecisionType;
  emoji:      string;
  confidence: number;
  summary:    string;
  factors:    DecisionFactor[];
}

export interface Terrain {
  id:       number;
  name:     string;
  location: string;
  surface:  number;
  score:    number;
  decision: DecisionType;
  date:     string;
  humidity: number;
  ndvi:     number;
  latitude: number;
  longitude: number;
  geometryJson?: string;
}

export interface KpiCard {
  label:   string;
  value:   string;
  icon:    string;
  trend:   string;
  trendUp: boolean;
  color:   string;
}

// ─── NAVIGATION ───────────────────────────────────────────────────────────────

export interface NavItem {
  id:    string;
  label: string;
  icon:  string;
  link:  string;
  badge?: string;
}

const NAV_ITEMS: NavItem[] = [
  { id:'dashboard',       label:'Dashboard',       icon:'grid',        link:'/dashboard' },
  { id:'terrains',        label:'Terrains',        icon:'map',         link:'/terrains' },
  { id:'scoring',         label:'Scoring',         icon:'bar-chart-2', link:'/scoring' },
  { id:'recommendations', label:'Recommandations', icon:'star',        link:'/recommendations' },
  { id:'decisions',       label:'Décisions',       icon:'shield',      link:'/decisions' },
  { id:'market',          label:'Marché',          icon:'pie-chart',   link:'/market-demo' },
  { id:'profile',         label:'Profil',          icon:'user',        link:'/profile' },
  { id:'settings',        label:'Paramètres',      icon:'settings',    link:'/settings' },
];

// ─── COMPONENT ────────────────────────────────────────────────────────────────

@Component({
  selector:        'app-dashboard',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule, BaseChartDirective, AgroMapComponent],
  templateUrl:     './dashboard.component.html',
  styleUrl:        './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {

  private auth       = inject(AuthService);
  private router     = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private scoringService = inject(ScoringService);
  private terrainService = inject(TerrainService);
  private marketService = inject(MarketService);

  // ── State ────────────────────────────────────────────────────────
  sidebarOpen      = signal<boolean>(true);
  notifOpen        = signal<boolean>(false);
  activeTerrain    = signal<Terrain | null>(null);
  scoreAnimated    = signal<number>(0);
  
  isLoading        = signal<boolean>(true);
  isTerrainLoading = signal<boolean>(false);
  errorMessage     = signal<string | null>(null);

  // ── Data signals ─────────────────────────────────────────────────
  user             = this.auth.user;
  kpis             = signal<KpiCard[]>([]);
  breakdown        = signal<ScoreBreakdown[]>([]);
  recommendations  = signal<CropRecommendation[]>([]);
  decision         = signal<FinalDecision | null>(null);
  terrains         = signal<Terrain[]>([]);
  topTerrains      = signal<Terrain[]>([]);
  marketInsights   = signal<CropRecommendationDTO[]>([]);
  navItems         = signal<NavItem[]>(NAV_ITEMS);

  // ── Charts ───────────────────────────────────────────────────────
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' }, border: { display: false }, ticks: { color: '#64748b' } },
      x: { grid: { display: false }, border: { display: false }, ticks: { color: '#64748b' } }
    }
  };

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { color: '#94a3b8', usePointStyle: true, padding: 20 } }
    }
  };

  distributionData = signal<ChartData<'pie'>>({
    labels: ['Faible', 'Moyen', 'Elevé'],
    datasets: [{
      data: [0, 0, 0],
      backgroundColor: ['#00ff88', '#ffcc00', '#ff4d4d'],
      borderWidth: 0
    }]
  });

  trendData = signal<ChartData<'bar'>>({
    labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin'],
    datasets: [{
      data: [65, 59, 80, 81, 56, 75],
      backgroundColor: '#00ccff',
      borderRadius: 6
    }]
  });

  // ── Computed ─────────────────────────────────────────────────────
  globalScore    = computed(() => this.activeTerrain()?.score ?? 0);
  scoreLabel     = computed(() => this.getScoreLabel(this.globalScore()));
  scoreColor     = computed(() => this.getScoreColor(this.globalScore()));
  scoreOffset    = computed(() => {
    const c = 2 * Math.PI * 80; // circumference r=80
    return c * (1 - this.scoreAnimated() / 100);
  });
  userName       = computed(() => this.user()?.name  ?? 'Utilisateur');
  userRole       = computed(() => this.user()?.role  ?? 'Analyste');
  userInitials   = computed(() => {
    const n = this.userName();
    return n.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  });
  decisionClass  = computed(() => {
    const dec = this.decision();
    if (!dec) return {};
    return {
      'decision-green':  dec.type === 'FINANCER',
      'decision-orange': dec.type === 'SURVEILLER',
      'decision-red':    dec.type === 'REFUSER',
    };
  });

  // ── Lifecycle ────────────────────────────────────────────────────
  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      stats: this.scoringService.getStatistiques().pipe(catchError(() => of(null))),
      terrainsData: this.terrainService.getAll().pipe(catchError(() => of([]))),
      top5: this.scoringService.getTopTerrains(100).pipe(catchError(() => of([]))) // On prend large pour mapper tous les terrains
    }).subscribe({
      next: (res) => {
        if (res.stats) {
          const backendKpis: KpiCard[] = [
            { label:'Score Moyen Global', value: (res.stats.scoreMoyenGlobal ?? 0).toFixed(1), icon:'📈', trend:'+1.2%', trendUp:true, color:'#00ff88' },
            { label:'Meilleure Parcelle', value: '92.4', icon:'🏆', trend:'', trendUp:true, color:'#facc15' },
            { label:'Total Terrains', value: (res.stats.totalTerrains ?? 0).toString(), icon:'🗺️', trend:'', trendUp:true, color:'#00ccff' },
            { label:'Total Analyses', value: (res.stats.totalScores ?? 0).toString(), icon:'📊', trend:'', trendUp:true, color:'#a78bfa' }
          ];
          this.kpis.set(backendKpis);

          this.distributionData.set({
            labels: ['Faible', 'Moyen', 'Elevé'],
            datasets: [{
              data: [res.stats.risqueFaible, res.stats.risqueMoyen, res.stats.risqueEleve],
              backgroundColor: ['#00ff88', '#ffcc00', '#ff4d4d'],
              borderWidth: 0
            }]
          });
        }

        if (res.terrainsData && res.terrainsData.length > 0) {
          // --- Logic for Top 5 and Mapping scores to all terrains ---
          const scoredMap = new window.Map<number, any>();
          (res.top5 || []).forEach(s => {
            if (!scoredMap.has(s.terrainAgricole.id)) {
              scoredMap.set(s.terrainAgricole.id, s);
            }
          });

          const mappedTerrains: Terrain[] = res.terrainsData.map(t => {
            const lastScore = scoredMap.get(t.id!);
            return {
              id: t.id!,
              name: `Parcelle #${t.id}`,
              location: t.region || 'Inconnue',
              surface: t.surface || 0,
              score: lastScore ? lastScore.score : 0,
              decision: (lastScore ? (lastScore.score >= 75 ? 'FINANCER' : (lastScore.score >= 50 ? 'SURVEILLER' : 'REFUSER')) : 'SURVEILLER') as DecisionType,
              date: lastScore ? new Date(lastScore.dateCalcul).toLocaleDateString() : 'Non analysé',
              humidity: 45 + Math.floor(Math.random() * 20),
              ndvi: lastScore ? (lastScore.score / 100) : 0.65,
              latitude: t.latitude,
              longitude: t.longitude,
              geometryJson: t.geometryJson
            };
          });

          this.terrains.set(mappedTerrains);
          
          const scoredList = Array.from(scoredMap.values()).map(s => ({
            id: s.terrainAgricole.id,
            name: `Parcelle #${s.terrainAgricole.id}`,
            location: s.terrainAgricole.region || 'N/A',
            surface: s.terrainAgricole.surface || 0,
            score: s.score, 
            decision: (s.score >= 75 ? 'FINANCER' : (s.score >= 50 ? 'SURVEILLER' : 'REFUSER')) as DecisionType,
            date: new Date(s.dateCalcul).toLocaleDateString(),
            humidity: 45 + Math.floor(Math.random() * 20),
            ndvi: s.score / 100,
            latitude: s.terrainAgricole.latitude,
            longitude: s.terrainAgricole.longitude,
            geometryJson: s.terrainAgricole.geometryJson
          }));

          const scoredIds = new Set(scoredList.map(s => s.id));
          const others = mappedTerrains
            .filter(t => !scoredIds.has(t.id))
            .map(t => ({ ...t, score: 0, decision: 'SURVEILLER' as DecisionType, date: 'Non analysé', humidity: 50, ndvi: 0.5 }));

          this.topTerrains.set([...scoredList, ...others].slice(0, 5));
          
          this.navItems.update(items => items.map(i => i.id === 'terrains' ? { ...i, badge: mappedTerrains.length.toString() } : i));
          
          if (!this.activeTerrain()) {
            this.selectTerrain(mappedTerrains[0]);
          }
        } else {
          this.errorMessage.set('Aucun terrain trouvé. Veuillez ajouter des terrains.');
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur globale :", err);
        this.errorMessage.set('Erreur de connexion au serveur backend.');
        this.isLoading.set(false);
      }
    });
  }

  calculerScore() {
    const terrain = this.activeTerrain();
    if (!terrain) return;

    this.isTerrainLoading.set(true);
    this.scoringService.calculerScore(terrain.id).subscribe({
      next: (res) => {
        // Rafraîchir tout le dashboard pour mettre à jour les listes et KPIs
        this.loadDashboardData();
        // Forcer la sélection pour voir le nouveau score animé
        this.selectTerrain(terrain);
      },
      error: (err) => {
        console.error("Erreur calcul :", err);
        this.isTerrainLoading.set(false);
      }
    });
  }

  // ── Methods ──────────────────────────────────────────────────────
  toggleSidebar()    { this.sidebarOpen.update(v => !v); }
  toggleNotif()      { this.notifOpen.update(v => !v); }

  selectTerrain(t: Terrain) {
    if (this.activeTerrain()?.id === t.id && this.breakdown().length > 0) return; // Évite les appels inutiles
    
    this.activeTerrain.set(t);
    this.isTerrainLoading.set(true);
    
    // Rénitialiser score pour animation
    this.scoreAnimated.set(0);

    forkJoin({
      decisionReq: this.scoringService.getDecision(t.id).pipe(catchError(() => of(null))),
      breakdownReq: this.scoringService.getBreakdown(t.id).pipe(catchError(() => of(null))),
      recsReq: this.scoringService.getRecommandations(t.id).pipe(catchError(() => of([]))),
      scoreReq: this.scoringService.getDernierScore(t.id).pipe(catchError(() => of(null)))
    }).subscribe({
      next: (res) => {
        // --- Decision ---
        if (res.decisionReq) {
          let dType: DecisionType = 'SURVEILLER';
          if (res.decisionReq.decision === 'FINANCER') dType = 'FINANCER';
          if (res.decisionReq.decision === 'REFUSER') dType = 'REFUSER';

          this.decision.set({
            type: dType,
            emoji: dType === 'FINANCER' ? '✅' : dType === 'REFUSER' ? '❌' : '⚠️',
            confidence: res.decisionReq.confiance || 80,
            summary: res.decisionReq.raisonPrincipale || 'Décision analysée par l\'IA basée sur les indicateurs.',
            factors: [
              { label: 'Score de confiance IA supérieur à 50%', positive: (res.decisionReq.confiance || 0) > 50 },
              { label: 'Analyse des données satellites', positive: true }
            ]
          });
          
          // Mettre à jour l'élément terrain listé avec la vraie décision
          this.terrains.update(ts => ts.map(terr => terr.id === t.id ? {...terr, decision: dType} : terr));
        } else {
          this.decision.set({
            type: 'SURVEILLER', emoji: '⚠️', confidence: 0, summary: 'Données de décision non disponibles.', factors: []
          });
        }

        // --- Breakdown ---
        if (res.breakdownReq) {
          const b = res.breakdownReq;
          const bd: ScoreBreakdown[] = [
            { key: 'soil',    label: 'Qualité du Sol',   icon: '🪨', score: Math.round(b.agronomique), trend: 0, detail: 'Analyse pH & MO' },
            { key: 'climate', label: 'Climatologie',     icon: '🌤️', score: Math.round(b.climatique),  trend: 0, detail: 'Pluviométrie' },
            { key: 'market',  label: 'Opportunité',      icon: '💰', score: Math.round(b.marketScore), trend: 0, detail: 'Prix local' },
            { key: 'prod',    label: 'Productivité',    icon: '⚡', score: Math.round(b.productivite),trend: 0, detail: 'Biomasse NDVI' },
          ];
          this.breakdown.set(bd);
        } else {
          this.breakdown.set([]);
        }

        // --- Market Insights ---
        this.marketService.getMarketOpportunities(t.id).subscribe({
          next: (m) => this.marketInsights.set(m.slice(0, 3)),
          error: () => this.marketInsights.set([])
        });

        // --- Recommendations ---
        if (res.recsReq && res.recsReq.length > 0) {
          const rs: CropRecommendation[] = res.recsReq.map((r, idx) => ({
            rank: idx + 1,
            name: r.type || 'Action',
            icon: '🌱',
            matchScore: 85,
            reason: r.message,
            season: r.priorite || 'N/A'
          }));
          this.recommendations.set(rs);
        } else {
          this.recommendations.set([]);
        }

        // --- Score final ---
        let finalScore = 0;
        if (res.scoreReq) {
          finalScore = res.scoreReq.score;
        } else if (res.breakdownReq) {
          finalScore = res.breakdownReq.scoreFinal;
        }
        
        this.activeTerrain.update(curr => curr ? {...curr, score: finalScore} : null);
        this.terrains.update(ts => ts.map(terr => terr.id === t.id ? {...terr, score: finalScore} : terr));
        
        if (isPlatformBrowser(this.platformId)) {
          setTimeout(() => this.scoreAnimated.set(finalScore), 200);
        }
        
        this.isTerrainLoading.set(false);
      },
      error: () => {
        this.isTerrainLoading.set(false);
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  getScoreLabel(s: number): string {
    if (s >= 80) return 'Excellent';
    if (s >= 70) return 'Bon potentiel';
    if (s >= 55) return 'Acceptable';
    if (s >= 40) return 'Risqué';
    return 'Insuffisant';
  }

  getScoreColor(s: number): string {
    if (s >= 80) return '#00ff88';
    if (s >= 65) return '#00ccff';
    if (s >= 50) return '#ffcc00';
    if (s >= 40) return '#ff8c00';
    return '#ff4d4d';
  }

  getBarColor(s: number): string { return this.getScoreColor(s); }

  getDecisionConfig(type: DecisionType) {
    const cfg = {
      FINANCER:   { color:'#00ff88', bg:'rgba(0,255,136,0.08)', border:'rgba(0,255,136,0.3)',  label:'FINANCER',   emoji:'✅' },
      SURVEILLER: { color:'#ffcc00', bg:'rgba(255,204,0,0.08)', border:'rgba(255,204,0,0.3)',  label:'SURVEILLER', emoji:'⚠️' },
      REFUSER:    { color:'#ff4d4d', bg:'rgba(255,77,77,0.08)', border:'rgba(255,77,77,0.3)',  label:'REFUSER',    emoji:'❌' },
    };
    return cfg[type] || cfg['SURVEILLER'];
  }

  trackByFn(_: number, item: any) {
    return item.id ?? item.key ?? item.rank ?? item.label;
  }
}
