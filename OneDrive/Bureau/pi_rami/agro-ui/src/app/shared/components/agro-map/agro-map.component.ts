import { 
  Component, Input, OnInit, OnChanges, SimpleChanges, ElementRef, ViewChild, AfterViewInit, ViewEncapsulation 
} from '@angular/core';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-agro-map',
  standalone: true,
  imports: [CommonModule],
  encapsulation: ViewEncapsulation.None,
  template: `<div #mapContainer class="map-frame"></div>`,
  styles: [`
    .map-frame {
      height: 100%;
      min-height: 400px;
      width: 100%;
      border-radius: 1.5rem;
      overflow: hidden;
      border: 1px solid rgba(255, 255, 255, 0.1);
      z-index: 1;
      background: #0f172a;
    }

    /* Custom Leaflet Styling */
    .leaflet-container {
      background: #0f172a !important;
    }
    
    .agro-popup .leaflet-popup-content-wrapper {
      background: rgba(15, 23, 42, 0.85) !important;
      backdrop-filter: blur(16px) !important;
      border: 1px solid rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      border-radius: 1.25rem !important;
      padding: 0 !important;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 10px 10px -5px rgba(0, 0, 0, 0.4) !important;
    }
    
    .agro-popup .leaflet-popup-tip {
      background: rgba(15, 23, 42, 0.85) !important;
    }
    
    .agro-popup .leaflet-popup-content {
      margin: 0 !important;
      width: 220px !important;
    }

    .leaflet-control-zoom {
      border: none !important;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;
    }
    
    .leaflet-control-zoom a {
      background: rgba(15, 23, 42, 0.9) !important;
      color: white !important;
      border: 1px solid rgba(255, 255, 255, 0.1) !important;
      backdrop-filter: blur(4px);
    }

    .leaflet-control-zoom a:hover {
      background: #1e293b !important;
      color: #00ff88 !important;
    }

    /* Custom Marker Pin */
    .marker-pin {
      width: 28px;
      height: 28px;
      border-radius: 50% 50% 50% 0;
      position: absolute;
      transform: rotate(-45deg);
      left: 50%;
      top: 50%;
      margin: -14px 0 0 -14px;
      border: 2px solid rgba(255,255,255,0.9);
      box-shadow: 0 8px 16px rgba(0,0,0,0.6);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .marker-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: white;
      position: absolute;
      top: 50%;
      left: 50%;
      margin: -4px 0 0 -4px;
      z-index: 10;
    }

    /* Pulse Effect */
    .marker-pin::after {
      content: '';
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50% 50% 50% 0;
      background: inherit;
      animation: pulse 2s ease-out infinite;
      opacity: 0.5;
      z-index: -1;
    }

    @keyframes pulse {
      0% { transform: scale(1); opacity: 0.6; }
      100% { transform: scale(3); opacity: 0; }
    }

    /* Scanning Effect */
    .map-frame::after {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: linear-gradient(to bottom, transparent 50%, rgba(0, 255, 136, 0.03) 50%);
      background-size: 100% 4px;
      pointer-events: none;
      z-index: 10;
      opacity: 0.2;
    }

    .agro-popup-container {
      animation: popup-slide 0.4s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes popup-slide {
      0% { transform: translateY(15px) scale(0.95); opacity: 0; }
      100% { transform: translateY(0) scale(1); opacity: 1; }
    }
  `]
})
export class AgroMapComponent implements OnChanges, AfterViewInit {
  @ViewChild('mapContainer') mapContainer!: ElementRef;
  @Input() terrains: any[] = []; 
  @Input() activeTerrainId?: number;

  private map?: L.Map;
  private geoJsonLayer?: L.GeoJSON;
  private markerLayer = L.layerGroup();
  private isInitialLoad = true;

  ngAfterViewInit() {
    setTimeout(() => {
      this.initMap();
      this.updateLayers();
    }, 150);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.map) {
      if (changes['terrains']) {
        this.updateLayers();
      }
      if (changes['activeTerrainId'] && !changes['activeTerrainId'].isFirstChange()) {
        this.focusOnTerrain(this.activeTerrainId);
      }
    }
  }

  private initMap() {
    if (this.map) return;

    this.map = L.map(this.mapContainer.nativeElement, {
      center: [36.8065, 10.1815], 
      zoom: 6,
      zoomControl: false,
      attributionControl: false,
      preferCanvas: true
    });

    // Satellite Layer (Esri World Imagery)
    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
      maxZoom: 19
    }).addTo(this.map);

    this.markerLayer.addTo(this.map);
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    L.control.attribution({ position: 'bottomleft', prefix: 'AgroProtect AI' }).addTo(this.map);
  }

  private updateLayers() {
    if (!this.map || !this.terrains.length) return;
    
    // Clear existing layers
    this.markerLayer.clearLayers();
    if (this.geoJsonLayer) {
      this.map.removeLayer(this.geoJsonLayer);
    }

    const geojsons: any[] = [];

    this.terrains.forEach(t => {
      const terrainColor = this.getColor(t.score);
      
      // 1. Add Custom Marker
      if (t.latitude && t.longitude) {
        const customIcon = L.divIcon({
          className: 'custom-div-icon',
          html: `<div style="background-color: ${terrainColor};" class="marker-pin"></div><i class="marker-dot"></i>`,
          iconSize: [30, 42],
          iconAnchor: [15, 42]
        });

        const marker = L.marker([t.latitude, t.longitude], { icon: customIcon })
          .bindPopup(this.getPopupContent(t), { className: 'agro-popup' });
        
        this.markerLayer.addLayer(marker);
      }

      // 2. Prepare Polygon data
      if (t.geometryJson) {
        try {
          const geom = JSON.parse(t.geometryJson);
          geom.properties = { ...t, name: t.name || `Parcelle #${t.id}` };
          geojsons.push(geom);
        } catch (e) {
          console.error("Invalid GeoJSON for terrain", t.id);
        }
      } else if (t.latitude && t.longitude) {
         // Create mock polygon for visualization if only coordinates exist
         const size = 0.0015;
         const mockGeoJson = {
           type: "Feature",
           properties: { ...t, name: t.name || `Parcelle #${t.id}` },
           geometry: {
             type: "Polygon",
             coordinates: [[
               [t.longitude - size, t.latitude - size],
               [t.longitude + size, t.latitude - size],
               [t.longitude + size, t.latitude + size],
               [t.longitude - size, t.latitude + size],
               [t.longitude - size, t.latitude - size]
             ]]
           }
         };
         geojsons.push(mockGeoJson);
      }
    });

    // 3. Add GeoJSON Polygons
    if (geojsons.length > 0) {
      this.geoJsonLayer = L.geoJSON(geojsons as any, {
        style: (feature) => ({
          fillColor: this.getColor(feature?.properties.score || 0),
          weight: 2,
          opacity: 0.9,
          color: 'white',
          fillOpacity: 0.35
        }),
        onEachFeature: (feature, layer) => {
          layer.bindPopup(this.getPopupContent(feature.properties), { className: 'agro-popup' });
        }
      }).addTo(this.map);

      // 4. Auto-zoom Logic
      if (this.isInitialLoad) {
        const bounds = this.geoJsonLayer.getBounds();
        if (bounds.isValid()) {
          this.map.fitBounds(bounds, { padding: [50, 50], animate: true, duration: 1.5 });
          this.isInitialLoad = false;
        }
      } else if (this.activeTerrainId) {
        this.focusOnTerrain(this.activeTerrainId);
      }
    }
  }

  private getPopupContent(p: any): string {
    const color = this.getColor(p.score);
    const scoreLabel = p.score >= 75 ? 'Excellent' : (p.score >= 50 ? 'Modéré' : 'Risque');
    
    return `
      <div class="agro-popup-container overflow-hidden">
        <div class="h-1" style="background: ${color}"></div>
        <div class="p-4">
          <div class="flex items-center justify-between mb-4">
             <div>
               <h4 class="font-black text-white text-sm m-0 leading-tight">${p.name}</h4>
               <p class="text-[10px] text-gray-400 mt-0.5 flex items-center gap-1">
                 <svg class="w-2.5 h-2.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path></svg>
                 ${p.location || 'Région N/A'}
               </p>
             </div>
             <div class="px-2 py-1 rounded bg-white/5 border border-white/10 text-[9px] font-bold text-gray-300">
               ${p.surface} ha
             </div>
          </div>
          
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div class="bg-white/5 p-2 rounded-lg border border-white/5">
              <p class="text-[8px] uppercase tracking-widest text-gray-500 font-bold mb-1">Score IA</p>
              <p class="text-sm font-black" style="color: ${color}">${p.score.toFixed(1)}%</p>
            </div>
            <div class="bg-white/5 p-2 rounded-lg border border-white/5">
              <p class="text-[8px] uppercase tracking-widest text-gray-500 font-bold mb-1">Santé NDVI</p>
              <p class="text-sm font-black text-blue-400">${(p.ndvi || 0.65).toFixed(2)}</p>
            </div>
          </div>

          <div class="space-y-3">
            <div class="space-y-1">
              <div class="flex items-center justify-between text-[8px] font-bold uppercase tracking-widest text-gray-400">
                <span>Indicateur de Vitalité</span>
                <span style="color: ${color}">${scoreLabel}</span>
              </div>
              <div class="h-1 bg-white/10 rounded-full overflow-hidden">
                <div class="h-full transition-all duration-1000 shadow-[0_0_8px] shadow-current" 
                     style="width: ${p.score}%; color: ${color}; background: currentColor"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-white/5 px-4 py-2 border-t border-white/5">
           <p class="text-[9px] text-gray-500 italic text-center">Données Sentinel-2 actualisées</p>
        </div>
      </div>
    `;
  }

  private focusOnTerrain(id?: number) {
    if (!id || !this.geoJsonLayer || !this.map) return;
    
    this.geoJsonLayer.eachLayer((layer: any) => {
      if (layer.feature && layer.feature.properties && layer.feature.properties.id === id) {
        this.map?.flyToBounds(layer.getBounds(), { 
          padding: [120, 120], 
          maxZoom: 17, 
          duration: 1.2,
          easeLinearity: 0.25
        });
        setTimeout(() => layer.openPopup(), 1200);
      }
    });
  }

  private getColor(s: number): string {
    if (s >= 75) return '#00ff88'; 
    if (s >= 50) return '#ffcc00'; 
    return '#ff4d4d';             
  }
}
