// src/app/backoffice/annonces/annonce-create/annonce-create.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { AnnonceService, Attachment } from '../../../core/service/annonce.service';
import { Annonce, TypeAnnonce, StatusAnnonce } from '../../../core/models/annonce.model';

@Component({
  selector: 'app-annonce-create',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './annonce-create.component.html',
  styleUrls: ['./annonce-create.component.scss']
})
export class AnnonceCreateComponent implements OnInit {

  form!: FormGroup;
  saving = false;
  loading = false; 
  error: string | null = null;
  annonceId: number | null = null; // Set after successful create

  // Attachment state
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
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
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

  // === Annonce Submit ===
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const annonce: Annonce = this.form.value;

    this.annonceService.create(annonce).subscribe({
      next: (created) => {
        this.saving = false;
        this.annonceId = created.id ?? null; // 🔑 Reveal upload section
        this.uploadMessage = '✅ Annonce created! You can now upload attachments.';
        this.loadAttachments(); // Load any existing files
        
        // ️ DO NOT redirect immediately. Let user upload files first.
        // Add a "Go to List" button in HTML if you want, or keep it simple.
      },
      error: (err: Error) => {
        this.error = 'Create failed: ' + err.message;
        this.saving = false;
      }
    });
  }

  // === Attachment Upload ===
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
    if (!this.selectedFile || !this.annonceId) {
      if (!this.annonceId) {
        this.uploadError = 'Save the annonce first, then upload attachments.';
      }
      return;
    }

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