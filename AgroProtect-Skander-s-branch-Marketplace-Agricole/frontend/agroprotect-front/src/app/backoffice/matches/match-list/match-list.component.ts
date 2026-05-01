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
 

  // Helpers for template
  getStatusClass(status: StatusMatch): string {
    switch (status) {
      case StatusMatch.EN_ATTENTE: return 'bg-warning text-dark';
      case StatusMatch.ACCEPTE: return 'bg-success';
      case StatusMatch.REFUSE: return 'bg-danger';
      case StatusMatch.EXPIRE: return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }

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

  formatCurrency(amount: number | null | undefined): string {
    if (amount == null) return '-';
    return new Intl.NumberFormat('fr-TN', { 
      style: 'currency', 
      currency: 'TND' 
    }).format(amount);
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
  
}