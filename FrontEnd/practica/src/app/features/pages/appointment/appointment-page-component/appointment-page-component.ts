import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api, Appointment, AppointmentPayload, AppointmentStatus, Barber, Client, ServiceItem } from '../../../../core/api';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-appointment-page',
  templateUrl: './appointment-page-component.html',
})
export class AppointmentPageComponent implements OnInit {
  readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  clients: Client[] = [];
  barbers: Barber[] = [];
  services: ServiceItem[] = [];
  appointments: Appointment[] = [];

  editId: number | null = null;
  clientId: number | null = null;
  barberId: number | null = null;
  serviceId: number | null = null;

  today = this.localDateKey(new Date());
  date = this.today;
  time = '10:00';
  status: AppointmentStatus = 'PENDING';

  // Inline creation states
  newClient = false;
  clientName = '';
  clientLastName = '';
  clientPhone = '';

  newBarber = false;
  barberNameInput = '';
  barberLastNameInput = '';

  newService = false;
  serviceNameInput = '';
  serviceDescInput = '';
  serviceDurationInput = 30;
  servicePriceInput = 150;

  loading = true;
  saving = false;
  error = '';
  notice = '';

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
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      clients: this.api.getClients(),
      barbers: this.api.getBarbers(),
      services: this.api.getServices(),
      appointments: this.api.getAppointments()
    }).subscribe({
      next: data => {
        this.clients = data.clients;
        this.barbers = data.barbers;
        this.services = data.services;
        this.appointments = data.appointments;
        this.loading = false;

        if (this.services.length && !this.serviceId) {
          this.serviceId = this.services[0].id;
        }

        const id = Number(this.route.snapshot.queryParamMap.get('id'));
        if (id) {
          const item = this.appointments.find(a => a.id === id);
          if (item) {
            this.editId = id;
            this.clientId = item.client;
            this.barberId = item.barber;
            this.serviceId = item.service;
            this.status = item.status;
            this.date = item.dateTime.slice(0, 10);
            this.time = item.dateTime.slice(11, 16);
          }
        }
        this.changeDetector.markForCheck();
      },
      error: err => {
        this.loading = false;
        this.error = err.status === 0
          ? 'No se pudo conectar con el backend. Inicia los servicios con Docker y vuelve a cargar.'
          : 'No se pudieron cargar clientes, barberos, servicios y citas.';
        this.changeDetector.markForCheck();
      }
    });
  }

  get selectedService(): ServiceItem | undefined {
    return this.services.find(s => s.id === this.serviceId);
  }

  private localDateKey(date: Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  clientNameFor(id: number) {
    const c = this.clients.find(x => x.id === id);
    return c ? `${c.name} ${c.lastName}` : 'Cliente';
  }

  barberNameFor(id: number) {
    const b = this.barbers.find(x => x.id === id);
    return b ? `${b.name} ${b.lastName}` : 'Barbero';
  }

  serviceNameFor(id: number) {
    const s = this.services.find(x => x.id === id);
    return s ? s.name : `Servicio #${id}`;
  }

  statusName(s: AppointmentStatus) {
    return s === 'PENDING' ? 'Pendiente' : s === 'DONE' ? 'Completada' : 'Cancelada';
  }

  save(): void {
    this.error = '';
    this.notice = '';

    // Validate inputs
    if (!this.date || !this.time) {
      this.error = 'Completa la fecha y hora de la cita.';
      return;
    }
    if (!this.newBarber && !this.barberId) {
      this.error = 'Selecciona o registra un barbero.';
      return;
    }
    if (!this.newClient && !this.clientId) {
      this.error = 'Selecciona o registra un cliente.';
      return;
    }
    if (!this.newService && !this.serviceId) {
      this.error = 'Selecciona o registra un servicio.';
      return;
    }

    const duration = this.newService ? this.serviceDurationInput : (this.selectedService?.durationMinutes ?? 30);
    const scheduled = new Date(`${this.date}T${this.time}:00`);
    const now = new Date();
    const endMinutes = scheduled.getHours() * 60 + scheduled.getMinutes() + duration;

    if (scheduled.getTime() < now.getTime() + 30 * 60 * 1000) {
      this.error = 'Agenda con al menos 30 minutos de anticipación.';
      return;
    }
    if (scheduled.getTime() > now.getTime() + 60 * 24 * 60 * 60 * 1000) {
      this.error = 'Solo se pueden agendar citas dentro de los próximos 60 días.';
      return;
    }
    if (scheduled.getHours() < 8 || scheduled.getHours() >= 20 || endMinutes > 20 * 60) {
      this.error = 'El horario de atención es de 08:00 a 20:00. Ajusta la hora o el servicio.';
      return;
    }

    this.saving = true;

    // Step 1: Resolve Barber
    const resolveBarber = (cb: (bId: number) => void) => {
      if (this.newBarber) {
        if (!this.barberNameInput.trim() || !this.barberLastNameInput.trim()) {
          this.saving = false;
          this.error = 'Captura el nombre y apellido del nuevo barbero.';
          return;
        }
        this.api.createBarber({ name: this.barberNameInput.trim(), lastName: this.barberLastNameInput.trim() }).subscribe({
          next: b => {
            this.barbers = [...this.barbers, b];
            this.barberId = b.id;
            this.newBarber = false;
            cb(b.id);
          },
          error: e => {
            this.saving = false;
            this.error = e.error || 'No se pudo registrar al barbero.';
            this.changeDetector.markForCheck();
          }
        });
      } else {
        cb(this.barberId!);
      }
    };

    // Step 2: Resolve Service
    const resolveService = (cb: (sId: number) => void) => {
      if (this.newService) {
        if (!this.serviceNameInput.trim() || !this.serviceDescInput.trim()) {
          this.saving = false;
          this.error = 'Captura el nombre y descripción del nuevo servicio.';
          return;
        }
        this.api.createService({
          name: this.serviceNameInput.trim(),
          description: this.serviceDescInput.trim(),
          durationMinutes: this.serviceDurationInput,
          price: this.servicePriceInput
        }).subscribe({
          next: s => {
            this.services = [...this.services, s];
            this.serviceId = s.id;
            this.newService = false;
            cb(s.id);
          },
          error: e => {
            this.saving = false;
            this.error = e.error || 'No se pudo crear el servicio.';
            this.changeDetector.markForCheck();
          }
        });
      } else {
        cb(this.serviceId!);
      }
    };

    // Step 3: Resolve Client
    const resolveClient = (cb: (cId: number) => void) => {
      if (this.newClient) {
        if (!this.clientName.trim() || !this.clientLastName.trim() || !/^\+?[0-9]{10,15}$/.test(this.clientPhone.trim())) {
          this.saving = false;
          this.error = 'Captura nombre, apellido y teléfono válido (10 a 15 dígitos).';
          return;
        }
        this.api.createClient({ name: this.clientName.trim(), lastName: this.clientLastName.trim(), phone: this.clientPhone.trim() }).subscribe({
          next: c => {
            this.clients = [...this.clients, c];
            this.clientId = c.id;
            this.newClient = false;
            cb(c.id);
          },
          error: e => {
            this.saving = false;
            this.error = e.error || 'No se pudo registrar al cliente.';
            this.changeDetector.markForCheck();
          }
        });
      } else {
        cb(this.clientId!);
      }
    };

    // Execute chain
    resolveBarber(bId => {
      resolveService(sId => {
        resolveClient(cId => {
          const payload: AppointmentPayload = {
            client: cId,
            barber: bId,
            service: sId,
            status: this.status,
            dateTime: `${this.date}T${this.time}:00`
          };
          const request = this.editId
            ? this.api.updateAppointment(this.editId, payload)
            : this.api.createAppointment(payload);

          request.subscribe({
            next: () => {
              this.saving = false;
              this.notice = this.editId ? 'Los cambios de la cita se guardaron.' : 'La cita se creó correctamente.';
              this.editId = null;
              this.changeDetector.markForCheck();
              void this.router.navigate(['/home']);
            },
            error: e => {
              this.saving = false;
              this.error = e.status === 0
                ? 'No se pudo conectar con el backend.'
                : (e.error || 'No se pudo guardar la cita. Revisa que la fecha sea futura.');
              this.changeDetector.markForCheck();
            }
          });
        });
      });
    });
  }

  changeStatus(item: Appointment, status: AppointmentStatus): void {
    const payload: AppointmentPayload = {
      barber: item.barber,
      client: item.client,
      service: item.service,
      status,
      dateTime: item.dateTime
    };
    this.api.updateAppointment(item.id, payload).subscribe({
      next: updated => {
        this.appointments = this.appointments.map(a => a.id === item.id ? updated : a);
        this.notice = 'Estado actualizado.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo actualizar el estado de la cita.';
        this.changeDetector.markForCheck();
      }
    });
  }

  cancel(item: Appointment): void {
    if (!confirm(`¿Cancelar la cita de ${this.clientNameFor(item.client)}?`)) return;
    const payload: AppointmentPayload = {
      barber: item.barber,
      client: item.client,
      service: item.service,
      status: 'CANCELLED',
      dateTime: item.dateTime
    };
    this.api.updateAppointment(item.id, payload).subscribe({
      next: updated => {
        this.appointments = this.appointments.map(a => a.id === updated.id ? updated : a);
        this.notice = 'La cita se canceló y quedó en el historial.';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo cancelar la cita.';
        this.changeDetector.markForCheck();
      }
    });
  }

  logout(): void {
    this.api.logout();
    void this.router.navigate(['/login']);
  }
}
