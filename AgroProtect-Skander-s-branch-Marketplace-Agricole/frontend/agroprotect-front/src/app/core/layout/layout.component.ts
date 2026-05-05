import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {

  menuItems = [
    { label: 'Dashboard', path: '/backoffice/dashboard', icon: 'bi-speedometer2' },
    { label: 'Annonces', path: '/backoffice/annonces', icon: 'bi-megaphone' },
    { label: 'Funds', path: '/backoffice/matches', icon: 'bi bi-cash-stack' },
    { label: 'Notifications', path: '/backoffice/notifications', icon: 'bi-bell' }
  ];

}