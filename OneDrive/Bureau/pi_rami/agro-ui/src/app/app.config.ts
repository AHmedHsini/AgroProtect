import { ApplicationConfig }                       from '@angular/core';
import { provideRouter, withComponentInputBinding, withPreloading, PreloadAllModules } from '@angular/router';
import { provideHttpClient, withInterceptors }      from '@angular/common/http';
import { provideClientHydration }                   from '@angular/platform-browser';
import { routes }                                   from './app.routes';
import { authInterceptor }                          from './core/interceptors/auth.interceptor';
import { provideCharts, withDefaultRegisterables }  from 'ng2-charts';

// Lucide — icônes globales
import { LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import {
  Zap, Leaf, BarChart2, CloudRain,
  Satellite, TrendingUp, Shield,
  ChevronRight, Star, Eye, EyeOff,
  Bell, Settings, LogOut, LayoutDashboard,
  Menu, User, CheckCircle, AlertTriangle,
  XCircle, RefreshCw, ArrowRight, ArrowLeft, CheckCircle2, Cpu, Cloud,
  Layout, BarChart3, CloudSun, Sprout, Search, Info, LineChart,
  Check, MapPin, Grid, Map,
  Loader, ShieldCheck, PieChart, Mountain, Sun, Droplets, Wind, Lightbulb, Activity, AlertCircle
} from 'lucide-angular';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding(), withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideClientHydration(),
    provideCharts(withDefaultRegisterables()),

    // ─── Toutes les icônes disponibles globalement ───────────
    {
      provide:  LUCIDE_ICONS,
      multi:    true,
      useValue: new LucideIconProvider({
        Zap, Leaf, BarChart2, CloudRain,
        Satellite, TrendingUp, Shield,
        ChevronRight, Star, Eye, EyeOff,
        Bell, Settings, LogOut, LayoutDashboard,
        Menu, User, CheckCircle, AlertTriangle,
        XCircle, RefreshCw, ArrowRight, ArrowLeft, CheckCircle2, Cpu, Cloud,
        Layout, BarChart3, CloudSun, Sprout, Search, Info, LineChart,
        Check, MapPin, Grid, Map,
        Loader, ShieldCheck, PieChart, Mountain, Sun, Droplets, Wind, Lightbulb, Activity, AlertCircle
      })
    },
  ],
};