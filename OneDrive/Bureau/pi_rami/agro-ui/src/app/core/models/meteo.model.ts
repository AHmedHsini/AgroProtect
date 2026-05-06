export interface MeteoDTO {
  ville: string;
  temperature: number;
  temperatureRessentie: number;
  humidity: number;
  windSpeed: number;
  cloudiness: number;
  description: string;
  resume: string;
}

export interface MeteoHistoriqueDTO {
  annee: number;
  temperatureMoyenne: number;
  pluieTotale: number;
}
