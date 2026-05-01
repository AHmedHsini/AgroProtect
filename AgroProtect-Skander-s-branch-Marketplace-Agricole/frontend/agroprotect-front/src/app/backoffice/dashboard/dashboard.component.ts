import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AnnonceService } from '../../core/service/annonce.service';
import { Annonce, StatusAnnonce, TypeAnnonce } from '../../core/models/annonce.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  
  stats = {
    total: 0,
    enAttente: 0,
    disponible: 0,
    nonDisponible: 0,
    projets: 0,
    equipements: 0
  };

  recentAnnonces: Annonce[] = [];
  loading = true;

  statusAnnonce = StatusAnnonce;
  typeAnnonce = TypeAnnonce;

  constructor(private annonceService: AnnonceService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.annonceService.getAll().subscribe({
      next: (data: Annonce[]) => {
        this.stats.total = data.length;
        this.stats.enAttente = data.filter(a => a.status === StatusAnnonce.EN_ATTENTE).length;
        this.stats.disponible = data.filter(a => a.status === StatusAnnonce.DISPONIBLE).length;
        this.stats.nonDisponible = data.filter(a => a.status === StatusAnnonce.NON_DISPONIBLE).length;
        this.stats.projets = data.filter(a => a.typeAnnonce === TypeAnnonce.PROJET_AGRICOLE).length;
        this.stats.equipements = data.filter(a => a.typeAnnonce === TypeAnnonce.EQUIPEMENT).length;
        
        this.recentAnnonces = data.slice(0, 5);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getStatusClass(status: StatusAnnonce): string {
    switch (status) {
      case StatusAnnonce.EN_ATTENTE: return 'badge-warning';
      case StatusAnnonce.DISPONIBLE: return 'badge-success';
      case StatusAnnonce.NON_DISPONIBLE: return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
}