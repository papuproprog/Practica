import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api, Branch, UserAccount } from '../../../../core/api';

@Component({
  imports: [CommonModule, FormsModule],
  selector: 'app-global-admin-page',
  templateUrl: './global-admin-page-component.html',
})
export class GlobalAdminPageComponent implements OnInit {
  readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  branches: Branch[] = [];
  admins: UserAccount[] = [];

  loading = true;
  error = '';
  notice = '';
  currentTab: 'branches' | 'admins' = 'branches';

  // Branch creation form
  branchId = '';
  branchName = '';
  branchAddress = '';
  branchPhone = '';
  savingBranch = false;

  // Admin creation form
  adminEmail = '';
  adminPassword = '';
  adminBranch = 'centro';
  savingAdmin = false;

  ngOnInit(): void {
    if (!this.api.loggedIn()) {
      void this.router.navigate(['/login']);
      return;
    }

    if (this.api.user()?.role !== 'SUPERADMIN') {
      void this.router.navigate(['/home']);
      return;
    }

    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = '';

    forkJoin({
      branches: this.api.getBranches(),
      admins: this.api.getUsers()
    }).subscribe({
      next: res => {
        this.branches = res.branches;
        this.admins = res.admins;
        if (this.branches.length && !this.adminBranch) {
          this.adminBranch = this.branches[0].id;
        }
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudieron cargar los datos globales del sistema.';
        this.changeDetector.markForCheck();
      }
    });
  }

  createBranch(): void {
    this.error = '';
    this.notice = '';

    if (!this.branchId.trim() || !this.branchName.trim()) {
      this.error = 'Ingresa el identificador y el nombre de la sucursal.';
      return;
    }

    const cleanId = this.branchId.trim().toLowerCase().replace(/[^a-z0-9_-]/g, '');
    if (!cleanId) {
      this.error = 'El identificador solo debe contener letras, números y guiones.';
      return;
    }

    this.savingBranch = true;
    this.api.createBranch({
      id: cleanId,
      name: this.branchName.trim(),
      address: this.branchAddress.trim(),
      phone: this.branchPhone.trim()
    }).subscribe({
      next: branch => {
        this.branches = [...this.branches, branch];
        this.branchId = '';
        this.branchName = '';
        this.branchAddress = '';
        this.branchPhone = '';
        this.savingBranch = false;
        this.notice = `Sucursal "${branch.name}" (${branch.id}) registrada correctamente.`;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.savingBranch = false;
        this.error = 'No se pudo crear la sucursal.';
        this.changeDetector.markForCheck();
      }
    });
  }

  createAdmin(): void {
    this.error = '';
    this.notice = '';

    if (!this.adminEmail.trim() || !this.adminPassword.trim()) {
      this.error = 'Ingresa el correo y la contraseña del administrador.';
      return;
    }

    if (this.adminPassword.length < 6) {
      this.error = 'La contraseña debe tener al menos 6 caracteres.';
      return;
    }

    if (!this.adminBranch) {
      this.error = 'Selecciona la sucursal a la que pertenecerá el administrador.';
      return;
    }

    this.savingAdmin = true;
    this.api.createAdmin({
      email: this.adminEmail.trim(),
      password: this.adminPassword,
      branch: this.adminBranch,
      role: 'ADMIN'
    }).subscribe({
      next: user => {
        this.admins = [...this.admins, user];
        this.adminEmail = '';
        this.adminPassword = '';
        this.savingAdmin = false;
        this.notice = `Administrador ${user.email} asignado a la sucursal "${user.branch.toUpperCase()}".`;
        this.changeDetector.markForCheck();
      },
      error: err => {
        this.savingAdmin = false;
        this.error = typeof err.error === 'string' ? err.error : 'No se pudo registrar la cuenta de administrador.';
        this.changeDetector.markForCheck();
      }
    });
  }

  branchNameFor(id: string): string {
    const b = this.branches.find(x => x.id === id);
    return b ? b.name : id.toUpperCase();
  }

  logout(): void {
    this.api.logout();
    void this.router.navigate(['/login']);
  }
}
