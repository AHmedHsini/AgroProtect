import {
  Component, inject, signal,
  ChangeDetectionStrategy
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  error     = signal('');
  loading   = signal(false);
  showPass  = signal(false);

  form = this.fb.group({
    email:    ['karim@agroprotect.dz', [Validators.required, Validators.email]],
    password: ['admin123',             [Validators.required]],
  });

  onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set('');

    const { email, password } = this.form.value;

    // Simulation délai réseau
    setTimeout(() => {
      const ok = this.auth.loginStatic(email!, password!);
      if (ok) {
        this.router.navigate(['/dashboard']);
      } else {
        this.error.set('Email ou mot de passe incorrect.');
        this.loading.set(false);
      }
    }, 800);
  }
}