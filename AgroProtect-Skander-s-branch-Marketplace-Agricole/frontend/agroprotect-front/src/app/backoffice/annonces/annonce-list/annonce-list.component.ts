import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AnnonceService } from '../../../core/service/annonce.service';
import { Annonce } from '../../../core/models/annonce.model';

@Component({
  selector: 'app-annonce-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './annonce-list.component.html',
  styleUrls: ['./annonce-list.component.scss']
})
export class AnnonceListComponent implements OnInit {
  
  annonces: Annonce[] = [];
  loading = false;
  error: string | null = null;

  constructor(private annonceService: AnnonceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAnnonces();
  }

  loadAnnonces(): void {
    this.loading = true;
    this.error = null;
    
    this.annonceService.getAll().subscribe({
      next: (data: Annonce[]) => {
        this.annonces = data;
        this.loading = false;
        console.log('Loaded annonces:', data);
      },
      error: (err: Error) => {
        this.error = 'Failed to load annonces. Is Spring Boot running on port 8083?';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }
  formatDate(date: string | Date | undefined): string {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: 'short', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  edit(annonce: Annonce): void {
    this.router.navigate(['/backoffice/annonces/edit', annonce.id]);
  }
  

  delete(annonce: Annonce): void {
    if (!confirm(`Are you sure you want to delete "${annonce.titre}"?`)) {
      return;
    }
    
    this.annonceService.delete(annonce.id!).subscribe({
      next: () => {
        this.loadAnnonces(); // Refresh the list
      },
      error: (err: Error) => {
        alert('Delete failed: ' + err.message);
      }
    });
  }
}