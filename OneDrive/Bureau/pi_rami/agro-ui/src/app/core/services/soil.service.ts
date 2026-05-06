import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SoilData } from '../models/soil.model';

@Injectable({
  providedIn: 'root'
})
export class SoilService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/soil`;

  getSoilData(terrainId: number): Observable<SoilData> {
    return this.http.get<SoilData>(`${this.apiUrl}/terrain/${terrainId}`);
  }
}
