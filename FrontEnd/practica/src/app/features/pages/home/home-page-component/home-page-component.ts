import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api, Appointment, AppointmentStatus, Barber, Client, ServiceType } from '../../../../core/api';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-home-page-component',
  templateUrl: './home-page-component.html',
})
export class HomePageComponent implements OnInit {
  readonly api=inject(Api); private readonly router=inject(Router);
  private readonly changeDetector=inject(ChangeDetectorRef);
  appointments:Appointment[]=[]; clients:Client[]=[]; barbers:Barber[]=[]; loading=true; error=''; notice=''; filter='ALL';
  readonly serviceNames:Record<ServiceType,string>={HAIRCUT:'Corte de cabello',BEARD_TRIM:'Arreglo de barba',HAIRCUT_AND_BEARD:'Corte y barba',SHAVE:'Afeitado tradicional',HAIR_DYE:'Tinte / Coloración',FACIAL_TREATMENT:'Tratamiento facial',EYEBROW_DESIGN:'Perfilado de cejas',HAIR_WASH_AND_STYLING:'Lavado y peinado'};
  ngOnInit():void {
    if(!this.api.loggedIn()){void this.router.navigate(['/login']);return;}
    forkJoin({appointments:this.api.getAppointments(),clients:this.api.getClients(),barbers:this.api.getBarbers()}).subscribe({
      next:d=>{this.appointments=d.appointments.sort((a,b)=>a.dateTime.localeCompare(b.dateTime));this.clients=d.clients;this.barbers=d.barbers;this.loading=false;this.changeDetector.markForCheck();},
      error:e=>{this.loading=false;this.error=e.status===0?'No se pudo conectar con el backend. Comprueba que los servicios estén activos con Docker.':'No se pudieron cargar los datos de la agenda.';this.changeDetector.markForCheck();}
    });
  }
  get filtered(){return this.filter==='ALL'?this.appointments:this.appointments.filter(a=>a.status===this.filter);}
  get todayCount(){const d=new Date();const today=`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;return this.appointments.filter(a=>a.dateTime.startsWith(today)&&a.status!=='CANCELLED').length;}
  get pendingCount(){return this.appointments.filter(a=>a.status==='PENDING').length;}
  get clientCount(){return this.clients.length;}
  clientName(id:number){const c=this.clients.find(x=>x.id===id);return c?`${c.name} ${c.lastName}`:'Cliente';}
  barberName(id:number){const b=this.barbers.find(x=>x.id===id);return b?`${b.name} ${b.lastName}`:'Barbero';}
  setStatus(a:Appointment,status:AppointmentStatus){const body={barber:a.barber,client:a.client,service:a.service,status,dateTime:a.dateTime};this.api.updateAppointment(a.id,body).subscribe({next:u=>{this.appointments=this.appointments.map(x=>x.id===u.id?u:x);this.notice='Estado actualizado.';this.changeDetector.markForCheck();},error:()=>{this.error='No se pudo actualizar el estado.';this.changeDetector.markForCheck();}});}
  cancel(a:Appointment){if(!confirm(`¿Cancelar la cita de ${this.clientName(a.client)}?`))return;const body={barber:a.barber,client:a.client,service:a.service,status:'CANCELLED' as const,dateTime:a.dateTime};this.api.updateAppointment(a.id,body).subscribe({next:u=>{this.appointments=this.appointments.map(x=>x.id===u.id?u:x);this.notice='La cita se canceló y quedó en el historial.';this.changeDetector.markForCheck();},error:()=>{this.error='No se pudo cancelar la cita.';this.changeDetector.markForCheck();}});}
  logout(){this.api.logout();void this.router.navigate(['/login']);}
}
