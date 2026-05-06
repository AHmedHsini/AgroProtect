import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TerrainAgricole } from '../models/terrain.model';

@Injectable({
  providedIn: 'root'
})
export class TerrainService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/terrain`;

  getAll(): Observable<TerrainAgricole[]> {
    return this.http.get<TerrainAgricole[]>(this.apiUrl);
  }

  getById(id: number): Observable<TerrainAgricole> {
    return this.http.get<TerrainAgricole>(`${this.apiUrl}/${id}`);
  }

  create(terrain: TerrainAgricole): Observable<TerrainAgricole> {
    return this.http.post<TerrainAgricole>(this.apiUrl, terrain);
  }

  update(id: number, terrain: TerrainAgricole): Observable<TerrainAgricole> {
    return this.http.put<TerrainAgricole>(`${this.apiUrl}/${id}`, terrain);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
