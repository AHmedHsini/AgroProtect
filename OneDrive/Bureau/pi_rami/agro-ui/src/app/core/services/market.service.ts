import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CropRecommendationDTO, MarketCommodityDTO } from '../models/market.model';

@Injectable({
  providedIn: 'root'
})
export class MarketService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/market/local`;

  /**
   * Récupère les opportunités de marché pour un terrain spécifique.
   * Retourne directement le tableau JSON.
   */
  getMarketOpportunities(terrainId: number): Observable<CropRecommendationDTO[]> {
    return this.http.get<CropRecommendationDTO[]>(`${this.baseUrl}/opportunities/${terrainId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Récupère les dépendances du marché local.
   * Retourne directement le tableau JSON.
   */
  getMarketDependencies(): Observable<MarketCommodityDTO[]> {
    return this.http.get<MarketCommodityDTO[]>(`${this.baseUrl}/dependency`)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue lors de la communication avec le serveur.';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client : ${error.error.message}`;
    } else {
      // Pour les erreurs serveur, on essaie de récupérer le message du backend s'il existe
      const backendMessage = error.error?.message || error.message;
      errorMessage = `Erreur serveur (${error.status}) : ${backendMessage}`;
    }
    
    console.error('MarketService Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
