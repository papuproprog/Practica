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
  branch: 'centro' | 'norte' = 'centro';
  error = '';
  loading = false;
  registering = false;
  success = '';

  ngOnInit(): void {
    if (this.api.loggedIn()) {
      void this.router.navigate(['/home']);
    }
  }

  selectBranch(b: 'centro' | 'norte'): void {
    this.branch = b;
    this.error = '';
  }

  onEmailChange(): void {
    const val = this.email.toLowerCase();
    if (val.includes('norte')) {
      this.branch = 'norte';
    } else if (val.includes('centro')) {
      this.branch = 'centro';
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

    // Assign tenant branch header prior to auth call
    this.api.setBranch(this.branch);

    if (this.registering) {
      this.api.register(this.email.trim(), this.password).subscribe({
        next: () => {
          this.loading = false;
          this.registering = false;
          this.success = `Cuenta creada para ${this.branch === 'norte' ? 'Sucursal Norte' : 'Sucursal Centro'}. Ya puedes iniciar sesión.`;
          this.changeDetector.markForCheck();
        },
        error: err => {
          this.loading = false;
          this.error = typeof err.error === 'string' ? err.error : 'No se pudo crear la cuenta en esta sucursal. Verifica los datos.';
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
        this.error = err.status === 0
          ? 'No se pudo conectar con el servidor. Comprueba que el backend esté iniciado.'
          : `Credenciales inválidas para ${this.branch === 'norte' ? 'Sucursal Norte' : 'Sucursal Centro'}.`;
        this.changeDetector.markForCheck();
      }
    });
  }
}
