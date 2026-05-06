import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SatelliteIndexDTO, SatelliteBiomasseDTO } from '../models/satellite.model';
import { EvolutionScoreDTO } from '../models/score.model';

@Injectable({
  providedIn: 'root'
})
export class SatelliteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/satellite`;

  getNdvi(terrainId: number): Observable<SatelliteIndexDTO> {
    return this.http.get<SatelliteIndexDTO>(`${this.apiUrl}/ndvi/${terrainId}`);
  }

  getEvolution(terrainId: number): Observable<EvolutionScoreDTO[]> {
    return this.http.get<EvolutionScoreDTO[]>(`${this.apiUrl}/evolution/${terrainId}`);
  }

  getBiomasse(terrainId: number): Observable<SatelliteBiomasseDTO> {
    return this.http.get<SatelliteBiomasseDTO>(`${this.apiUrl}/biomasse/${terrainId}`);
  }
}
