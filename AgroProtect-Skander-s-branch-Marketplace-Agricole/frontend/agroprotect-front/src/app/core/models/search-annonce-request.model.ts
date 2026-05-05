export interface SearchAnnonceRequest {
    search?: string;
    type?: string;
    status?: string;
    location?: string;
    minAmount?: number;
    maxAmount?: number;
    page: number;
    size: number;
    sortBy: string;
    sortDesc: boolean;
  }