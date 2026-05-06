export interface ScoreAgricole {
  id?: number;
  terrainId?: number;
  dateCalcul?: string;
  score: number;
  niveau?: string;
  terrainAgricole?: any;
}

export interface StatistiquesDTO {
  totalTerrains: number;
  totalScores: number;
  scoreMoyenGlobal: number;
  risqueFaible: number;
  risqueMoyen: number;
  risqueEleve: number;
  repartitionRisque: { [key: string]: number };
  evolutionMoyenne: { [key: string]: number };
}

export interface EvolutionScoreDTO {
  date: string;
  score: number;
  risque: string;
}

export interface RecommandationDTO {
  type: string;
  message: string;
  priorite: string;
}

export interface ScoreBreakdownDTO {
  agronomique: number;
  climatique: number;
  productivite: number;
  stabilite: number;
  marketScore: number;
  scoreFinal: number;
  [key: string]: any;
}

export interface DecisionDTO {
  decision: string;
  confiance: number;
  raisonPrincipale: string;
}

export interface CropScoreComparisonDTO {
  terrainId: number;
  culture: string;
  scoreMatch: number;
  recommandationsSpecifiques: string[];
}
