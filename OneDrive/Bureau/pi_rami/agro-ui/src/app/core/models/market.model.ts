export interface CropRecommendationDTO {
  cropName: string;
  localNeedScore: number;
  soilCompatibilityScore: number;
  climateCompatibilityScore: number;
  finalOpportunityScore: number;
  justification: string;
}

export interface MarketCommodityDTO {
  name: string;
  production: number;
  imports: number;
  exports: number;
  importDependencyRatio: number;
}
