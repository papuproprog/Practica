import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api, Appointment, AppointmentStatus, Barber, Client, ServiceItem } from '../../../../core/api';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-home-page-component',
  templateUrl: './home-page-component.html',
})
export class HomePageComponent implements OnInit {
  readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly changeDetector = inject(ChangeDetectorRef);

  appointments: Appointment[] = [];
  clients: Client[] = [];
  barbers: Barber[] = [];
  services: ServiceItem[] = [];

  loading = true;
  error = '';
  notice = '';
  filter = 'ALL';
  currentTab: 'agenda' | 'barbers' | 'services' = 'agenda';

  // New Barber Form
  newBarberName = '';
  newBarberLastName = '';
  savingBarber = false;

  // New Service Form
  newServiceName = '';
  newServiceDesc = '';
  newServiceDuration = 30;
  newServicePrice = 150;
  savingService = false;

  get branchLabel(): string {
    return this.api.branch() === 'norte' ? 'Sucursal Norte' : 'Sucursal Centro';
  }

  ngOnInit(): void {
    if (!this.api.loggedIn()) {
      void this.router.navigate(['/login']);
      return;
    }

    if (this.api.user()?.role === 'SUPERADMIN') {
      void this.router.navigate(['/global-admin']);
      return;
    }

    this.route.queryParamMap.subscribe(params => {
      const tab = params.get('tab');
      if (tab === 'barbers' || tab === 'services' || tab === 'agenda') {
        this.currentTab = tab;
        this.changeDetector.markForCheck();
      }
    });

    this.loadData();
  }

  setTab(tab: 'agenda' | 'barbers' | 'services'): void {
    this.currentTab = tab;
    void this.router.navigate([], { queryParams: { tab }, queryParamsHandling: 'merge' });
  }

  loadData(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      appointments: this.api.getAppointments(),
      clients: this.api.getClients(),
      barbers: this.api.getBarbers(),
      services: this.api.getServices()
    }).subscribe({
      next: d => {
        this.appointments = d.appointments.sort((a, b) => a.dateTime.localeCompare(b.dateTime));
        this.clients = d.clients;
        this.barbers = d.barbers;
        this.services = d.services;
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: e => {
        this.loading = false;
        this.error = e.status === 0
          ? 'No se pudo conectar con el backend. Comprueba que los servicios estén activos con Docker.'
          : 'No se pudieron cargar los datos de la sucursal.';
        this.changeDetector.markForCheck();
      }
    });
  }

  get filtered() {
    return this.filter === 'ALL'
      ? this.appointments
      : this.appointments.filter(a => a.status === this.filter);
  }

  get todayCount() {
    const d = new Date();
    const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    return this.appointments.filter(a => a.dateTime.startsWith(today) && a.status !== 'CANCELLED').length;
  }

  get pendingCount() {
    return this.appointments.filter(a => a.status === 'PENDING').length;
  }

  get clientCount() {
    return this.clients.length;
  }

  get barberCount() {
    return this.barbers.length;
  }

  get serviceCount() {
    return this.services.length;
  }

  clientName(id: number) {
    const c = this.clients.find(x => x.id === id);
    return c ? `${c.name} ${c.lastName}` : 'Cliente';
  }

  barberName(id: number) {
    const b = this.barbers.find(x => x.id === id);
    return b ? `${b.name} ${b.lastName}` : 'Barbero';
  }

  serviceName(id: number) {
    const s = this.services.find(x => x.id === id);
    return s ? s.name : `Servicio #${id}`;
  }

  // Appointment Actions
  setStatus(a: Appointment, status: AppointmentStatus) {
    const body = { barber: a.barber, client: a.client, service: a.service, status, dateTime: a.dateTime };
    this.api.updateAppointment(a.id, body).subscribe({
      next: u => {
        this.appointments = this.appointments.map(x => x.id === u.id ? u : x);
        this.notice = 'Estado actualizado.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo actualizar el estado.';
        this.changeDetector.markForCheck();
      }
    });
  }

  cancel(a: Appointment) {
    if (!confirm(`¿Cancelar la cita de ${this.clientName(a.client)}?`)) return;
    const body = { barber: a.barber, client: a.client, service: a.service, status: 'CANCELLED' as const, dateTime: a.dateTime };
    this.api.updateAppointment(a.id, body).subscribe({
      next: u => {
        this.appointments = this.appointments.map(x => x.id === u.id ? u : x);
        this.notice = 'La cita se canceló y quedó en el historial.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo cancelar la cita.';
        this.changeDetector.markForCheck();
      }
    });
  }

  // Barber Actions
  addBarber(): void {
    if (!this.newBarberName.trim() || !this.newBarberLastName.trim()) {
      this.error = 'Por favor escribe el nombre y apellido del barbero.';
      return;
    }
    this.savingBarber = true;
    this.error = '';
    this.api.createBarber({
      name: this.newBarberName.trim(),
      lastName: this.newBarberLastName.trim()
    }).subscribe({
      next: barber => {
        this.barbers = [...this.barbers, barber];
        this.newBarberName = '';
        this.newBarberLastName = '';
        this.savingBarber = false;
        this.notice = `Barbero ${barber.name} ${barber.lastName} agregado a la sucursal.`;
        this.changeDetector.markForCheck();
      },
      error: err => {
        this.savingBarber = false;
        this.error = err.error || 'No se pudo agregar el barbero.';
        this.changeDetector.markForCheck();
      }
    });
  }

  removeBarber(id: number): void {
    const barber = this.barbers.find(b => b.id === id);
    if (!confirm(`¿Eliminar al barbero ${barber?.name} ${barber?.lastName}?`)) return;
    this.api.deleteBarber(id).subscribe({
      next: () => {
        this.barbers = this.barbers.filter(b => b.id !== id);
        this.notice = 'Barbero eliminado de la sucursal.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo eliminar el barbero.';
        this.changeDetector.markForCheck();
      }
    });
  }

  // Service Actions
  addService(): void {
    if (!this.newServiceName.trim() || !this.newServiceDesc.trim()) {
      this.error = 'Por favor escribe el nombre y la descripción del servicio.';
      return;
    }
    if (this.newServiceDuration <= 0 || this.newServicePrice < 0) {
      this.error = 'La duración debe ser mayor a 0 y el precio no puede ser negativo.';
      return;
    }
    this.savingService = true;
    this.error = '';
    this.api.createService({
      name: this.newServiceName.trim(),
      description: this.newServiceDesc.trim(),
      durationMinutes: this.newServiceDuration,
      price: this.newServicePrice
    }).subscribe({
      next: service => {
        this.services = [...this.services, service];
        this.newServiceName = '';
        this.newServiceDesc = '';
        this.newServiceDuration = 30;
        this.newServicePrice = 150;
        this.savingService = false;
        this.notice = `Servicio "${service.name}" creado en la base de datos de la sucursal.`;
        this.changeDetector.markForCheck();
      },
      error: err => {
        this.savingService = false;
        this.error = err.error || 'No se pudo crear el servicio.';
        this.changeDetector.markForCheck();
      }
    });
  }

  removeService(id: number): void {
    const service = this.services.find(s => s.id === id);
    if (!confirm(`¿Eliminar el servicio "${service?.name}"?`)) return;
    this.api.deleteService(id).subscribe({
      next: () => {
        this.services = this.services.filter(s => s.id !== id);
        this.notice = 'Servicio eliminado de la sucursal.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo eliminar el servicio.';
        this.changeDetector.markForCheck();
      }
    });
  }

  logout() {
    this.api.logout();
    void this.router.navigate(['/login']);
  }
}
