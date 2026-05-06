import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MeteoDTO, MeteoHistoriqueDTO } from '../models/meteo.model';

@Injectable({
  providedIn: 'root'
})
export class MeteoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/meteo`;

  getMeteo(terrainId: number): Observable<MeteoDTO> {
    return this.http.get<MeteoDTO>(`${this.apiUrl}/${terrainId}`);
  }

  getHistorique(terrainId: number): Observable<MeteoHistoriqueDTO[]> {
    return this.http.get<MeteoHistoriqueDTO[]>(`${this.apiUrl}/historique/${terrainId}`);
  }
}
