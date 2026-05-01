import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatchService } from '../../../core/service/match.service';
import { Match, StatusMatch } from '../../../core/models/match.model';

@Component({
  selector: 'app-match-create',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './match-create.component.html',
  styleUrls: ['./match-create.component.scss']
})
export class MatchCreateComponent implements OnInit {

  form!: FormGroup;
  saving = false;
  error: string | null = null;

  statusMatch = StatusMatch;

  constructor(
    private fb: FormBuilder,
    private matchService: MatchService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
  }

  buildForm(): void {
    this.form = this.fb.group({
      annonceId: [null, [Validators.required, Validators.min(1)]],
      investisseurId: [null, [Validators.required, Validators.min(1)]],
      status: [StatusMatch.EN_ATTENTE, Validators.required],
      montantPropose: [null, [Validators.min(0)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const match: Match = this.form.value;

    this.matchService.create(match).subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/backoffice/matches']);
      },
      error: (err: Error) => {
        this.error = 'Create failed: ' + err.message;
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/backoffice/matches']);
  }
}