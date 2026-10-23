import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export type AppointmentStatus = 'PENDING' | 'DONE' | 'CANCELLED';
export type ServiceType = 'HAIRCUT' | 'BEARD_TRIM' | 'HAIRCUT_AND_BEARD' | 'SHAVE' | 'HAIR_DYE' | 'FACIAL_TREATMENT' | 'EYEBROW_DESIGN' | 'HAIR_WASH_AND_STYLING';
export interface Client { id: number; name: string; lastName: string; phone: string }
export interface Barber { id: number; name: string; lastName: string }
export interface Appointment { id: number; barber: number; client: number; status: AppointmentStatus; service: ServiceType; dateTime: string }
export interface AppointmentPayload { barber: number; client: number; status: AppointmentStatus; service: ServiceType; dateTime: string }
export interface AuthResponse { token: string; email: string; role: string; tokenType: string }

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';
  private readonly currentUser = signal<{email: string; role: string} | null>(this.readUser());
  readonly user = this.currentUser.asReadonly();
  readonly loggedIn = computed(() => !!this.currentUser());
  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, {email, password}).pipe(tap(result => {
      localStorage.setItem('barber-token', result.token);
      localStorage.setItem('barber-user', JSON.stringify({email: result.email, role: result.role}));
      this.currentUser.set({email: result.email, role: result.role});
    }));
  }
  register(email: string, password: string): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/auth/register`, {email, password});
  }
  logout(): void { localStorage.removeItem('barber-token'); localStorage.removeItem('barber-user'); this.currentUser.set(null); }
  getClients(): Observable<Client[]> { return this.http.get<Client[]>(`${this.baseUrl}/clients`); }
  createClient(client: Omit<Client, 'id'>): Observable<Client> { return this.http.post<Client>(`${this.baseUrl}/clients`, client); }
  getBarbers(): Observable<Barber[]> { return this.http.get<Barber[]>(`${this.baseUrl}/barbers`); }
  getAppointments(): Observable<Appointment[]> { return this.http.get<Appointment[]>(`${this.baseUrl}/dates`); }
  createAppointment(payload: AppointmentPayload): Observable<Appointment> { return this.http.post<Appointment>(`${this.baseUrl}/dates`, payload); }
  updateAppointment(id: number, payload: AppointmentPayload): Observable<Appointment> { return this.http.put<Appointment>(`${this.baseUrl}/dates/${id}`, payload); }
  deleteAppointment(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/dates/${id}`); }
  private readUser(): {email: string; role: string} | null { try { return JSON.parse(localStorage.getItem('barber-user') ?? 'null'); } catch { return null; } }
}
