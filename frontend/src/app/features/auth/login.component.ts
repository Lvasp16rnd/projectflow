import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  error = '';

  onSubmit() {
    if (this.form.invalid) return;
    const { username, password } = this.form.getRawValue();
    if (this.authService.login(username, password)) {
      this.router.navigate(['/dashboard']);
    } else {
      this.error = 'Credenciais inválidas. Tente admin / admin';
    }
  }
}
