import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatchService } from '../../../core/service/match.service';
import { Match, StatusMatch } from '../../../core/models/match.model';

@Component({
  selector: 'app-match-update',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './match-update.component.html',
  styleUrls: ['./match-update.component.scss']
})
export class MatchUpdateComponent implements OnInit {

  form!: FormGroup;
  matchId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  statusMatch = StatusMatch;

  constructor(
    private fb: FormBuilder,
    private matchService: MatchService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.matchId = Number(id);
      this.loadMatch(this.matchId);
    }
  }

  buildForm(): void {
    this.form = this.fb.group({
      annonceId: [null, [Validators.required, Validators.min(1)]],
      status: [StatusMatch.EN_ATTENTE, Validators.required],
      montantPropose: [null, [Validators.min(0)]]
    });
  }

  // In loadMatch() method
// In loadMatch() method
loadMatch(id: number): void {
    this.loading = true;
    this.matchService.getById(id).subscribe({
      next: (match: Match) => {
        this.form.patchValue({
          annonceId: match.annonce?.id ?? match.annonceId ?? null,
          // ✅ Ensure status is always set with fallback
          status: match.status ?? StatusMatch.EN_ATTENTE,
    
          montantPropose: match.montantPropose 
        });
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = 'Failed to load match: ' + err.message;
        this.loading = false;
      }
    });
  }
  
  // In onSubmit() method
  onSubmit(): void {
    if (this.form.invalid || !this.matchId) {
      console.log('Form invalid:', this.form.errors);
      console.log('Form values:', this.form.value);
       return;
      }

      console.log('Form is valid');
      console.log('Match ID:', this.matchId);
      console.log('Form value:', this.form.value);
  
    this.saving = true;
    const matchData: Match = {
      ...this.form.value,
      // ✅ Ensure status is never null
      status: this.form.value.status ?? StatusMatch.EN_ATTENTE,
      montantPropose: this.form.value.montantPropose ?? null 
    };
    console.log('Sending update:', matchData);
  
    this.matchService.update(this.matchId, matchData).subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/backoffice/matches']);
      },
      error: (err: Error) => {
        this.error = 'Update failed: ' + err.message;
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/backoffice/matches']);
  }
}