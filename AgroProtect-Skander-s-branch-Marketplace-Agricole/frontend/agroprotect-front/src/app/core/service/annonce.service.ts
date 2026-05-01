// src/app/core/service/annonce.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Annonce } from '../models/annonce.model';

export interface Attachment {
  id: number;
  fileName: string;
  filePath: string;
  fileType: string;
  fileSize: number;
  category: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  uploadedBy: number;
  uploadedAt: string;
  reviewedBy?: number;
  reviewedAt?: string;
  rejectionReason?: string;
}

@Injectable({ providedIn: 'root' })
export class AnnonceService {
  private readonly baseUrl = `${environment.apiUrl}/Annonce`;
  private readonly attachmentUrl = `${environment.apiUrl}/Annonce/attachments`;

  constructor(private http: HttpClient) {}

  // === Existing CRUD ===
  getAll(): Observable<Annonce[]> {
    return this.http.get<Annonce[]>(`${this.baseUrl}/getAll`);
  }
  getById(id: number): Observable<Annonce> {
    return this.http.get<Annonce>(`${this.baseUrl}/getById/${id}`);
  }
  create(annonce: Annonce): Observable<Annonce> {
    return this.http.post<Annonce>(`${this.baseUrl}/addAnnonce`, annonce);
  }
  update(id: number, annonce: Annonce): Observable<Annonce> {
    return this.http.put<Annonce>(`${this.baseUrl}/updateAnnonce/${id}`, annonce);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/deleteAnnonce/${id}`);
  }

  // === NEW: Attachment Methods ===

  /**
   * Upload a file to an annonce
   */
  uploadAttachment(
    annonceId: number,
    file: File,
    userId: number,
    category: string = 'OTHER'
  ): Observable<Attachment> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId.toString());
    formData.append('category', category);

    return this.http.post<Attachment>(
      `${this.attachmentUrl}/upload/${annonceId}`,
      formData
    );
  }

  /**
   * Get all attachments for an annonce
   */
  getAttachments(annonceId: number): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(`${this.attachmentUrl}/${annonceId}`);
  }

  /**
   * Download a file
   */
  downloadAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.attachmentUrl}/download/${attachmentId}`, {
      responseType: 'blob'
    });
  }

  /**
   * Delete an attachment
   */
  deleteAttachment(attachmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.attachmentUrl}/delete/${attachmentId}`);
  }
}