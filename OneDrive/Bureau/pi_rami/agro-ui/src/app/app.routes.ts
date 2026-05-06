import { Routes }    from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home').then(m => m.Home),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
  },
  {
    path: 'terrains',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/terrains/terrain-list.component').then(m => m.TerrainListComponent),
  },
  {
    path: 'terrains/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/terrains/terrain-detail.component').then(m => m.TerrainDetailComponent),
  },
  {
    path: 'scoring',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/scoring/simulation.component').then(m => m.SimulationComponent),
  },
  {
    path: 'recommendations',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/recommendations/recommendations.component').then(m => m.RecommendationsComponent),
  },
  {
    path: 'decisions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/decisions/decisions.component').then(m => m.DecisionsComponent),
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile/profile.component').then(m => m.ProfileComponent),
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/settings/settings.component').then(m => m.SettingsComponent),
  },
  {
    path: 'market-demo',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/market-example/market-example.component').then(m => m.MarketExampleComponent),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent),
  },
];
