import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NotificationHistory, StatusNotification } from '../../../core/models/notification.model';
import { NotificationService } from '../../../core/service/notification.service';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss']
})
export class NotificationListComponent implements OnInit {

  notifications: NotificationHistory[] = [];
  filteredNotifications: NotificationHistory[] = [];
  loading = false;
  error: string | null = null;

  // ✅ Properties for simple stats (no complex logic in HTML)
  totalSent = 0;
  totalRead = 0;
  totalUnread = 0;
  totalErrors = 0;

  // Filters
  statusFilter: StatusNotification | 'ALL' = 'ALL';
  searchQuery = '';
  recipientFilter: number | null = null;

  StatusNotification = StatusNotification;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.error = null;

    this.notificationService.getAll().subscribe({
      next: (data) => {
        this.notifications = data;
        this.calculateStats();
        this.applyFilters();
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = 'Failed to load notifications: ' + err.message;
        this.loading = false;
      }
    });
  }

  // ✅ Move calculation logic to TypeScript
  calculateStats(): void {
    this.totalSent = this.notifications.length;
    this.totalRead = this.notifications.filter(n => n.status === StatusNotification.LU).length;
    this.totalUnread = this.notifications.filter(n => n.status === StatusNotification.NON_LU).length;
    this.totalErrors = this.notifications.filter(n => n.status === StatusNotification.ERREUR).length;
  }

  applyFilters(): void {
    let result = [...this.notifications];
    if (this.statusFilter !== 'ALL') {
      result = result.filter(n => n.status === this.statusFilter);
    }
    if (this.recipientFilter) {
      result = result.filter(n => n.to === this.recipientFilter);
    }
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(n => 
        n.subject.toLowerCase().includes(query) || 
        n.content.toLowerCase().includes(query)
      );
    }
    this.filteredNotifications = result;
  }

  markAsRead(notification: NotificationHistory): void {
    if (notification.status === StatusNotification.LU) return;
    this.notificationService.updateStatus(notification.id!, StatusNotification.LU).subscribe({
      next: () => {
        notification.status = StatusNotification.LU;
        this.calculateStats();
      },
      error: (err) => alert('Failed: ' + err.message)
    });
  }

  markAsUnread(notification: NotificationHistory): void {
    if (notification.status === StatusNotification.NON_LU) return;
    this.notificationService.updateStatus(notification.id!, StatusNotification.NON_LU).subscribe({
      next: () => {
        notification.status = StatusNotification.NON_LU;
        this.calculateStats();
      },
      error: (err) => alert('Failed: ' + err.message)
    });
  }

  viewContent(notification: NotificationHistory): void {
    alert(`Subject: ${notification.subject}\n\n${notification.content}`);
  }

  getStatusClass(status: StatusNotification): string {
    switch (status) {
      case StatusNotification.ENVOYE: return 'bg-primary';
      case StatusNotification.LU: return 'bg-success';
      case StatusNotification.NON_LU: return 'bg-warning text-dark';
      case StatusNotification.ERREUR: return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  formatDate(date: string | undefined): string {
    if (!date) return '-';
    return new Date(date).toLocaleString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  truncateContent(content: string, maxLength: number = 100): string {
    if (!content) return '';
    return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
  }

  clearFilters(): void {
    this.statusFilter = 'ALL';
    this.searchQuery = '';
    this.recipientFilter = null;
    this.applyFilters();
  }
}