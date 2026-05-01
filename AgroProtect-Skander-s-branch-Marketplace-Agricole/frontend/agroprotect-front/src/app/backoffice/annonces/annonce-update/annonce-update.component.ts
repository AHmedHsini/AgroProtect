import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { AnnonceService, Attachment } from '../../../core/service/annonce.service';
import { Annonce, TypeAnnonce, StatusAnnonce } from '../../../core/models/annonce.model';
import { HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-annonce-update',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule], // ← Added FormsModule
  templateUrl: './annonce-update.component.html',
  styleUrls: ['./annonce-update.component.scss']
})
export class AnnonceFormComponent implements OnInit { // ← Keep your existing class name

  form!: FormGroup;
  annonceId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  // 🔑 Attachment state properties
  selectedFile: File | null = null;
  uploadCategory = 'OTHER';
  uploading = false;
  uploadProgress = 0;
  uploadMessage = '';
  uploadError = '';
  attachments: Attachment[] = [];

  // 🔴 TEMP: Replace with actual auth service
  currentUserId = 1;

  typeAnnonce = TypeAnnonce;
  statusAnnonce = StatusAnnonce;

  constructor(
    private fb: FormBuilder,
    private annonceService: AnnonceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.annonceId = Number(id);
      this.loadAnnonce(this.annonceId);
      this.loadAttachments(); // 🔑 Load attachments on init
    }
  }

  buildForm(): void {
    this.form = this.fb.group({
      typeAnnonce: [TypeAnnonce.PROJET_AGRICOLE, Validators.required],
      titre: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', Validators.maxLength(2000)],
      status: [StatusAnnonce.EN_ATTENTE, Validators.required],
      targetAmount: [0, [Validators.required, Validators.min(0)]],
      location: ['', Validators.maxLength(100)],
      targetDurationMonths: [null]
    });
  }

  // === Load Existing Annonce ===
  loadAnnonce(id: number): void {
    this.loading = true;
    this.annonceService.getById(id).subscribe({
      next: (annonce: Annonce) => {
        this.form.patchValue(annonce);
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = 'Failed to load annonce: ' + err.message;
        this.loading = false;
      }
    });
  }

  // === Annonce Submit ===
  onSubmit(): void {
    if (this.form.invalid || !this.annonceId) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const annonce: Annonce = this.form.value;

    this.annonceService.update(this.annonceId, annonce).subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/backoffice/annonces']);
      },
      error: (err: Error) => {
        this.error = 'Update failed: ' + err.message;
        this.saving = false;
      }
    });
  }

  // === Attachment Upload Methods ===

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      const file = input.files[0];
      // Validate size (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        this.uploadError = 'File too large. Max 10MB.';
        return;
      }
      this.selectedFile = file;
      this.uploadError = '';
      this.uploadMessage = '';
    }
  }

  uploadAttachment(): void {
    if (!this.selectedFile || !this.annonceId) return;

    this.uploading = true;
    this.uploadProgress = 0;
    this.uploadError = '';
    this.uploadMessage = '';

    this.annonceService.uploadAttachment(
      this.annonceId,
      this.selectedFile,
      this.currentUserId,
      this.uploadCategory
    ).subscribe({
      next: (attachment) => {
        this.uploadProgress = 100;
        this.uploadMessage = '✓ Uploaded: ' + attachment.fileName;
        this.selectedFile = null;
        this.attachments.unshift(attachment); // Add to top
        setTimeout(() => {
          this.uploadMessage = '';
          this.uploading = false;
        }, 3000);
      },
      error: (err) => {
        // Handle CORS/network errors gracefully
        if (err.status === 0 || err.message?.includes('Unknown Error')) {
          console.warn('Possible CORS issue - checking if upload succeeded...');
          this.loadAttachments(); // Refresh list to see if file uploaded
          this.uploadMessage = '⚠️ File may have uploaded. Refreshing list...';
          this.uploading = false;
          return;
        }
        this.uploadError = 'Upload failed: ' + (err.message || 'Unknown error');
        this.uploadProgress = 0;
        this.uploading = false;
      }
    });
  }

  loadAttachments(): void {
    if (!this.annonceId) return;
    this.annonceService.getAttachments(this.annonceId).subscribe({
      next: (attachments) => this.attachments = attachments,
      error: () => {}
    });
  }

  downloadAttachment(attachmentId: number, fileName: string): void {
    this.annonceService.downloadAttachment(attachmentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => alert('Download failed: ' + err.message)
    });
  }

  deleteAttachment(attachmentId: number): void {
    if (!confirm('Delete this attachment?')) return;
    
    this.annonceService.deleteAttachment(attachmentId).subscribe({
      next: () => {
        this.attachments = this.attachments.filter(a => a.id !== attachmentId);
      },
      error: (err) => alert('Delete failed: ' + err.message)
    });
  }

  formatSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  cancel(): void {
    this.router.navigate(['/backoffice/annonces']);
  }
}