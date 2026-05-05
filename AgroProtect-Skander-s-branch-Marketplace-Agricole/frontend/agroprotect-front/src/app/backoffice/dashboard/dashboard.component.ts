import { Component, OnInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';


import { AnnonceService } from '../../core/service/annonce.service';
import { MatchService } from '../../core/service/match.service';
import { NotificationService } from '../../core/service/notification.service';
import { Annonce, StatusAnnonce } from '../../core/models/annonce.model';
import { Match, StatusMatch } from '../../core/models/match.model';


Chart.register(...registerables);
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  @ViewChild('annonceChart') annonceChartRef!: ElementRef;
  @ViewChild('matchChart') matchChartRef!: ElementRef;
  @ViewChild('notifChart') notifChartRef!: ElementRef;
  @ViewChild('fundChart') fundChartRef!: ElementRef;
  @ViewChild('fundingStatusChart') fundingStatusChartRef!: ElementRef; // <-- ADD THIS

  private annonceChart?: Chart;
  private matchChart?: Chart;
  private notifChart?: Chart;
  private fundChart?: Chart;
  private fundingStatusChart?: Chart; // <-- ADD THIS

  annonceData: number[] = [0, 0, 0];
  matchData: number[] = [0, 0, 0, 0];
  notificationData: number[] = [0, 0, 0, 0];
  fundingStatusData: number[] = [0, 0, 0]; // <-- ADD THIS

  totalTargetAmount = 0;
  totalFundedAmount = 0;
  loading = true;

  // ADD THESE TWO RAW ARRAYS TO STORE DATA FOR CROSS-REFERENCING
  rawAnnonces: Annonce[] = [];
  rawMatches: Match[] = [];

  get totalAnnonces(): number { return this.annonceData.reduce((a, b) => a + b, 0); }
  get totalMatches(): number { return this.matchData.reduce((a, b) => a + b, 0); }
  get totalNotifications(): number { return this.notificationData.reduce((a, b) => a + b, 0); }
  get totalFundingStatus(): number { return this.fundingStatusData.reduce((a, b) => a + b, 0); }

  constructor(
    private annonceService: AnnonceService,
    private matchService: MatchService,
    private notifService: NotificationService,
    private cdr: ChangeDetectorRef // <-- ADD THIS
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    let requestsCompleted = 0;

    const checkDone = () => {
      requestsCompleted++;
      if (requestsCompleted === 3) {
        this.loading = false;
        this.cdr.detectChanges(); // Forces Angular to render the canvases immediately
        setTimeout(() => this.createCharts(), 50); // Now it's safe to draw
      }
    };

    this.annonceService.getAll().subscribe({
      next: (data: Annonce[]) => { this.processAnnonces(data); checkDone(); },
      error: () => checkDone()
    });

    this.matchService.getAll().subscribe({
      next: (data: Match[]) => { this.processMatches(data); checkDone(); },
      error: () => checkDone()
    });

    this.notifService.getAll().subscribe({
      next: (data: any) => { this.processNotifications(data); checkDone(); },
      error: () => checkDone()
    });
  }

  private processAnnonces(annonces: Annonce[]): void {
    this.rawAnnonces = annonces; // <-- THIS IS THE LINE THAT WAS MISSING
    let disponible = 0, ferme = 0, attente = 0;
    annonces.forEach(a => {
      if (a.status === StatusAnnonce.DISPONIBLE) disponible++;
      else if (a.status === StatusAnnonce.NON_DISPONIBLE) ferme++;
      else if (a.status === StatusAnnonce.EN_ATTENTE) attente++;
      this.totalTargetAmount += (a.targetAmount || 0);
    });
    this.annonceData = [disponible, ferme, attente];
  }

  private processMatches(matches: Match[]): void {
    console.log('RAW MATCHES STRING:', JSON.stringify(matches, null, 2)); // <-- ADD THIS
    this.rawMatches = matches;
    // ... // <-- ADD THIS LINE
    let attente = 0, accepte = 0, refuse = 0, termine = 0;
    matches.forEach(m => {
      if (m.status === StatusMatch.ACCEPTE) {
        accepte++;
        this.totalFundedAmount += (m.montantPropose || 0);
      }
      else if (m.status === StatusMatch.EN_ATTENTE) attente++;
      else if (m.status === StatusMatch.REFUSE) refuse++;
      else if (m.status === StatusMatch.TERMINE) termine++;
    });
    this.matchData = [attente, accepte, refuse, termine];
  }

  private processNotifications(notifs: any[]): void {
    let envoye = 0, lu = 0, nonLu = 0, erreur = 0;
    notifs.forEach(n => {
      if (n.status === 'ENVOYE') envoye++;
      else if (n.status === 'LU') lu++;
      else if (n.status === 'NON_LU') nonLu++;
      else if (n.status === 'ERREUR') erreur++;
    });
    this.notificationData = [envoye, lu, nonLu, erreur];
  }

  private createCharts(): void {
    this.processFundingStatus(); // <-- ADD THIS LINE
    this.createAnnonceChart();
    this.createMatchChart();
    this.createNotifChart();
    this.createFundingStatusChart(); // <-- ADD THIS LINE
    this.createFundChart();
  }

  private createAnnonceChart(): void {
    if (this.annonceChart) this.annonceChart.destroy();
    this.annonceChart = new Chart(this.annonceChartRef.nativeElement.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: ['Disponible', 'Fermé', 'En Attente'],
        datasets: [{ data: this.annonceData, backgroundColor: ['rgba(46, 125, 50, 0.8)', 'rgba(198, 40, 40, 0.8)', 'rgba(249, 168, 37, 0.8)'], borderWidth: 0 }]
      },
      options: this.getPieOptions()
    });
  }

  private createMatchChart(): void {
    if (this.matchChart) this.matchChart.destroy();
    this.matchChart = new Chart(this.matchChartRef.nativeElement.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: ['En Attente', 'Accepté', 'Refusé', 'Terminé'],
        datasets: [{ data: this.matchData, backgroundColor: ['rgba(249, 168, 37, 0.8)', 'rgba(46, 125, 50, 0.8)', 'rgba(198, 40, 40, 0.8)', 'rgba(21, 101, 192, 0.8)'], borderWidth: 0 }]
      },
      options: this.getPieOptions()
    });
  }

  private createNotifChart(): void {
    if (this.notifChart) this.notifChart.destroy();
    this.notifChart = new Chart(this.notifChartRef.nativeElement.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: ['Envoyé', 'Lu', 'Non lu', 'Erreur'],
        datasets: [{ data: this.notificationData, backgroundColor: ['rgba(46, 125, 50, 0.8)', 'rgba(21, 101, 192, 0.8)', 'rgba(249, 168, 37, 0.8)', 'rgba(198, 40, 40, 0.8)'], borderWidth: 0 }]
      },
      options: this.getPieOptions()
    });
  }

  private createFundChart(): void {
    if (!this.fundChartRef) return; // Safety check
    
    if (this.fundChart) this.fundChart.destroy();
    this.fundChart = new Chart(this.fundChartRef.nativeElement.getContext('2d'), {
      type: 'bar',
      data: {
        labels: ['Montant Cible Total', 'Montant Financé Total'],
        datasets: [{
          data: [this.totalTargetAmount, this.totalFundedAmount],
          backgroundColor: ['rgba(249, 168, 37, 0.8)', 'rgba(46, 125, 50, 0.8)'],
          borderWidth: 0,
          barThickness: 50,
          borderRadius: 6
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { beginAtZero: true, ticks: { callback: function(value) { return value.toLocaleString('fr-TN') + ' TND'; } } }
        },
        plugins: { legend: { display: false } }
      }
    });
  }
  private processFundingStatus(): void {
    console.log('RAW DATA LENGTHS -> Annonces:', this.rawAnnonces.length, '| Matches:', this.rawMatches.length);
    
    let fullyFunded = 0;
    
    let partiallyFunded = 0;
    let zeroFunds = 0;

    // Step 1: Build a Map of how much money each Annonce ID received
    const fundedMap = new Map<number, number>();

    this.rawMatches.forEach(m => {
      if (m.status === StatusMatch.ACCEPTE) {
        // We use (m as any).annonceId to catch the hidden database foreign key
        const aId = m.annonce?.id || (m as any).annonceId;
        if (aId) {
          fundedMap.set(aId, (fundedMap.get(aId) || 0) + (m.montantPropose || 0));
        }
      }
    });

    // Step 2: Compare the map against our Annonces
    this.rawAnnonces.forEach(annonce => {
      const target = annonce.targetAmount || 0;
      
      if (target === 0) {
        zeroFunds++;
        return;
      }

      const funded = fundedMap.get(annonce.id || 0) || 0;

      if (funded >= target) {
        fullyFunded++;
      } else if (funded > 0) {
        partiallyFunded++;
      } else {
        zeroFunds++;
      }
    });

    this.fundingStatusData = [fullyFunded, partiallyFunded, zeroFunds];
  }

  private createFundingStatusChart(): void {
    if (!this.fundingStatusChartRef) return; // Safety check
    
    if (this.fundingStatusChart) this.fundingStatusChart.destroy();
    this.fundingStatusChart = new Chart(this.fundingStatusChartRef.nativeElement.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: ['Financé à 100%', 'Financé partiellement', 'Sans financement'],
        datasets: [{
          data: this.fundingStatusData,
          backgroundColor: [
            'rgba(46, 125, 50, 0.8)',   
            'rgba(255, 193, 7, 0.8)',   
            'rgba(158, 158, 158, 0.8)'  
          ],
          borderWidth: 0
        }]
      },
      options: this.getPieOptions()
    });
  }

  private getPieOptions(): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { font: { size: 12, family: 'Inter' }, padding: 16, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };
  }
}