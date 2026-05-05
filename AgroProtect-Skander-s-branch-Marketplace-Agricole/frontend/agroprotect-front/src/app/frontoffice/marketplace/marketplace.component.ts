import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnnonceService, Attachment } from '../../core/service/annonce.service';
import { AnnonceItem, PaginationInfo } from '../../core/models/annonce-search-response.model';
import { SearchAnnonceRequest } from '../../core/models/search-annonce-request.model';
import { environment } from '../../../environments/environment';


@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss'
})
export class MarketplaceComponent implements OnInit {

  annonces: AnnonceItem[] = [];
  pagination: PaginationInfo | null = null;
  imageUrls: { [key: number]: string } = {};
  loading = false;
  error: string | null = null;

  // Filters
  searchTerm = '';
  selectedType = '';
  selectedStatus = '';

  // Pagination state
  currentPage = 0;
  pageSize = 9; // 3x3 grid

  constructor(
    private annonceService: AnnonceService,
    private cdr: ChangeDetectorRef // <-- ADD THIS
  ) {}

  ngOnInit(): void {
    this.loadAnnonces();
  }

  loadAnnonces(): void {
    this.loading = true;
    this.error = null;

    const request: SearchAnnonceRequest = {
      search: this.searchTerm || undefined,
      type: this.selectedType || undefined,
      status: this.selectedStatus || undefined,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: 'datePublication',
      sortDesc: true
    };

    this.annonceService.searchAnnonces(request).subscribe({
      next: (response) => {
        this.annonces = response.content;
        this.pagination = response.pagination;
        this.loading = false;
        this.loadProjectImages();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des projets.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  loadProjectImages(): void {
    let requestsCompleted = 0;

    this.annonces.forEach(item => {
      this.annonceService.getAttachments(item.id).subscribe({
        next: (attachments: Attachment[]) => {
          const imageAtt = attachments.find(a => a.category?.toUpperCase() === 'IMAGE') ||
                                 attachments.find(a => {
                                   const ext = a.fileName?.split('.').pop()?.toLowerCase();
                                   return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext || '');
                                 });
          
          if (imageAtt) {
            this.imageUrls[item.id] = `${environment.apiUrl}/uploads/${imageAtt.filePath}`;
          }
          
          requestsCompleted++;
          if (requestsCompleted === this.annonces.length) {
            this.cdr.detectChanges(); // FORCES ANGULAR TO RE-RENDER
          }
        },
        error: () => {
          requestsCompleted++;
          if (requestsCompleted === this.annonces.length) {
            this.cdr.detectChanges();
          }
        }
      });
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadAnnonces();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < (this.pagination?.totalPages || 0)) {
      this.currentPage = page;
      this.loadAnnonces();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  formatType(type: string): string {
    const map: Record<string, string> = {
      PROJET_AGRICOLE: 'Projet Agricole',
      EQUIPEMENT: 'Équipement',
      EMPLOI: 'Emploi',
      SERVICE: 'Service'
    };
    return map[type] || type;
  }

  formatStatus(status: string): string {
    const map: Record<string, string> = {
      DISPONIBLE: 'Disponible',
      NON_DISPONIBLE: 'Fermé',
      EN_ATTENTE: 'En attente'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      DISPONIBLE: 'status-disponible',
      NON_DISPONIBLE: 'status-ferme',
      EN_ATTENTE: 'status-attente'
    };
    return map[status] || '';
  }

  getTypeClass(type: string): string {
    const map: Record<string, string> = {
      PROJET_AGRICOLE: 'type-projet',
      EQUIPEMENT: 'type-equipement',
      EMPLOI: 'type-emploi',
      SERVICE: 'type-service'
    };
    return map[type] || '';
  }

  formatCurrency(amount: number): string {
    return amount.toLocaleString('fr-TN') + ' TND';
  }

  truncateText(text: string, limit: number = 80): string {
    if (!text) return '';
    return text.length > limit ? text.substring(0, limit) + '...' : text;
  }

  roundPercentage(num: number): number {
    return Math.round(num);
  }
}