import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatchService } from '../../../core/service/match.service';
import { Match, StatusMatch } from '../../../core/models/match.model';


@Component({
  selector: 'app-match-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './match-list.component.html',
  styleUrls: ['./match-list.component.scss']
})
export class MatchListComponent implements OnInit {

  matches: Match[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private matchService: MatchService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMatches();
  }

  loadMatches(): void {
    this.loading = true;
    this.error = null;

    this.matchService.getAll().subscribe({
      next: (data: Match[]) => {
        this.matches = data;
        this.loading = false;
        console.log('Loaded matches:', data);
      },
      error: (err: Error) => {
        this.error = 'Failed to load matches. Is Spring Boot running on port 8083?';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  // Action handlers
 

  

  // In match-list.component.ts
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
  acceptMatch(m: Match): void {
    if (!confirm(`Êtes-vous sûr de vouloir accepter le financement #${m.id} ?\nCela va déclencher les notifications de milestone.`)) {
      return;
    }
  
    this.matchService.acceptMatch(m.id!).subscribe({
      next: () => {
        // Reload the list to show the new status (ACCEPTE) and hide the accept button
        this.loadMatches(); 
      },
      error: (err: any) => {
        // Show backend error if status was already changed or annonce is closed
        alert('Échec de l\'acceptation: ' + (err.error?.message || err.message));
      }
    });
  }

  
// Edit navigation
edit(match: Match): void {
    this.router.navigate(['/backoffice/matches/edit', match.id]);
  }
  
  // Delete with confirmation
  delete(match: Match): void {
    if (!confirm(`Delete match #${match.id}?`)) return;
    
    this.matchService.delete(match.id!).subscribe({
      next: () => this.loadMatches(),
      error: (err: Error) => alert('Delete failed: ' + err.message)
    });
}
getStatusClass(status: StatusMatch): string {
  const map: Record<string, string> = {
    EN_ATTENTE: 'status-en-attente',
    ACCEPTE: 'status-accepte',
    REFUSE: 'status-refuse',
    TERMINE: 'status-termine'
  };
  return map[status] || '';
}

formatStatus(status: StatusMatch): string {
  const map: Record<string, string> = {
    EN_ATTENTE: 'En attente',
    ACCEPTE: 'Accepté',
    REFUSE: 'Refusé',
    TERMINE: 'Terminé'
  };
  return map[status] || status;
}

formatCurrency(amount: number | undefined): string {
  if (!amount) return '— TND';
  return amount.toLocaleString('fr-TN', { minimumFractionDigits: 0, maximumFractionDigits: 0 }) + ' TND';
}
  
}