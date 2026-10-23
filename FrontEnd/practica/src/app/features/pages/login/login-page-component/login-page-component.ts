import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../../../core/api';

@Component({
  imports: [FormsModule],
  selector: 'app-login-page-component',
  styleUrl: './login-page-component.css',
  templateUrl: './login-page-component.html',
})
export class LoginPageComponent {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);
  email = '';
  password = '';
  error = '';
  loading = false;
  registering = false;
  success = '';
  toggleMode(): void { this.registering = !this.registering; this.error = ''; this.success = ''; }
  submit(): void {
    if (this.loading) return;
    this.loading = true; this.error = '';
    if (this.registering) {
      this.api.register(this.email.trim(), this.password).subscribe({
        next: () => {
          this.loading = false;
          this.registering = false;
          this.success = 'Cuenta creada. Ya puedes iniciar sesión.';
          this.changeDetector.markForCheck();
        },
        error: err => {
          this.loading = false;
          this.error = typeof err.error === 'string' ? err.error : 'No se pudo crear la cuenta. Comprueba el correo y que la contraseña tenga al menos 6 caracteres.';
          this.changeDetector.markForCheck();
        }
      });
      return;
    }
    this.api.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        this.changeDetector.markForCheck();
        void this.router.navigate(['/home']);
      },
      error: err => {
        this.loading = false;
        this.error = err.status === 0 ? 'No se pudo conectar con el servidor. Comprueba que el backend esté iniciado.' : 'Correo o contraseña incorrectos.';
        this.changeDetector.markForCheck();
      }
    });
  }
}
