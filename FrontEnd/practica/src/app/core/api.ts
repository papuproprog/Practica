import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export type AppointmentStatus = 'PENDING' | 'DONE' | 'CANCELLED';

export interface Branch {
  id: string;
  name: string;
  address?: string;
  phone?: string;
}

export interface Client {
  id: number;
  name: string;
  lastName: string;
  phone: string;
}

export interface Barber {
  id: number;
  name: string;
  lastName: string;
}

export interface ServiceItem {
  id: number;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Appointment {
  id: number;
  barber: number;
  client: number;
  status: AppointmentStatus;
  service: number;
  serviceDuration?: number;
  dateTime: string;
}

export interface AppointmentPayload {
  barber: number;
  client: number;
  status: AppointmentStatus;
  service: number;
  dateTime: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: string;
  tokenType: string;
}

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  private readonly currentUser = signal<{ email: string; role: string } | null>(this.readUser());
  readonly user = this.currentUser.asReadonly();
  readonly loggedIn = computed(() => !!this.currentUser());

  private readonly activeBranch = signal<string>(this.readBranch());
  readonly branch = this.activeBranch.asReadonly();

  setBranch(branchId: string): void {
    const clean = branchId.trim().toLowerCase();
    localStorage.setItem('barber-branch', clean);
    this.activeBranch.set(clean);
  }

  getBranch(): string {
    return this.activeBranch();
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { email, password }).pipe(
      tap(result => {
        localStorage.setItem('barber-token', result.token);
        localStorage.setItem('barber-user', JSON.stringify({ email: result.email, role: result.role }));
        this.currentUser.set({ email: result.email, role: result.role });
      })
    );
  }

  register(email: string, password: string): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/auth/register`, { email, password });
  }

  logout(): void {
    localStorage.removeItem('barber-token');
    localStorage.removeItem('barber-user');
    this.currentUser.set(null);
  }

  // Branches
  getBranches(): Observable<Branch[]> {
    return this.http.get<Branch[]>(`${this.baseUrl}/branches`);
  }

  createBranch(branch: Branch): Observable<Branch> {
    return this.http.post<Branch>(`${this.baseUrl}/branches`, branch);
  }

  // Clients
  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/clients`);
  }

  createClient(client: Omit<Client, 'id'>): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/clients`, client);
  }

  // Barbers
  getBarbers(): Observable<Barber[]> {
    return this.http.get<Barber[]>(`${this.baseUrl}/barbers`);
  }

  createBarber(barber: Omit<Barber, 'id'>): Observable<Barber> {
    return this.http.post<Barber>(`${this.baseUrl}/barbers`, barber);
  }

  deleteBarber(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/barbers/${id}`);
  }

  // Services
  getServices(): Observable<ServiceItem[]> {
    return this.http.get<ServiceItem[]>(`${this.baseUrl}/services`);
  }

  createService(service: Omit<ServiceItem, 'id'>): Observable<ServiceItem> {
    return this.http.post<ServiceItem>(`${this.baseUrl}/services`, service);
  }

  updateService(id: number, service: Partial<ServiceItem>): Observable<ServiceItem> {
    return this.http.put<ServiceItem>(`${this.baseUrl}/services/${id}`, service);
  }

  deleteService(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/services/${id}`);
  }

  // Appointments (Dates)
  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.baseUrl}/dates`);
  }

  createAppointment(payload: AppointmentPayload): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.baseUrl}/dates`, payload);
  }

  updateAppointment(id: number, payload: AppointmentPayload): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.baseUrl}/dates/${id}`, payload);
  }

  deleteAppointment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/dates/${id}`);
  }

  private readUser(): { email: string; role: string } | null {
    try {
      return JSON.parse(localStorage.getItem('barber-user') ?? 'null');
    } catch {
      return null;
    }
  }

  private readBranch(): string {
    try {
      return localStorage.getItem('barber-branch') || 'centro';
    } catch {
      return 'centro';
    }
  }
}
