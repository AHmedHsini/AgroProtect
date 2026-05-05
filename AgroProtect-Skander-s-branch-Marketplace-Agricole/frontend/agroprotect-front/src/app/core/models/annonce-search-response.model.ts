export interface AnnonceSearchResponse {
    content: AnnonceItem[];
    pagination: PaginationInfo;
  }
  
  export interface AnnonceItem {
    id: number;
    typeAnnonce: string;
    titre: string;
    description: string;
    status: string;
    datePublication: string;
    createurId: number;
    targetAmount: number;
    lastMilestoneNotified: number;
    location: string;
    targetDurationMonths: number;
    lastModified: string;
    fundedAmount: number;
    progressPercentage: number;
    totalInvestors: number;
  }
  
  export interface PaginationInfo {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
  }