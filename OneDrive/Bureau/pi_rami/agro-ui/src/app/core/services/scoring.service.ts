import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ScoreAgricole, StatistiquesDTO, EvolutionScoreDTO, RecommandationDTO, ScoreBreakdownDTO, CropScoreComparisonDTO, DecisionDTO } from '../models/score.model';

@Injectable({
  providedIn: 'root'
})
export class ScoringService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/scoring`;

  calculerScore(terrainId: number): Observable<ScoreAgricole> {
    return this.http.post<ScoreAgricole>(`${this.apiUrl}/calculer/${terrainId}`, {});
  }

  getHistorique(terrainId: number): Observable<ScoreAgricole[]> {
    return this.http.get<ScoreAgricole[]>(`${this.apiUrl}/historique/${terrainId}`);
  }

  getDernierScore(terrainId: number): Observable<ScoreAgricole> {
    return this.http.get<ScoreAgricole>(`${this.apiUrl}/dernier/${terrainId}`);
  }

  getStatistiques(): Observable<StatistiquesDTO> {
    return this.http.get<StatistiquesDTO>(`${this.apiUrl}/statistiques`);
  }

  getEvolution(terrainId: number): Observable<EvolutionScoreDTO[]> {
    return this.http.get<EvolutionScoreDTO[]>(`${this.apiUrl}/evolution/${terrainId}`);
  }

  getRecommandations(terrainId: number): Observable<RecommandationDTO[]> {
    return this.http.get<RecommandationDTO[]>(`${this.apiUrl}/recommandations/${terrainId}`);
  }

  getBreakdown(terrainId: number): Observable<ScoreBreakdownDTO> {
    return this.http.get<ScoreBreakdownDTO>(`${this.apiUrl}/breakdown/${terrainId}`);
  }

  comparerScore(terrainId: number, crop: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/crop/${terrainId}?crop=${crop}`);
  }

  getDecision(terrainId: number): Observable<DecisionDTO> {
    return this.http.get<DecisionDTO>(`${this.apiUrl}/decision/${terrainId}`);
  }

  getTopTerrains(limit: number = 5): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-terrains?limit=${limit}`);
  }
}
