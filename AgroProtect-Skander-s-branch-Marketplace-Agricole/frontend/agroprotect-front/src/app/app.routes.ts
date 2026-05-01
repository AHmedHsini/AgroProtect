import { Routes } from '@angular/router';
import { AnnonceListComponent } from './backoffice/annonces/annonce-list/annonce-list.component';
import { AnnonceFormComponent } from './backoffice/annonces/annonce-update/annonce-update.component';
import { AnnonceCreateComponent } from './backoffice/annonces/annonce-create/annonce-create.component';
import { MatchListComponent } from './backoffice/matches/match-list/match-list.component';
import { MatchCreateComponent } from './backoffice/matches/match-create/match-create.component';
import { MatchUpdateComponent } from './backoffice/matches/match-update/match-update.component';
import { NotificationListComponent } from './backoffice/notifications/notification-list/notification-list.component';
import { DashboardComponent } from './backoffice/dashboard/dashboard.component';

export const routes: Routes = [
  {
    path: 'backoffice/annonces',
    component: AnnonceListComponent
  },
  {
    path: 'backoffice/annonces/new',
    component: AnnonceCreateComponent
  },
  {
    path: 'backoffice/annonces/edit/:id',
    component: AnnonceFormComponent
  },
  {
    path: 'backoffice/matches',
    component: MatchListComponent
  },
  {
    path: 'backoffice/matches/new',
    component: MatchCreateComponent
  },
  {
    path: 'backoffice/matches/edit/:id',
    component: MatchUpdateComponent
  },

  { path: 'backoffice/notifications', component: NotificationListComponent },
  { path: 'dashboard', component: DashboardComponent },

  
  {
    path: '',
    redirectTo: 'backoffice/annonces',
    pathMatch: 'full'
  }
  
];