export enum TypeAnnonce {
    PROJET_AGRICOLE = 'PROJET_AGRICOLE',
    EQUIPEMENT = 'EQUIPEMENT',
    EMPLOI = 'EMPLOI',
    SERVICE = 'SERVICE'
  }
  
  export enum StatusAnnonce {
    DISPONIBLE = 'DISPONIBLE',
    NON_DISPONIBLE = 'NON_DISPONIBLE',
    EN_ATTENTE = 'EN_ATTENTE'
  }
  
  export interface Annonce {
    id?: number;
    typeAnnonce: TypeAnnonce;
    titre: string;
    description?: string;
    status: StatusAnnonce;
    datePublication?: string;
    createurId: number;
    targetAmount?: number;
    location?: string;
    targetDurationMonths?: number;
    lastMilestoneNotified?: number;
    version?: number;
    lastModified?: string;
  }