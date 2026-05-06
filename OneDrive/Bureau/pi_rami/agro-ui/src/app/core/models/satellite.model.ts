export interface SatelliteIndexDTO {
  ndvi: number;
  evi: number;
  ndwi: number;
  niveauSante: string;
  interpretation: string;
  recommandation: string;
  score: number;
}

export interface SatelliteBiomasseDTO {
  biomasseEstimee: number;
  rendementEstime: number;
  commentaire: string;
  niveau: string;
  interpretation: string;
  recommandation: string;
}
