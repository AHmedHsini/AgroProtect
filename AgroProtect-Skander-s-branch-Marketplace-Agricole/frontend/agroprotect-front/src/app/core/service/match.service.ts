import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Match, MatchResponseDTO, StatusMatch } from '../models/match.model';

@Injectable({
  providedIn: 'root'
})
export class MatchService {

  private readonly url = `${environment.apiUrl}/Match`;

  constructor(private http: HttpClient) {}

  // CRUD
  getAll(): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/getAll`);
  }

  getById(id: number): Observable<Match> {
    return this.http.get<Match>(`${this.url}/getById/${id}`);
  }

  create(match: Match): Observable<Match> {
    return this.http.post<Match>(`${this.url}/addMatch`, match);
  }

  update(id: number, match: Match): Observable<Match> {
    return this.http.put<Match>(`${this.url}/updateMatch/${id}`, match);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/deleteMatch/${id}`);
  }

  // Filters
  getByAnnonce(annonceId: number): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/getByAnnonce/${annonceId}`);
  }

  getByInvestisseur(investisseurId: number): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/getByInvestisseur/${investisseurId}`);
  }

  getByStatus(status: StatusMatch): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/getByStatus/${status}`);
  }

  // Status actions
  updateStatus(id: number, status: StatusMatch): Observable<Match> {
    return this.http.put<Match>(`${this.url}/updateStatus/${id}/${status}`, {});
  }

  acceptMatch(id: number): Observable<MatchResponseDTO> {
    return this.http.put<MatchResponseDTO>(`${this.url}/acceptMatch/${id}`, {});
  }

  // Maintenance
  expireOldMatches(): Observable<string> {
    return this.http.put<string>(`${this.url}/expireOldMatches`, {});
  }

  expireMatchesOlderThan(days: number): Observable<void> {
    return this.http.put<void>(`${this.url}/expireMatchesOlderThan/${days}`, {});
  }
}