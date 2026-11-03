import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../../../core/api';

@Component({
  imports: [FormsModule],
  selector: 'app-login-page-component',
  styleUrl: './login-page-component.css',
  templateUrl: './login-page-component.html',
})
export class LoginPageComponent implements OnInit {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  email = '';
  password = '';
  error = '';
  loading = false;
  registering = false;
  success = '';

  ngOnInit(): void {
    if (this.api.loggedIn()) {
      if (this.api.user()?.role === 'SUPERADMIN') {
        void this.router.navigate(['/global-admin']);
      } else {
        void this.router.navigate(['/home']);
      }
    }
  }

  toggleMode(): void {
    this.registering = !this.registering;
    this.error = '';
    this.success = '';
  }

  submit(): void {
    if (this.loading) return;
    this.loading = true;
    this.error = '';

    if (this.registering) {
      this.api.register(this.email.trim(), this.password).subscribe({
        next: () => {
          this.loading = false;
          this.registering = false;
          this.success = 'Cuenta creada exitosamente. Ya puedes iniciar sesión.';
          this.changeDetector.markForCheck();
        },
        error: err => {
          this.loading = false;
          this.error = typeof err.error === 'string' ? err.error : 'No se pudo crear la cuenta. Verifica los datos.';
          this.changeDetector.markForCheck();
        }
      });
      return;
    }

    this.api.login(this.email.trim(), this.password).subscribe({
      next: res => {
        this.loading = false;
        this.changeDetector.markForCheck();
        if (res.role === 'SUPERADMIN') {
          void this.router.navigate(['/global-admin']);
        } else {
          void this.router.navigate(['/home']);
        }
      },
      error: err => {
        this.loading = false;
        this.error = err.status === 0
          ? 'No se pudo conectar con el servidor. Comprueba que el backend esté iniciado.'
          : 'Credenciales inválidas. Verifica tu correo y contraseña.';
        this.changeDetector.markForCheck();
      }
    });
  }
}
