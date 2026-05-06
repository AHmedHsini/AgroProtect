export interface TerrainAgricole {
  id?: number;
  surface: number;
  typeSol: string;
  region: string;
  latitude: number;
  longitude: number;
  eosFieldId?: string;
  geometryJson?: string;
  agriculteurId?: number;
}
