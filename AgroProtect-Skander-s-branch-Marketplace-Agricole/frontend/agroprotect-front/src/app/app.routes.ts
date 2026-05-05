import { Routes } from '@angular/router';
import { Component } from '@angular/core';
import { LayoutComponent as BackofficeLayout } from './core/layout/layout.component';
import { LayoutComponent as FrontofficeLayout } from './frontoffice/layout/layout.component';
import { AnnonceListComponent } from './backoffice/annonces/annonce-list/annonce-list.component';
import { AnnonceFormComponent } from './backoffice/annonces/annonce-update/annonce-update.component';
import { AnnonceCreateComponent } from './backoffice/annonces/annonce-create/annonce-create.component';
import { MatchListComponent } from './backoffice/matches/match-list/match-list.component';
import { MatchCreateComponent } from './backoffice/matches/match-create/match-create.component';
import { MatchUpdateComponent } from './backoffice/matches/match-update/match-update.component';
import { NotificationListComponent } from './backoffice/notifications/notification-list/notification-list.component';
import { DashboardComponent } from './backoffice/dashboard/dashboard.component';
import { MarketplaceComponent } from './frontoffice/marketplace/marketplace.component';

@Component({ 
  template: '<div style="padding:40px; text-align:center;">Page coming soon...</div>', 
  standalone: true 
})
export class PlaceholderComponent {}

export const routes: Routes = [
  // BACKOFFICE
  {
    path: 'backoffice',
    component: BackofficeLayout,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'annonces', component: AnnonceListComponent },
      { path: 'annonces/new', component: AnnonceCreateComponent },
      { path: 'annonces/edit/:id', component: AnnonceFormComponent },
      { path: 'matches', component: MatchListComponent },
      { path: 'matches/new', component: MatchCreateComponent },
      { path: 'matches/edit/:id', component: MatchUpdateComponent },
      { path: 'notifications', component: NotificationListComponent },
      { path: '', redirectTo: 'annonces', pathMatch: 'full' }
    ]
  },

    // FRONTOFFICE
    {
      path: 'frontoffice',
      component: FrontofficeLayout,
      children: [
        { path: '', component: MarketplaceComponent }, // <-- Changed this line
        { path: 'my-projects', component: PlaceholderComponent },
        { path: 'my-investments', component: PlaceholderComponent }
      ]
    },

  { path: '', redirectTo: 'frontoffice', pathMatch: 'full' }
];