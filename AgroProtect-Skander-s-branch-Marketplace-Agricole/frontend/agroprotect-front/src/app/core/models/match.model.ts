export enum StatusMatch {
    EN_ATTENTE = 'EN_ATTENTE',
    ACCEPTE = 'ACCEPTE',
    REFUSE = 'REFUSE',
    TERMINE = 'TERMINE'
  }
  
  export interface Match {
    id?: number;
    annonce?: {
      id?: number;
      titre?: string;
    };
    annonceId: number;
    investisseurId: number;
    matchDate?: string;
    status: StatusMatch;
    montantPropose?: number;
  }
  
  export interface MatchResponseDTO {
    id: number;
    annonceId: number;
    annonceTitre: string;
    investisseurId: number;
    matchDate: string;
    status: StatusMatch;
    montantPropose?: number;
  }