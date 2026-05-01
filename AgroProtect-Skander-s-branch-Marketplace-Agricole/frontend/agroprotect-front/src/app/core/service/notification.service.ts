import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationHistory, StatusNotification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private readonly baseUrl = `${environment.apiUrl}/NotificationHistory`;

  constructor(private http: HttpClient) {}

  // === CRUD ===
  getAll(): Observable<NotificationHistory[]> {
    return this.http.get<NotificationHistory[]>(`${this.baseUrl}/getAll`);
  }

  getById(id: number): Observable<NotificationHistory> {
    return this.http.get<NotificationHistory>(`${this.baseUrl}/getById/${id}`);
  }

  create(notification: NotificationHistory): Observable<NotificationHistory> {
    return this.http.post<NotificationHistory>(`${this.baseUrl}/addNotification`, notification);
  }

  // === Filters ===
  getByRecipient(userId: number): Observable<NotificationHistory[]> {
    return this.http.get<NotificationHistory[]>(`${this.baseUrl}/getByRecipient/${userId}`);
  }

  getBySender(senderId: number): Observable<NotificationHistory[]> {
    return this.http.get<NotificationHistory[]>(`${this.baseUrl}/getBySender/${senderId}`);
  }

  getByStatus(status: StatusNotification): Observable<NotificationHistory[]> {
    return this.http.get<NotificationHistory[]>(`${this.baseUrl}/getByStatus/${status}`);
  }

  // === Actions ===
  updateStatus(id: number, status: StatusNotification): Observable<NotificationHistory> {
    return this.http.put<NotificationHistory>(`${this.baseUrl}/updateStatus/${id}/${status}`, {});
  }

  markAsRead(id: number): Observable<NotificationHistory> {
    return this.updateStatus(id, StatusNotification.LU);
  }

  markAsUnread(id: number): Observable<NotificationHistory> {
    return this.updateStatus(id, StatusNotification.NON_LU);
  }
}