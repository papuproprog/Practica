import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api, Appointment, AppointmentPayload, AppointmentStatus, Barber, Client, ServiceType } from '../../../../core/api';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-appointment-page',
  templateUrl: './appointment-page-component.html',
})
export class AppointmentPageComponent implements OnInit {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);
  clients: Client[] = [];
  barbers: Barber[] = [];
  appointments: Appointment[] = [];
  editId: number | null = null;
  clientId: number | null = null;
  barberId: number | null = null;
  today = this.localDateKey(new Date());
  date = this.today;
  time = '10:00';
  service: ServiceType = 'HAIRCUT';
  status: AppointmentStatus = 'PENDING';
  newClient = false;
  clientName = '';
  clientLastName = '';
  clientPhone = '';
  loading = true;
  saving = false;
  error = '';
  notice = '';
  readonly services: {id: ServiceType; name: string; duration: number}[] = [
    {id:'HAIRCUT',name:'Corte de cabello',duration:30},{id:'BEARD_TRIM',name:'Arreglo de barba',duration:20},
    {id:'HAIRCUT_AND_BEARD',name:'Corte y barba',duration:50},{id:'SHAVE',name:'Afeitado tradicional',duration:30},
    {id:'HAIR_DYE',name:'Tinte / Coloración',duration:60},{id:'FACIAL_TREATMENT',name:'Tratamiento facial',duration:40},
    {id:'EYEBROW_DESIGN',name:'Perfilado de cejas',duration:15},{id:'HAIR_WASH_AND_STYLING',name:'Lavado y peinado',duration:25}
  ];
  ngOnInit(): void {
    forkJoin({clients:this.api.getClients(),barbers:this.api.getBarbers(),appointments:this.api.getAppointments()}).subscribe({
      next: data => {
        this.clients=data.clients; this.barbers=data.barbers; this.appointments=data.appointments; this.loading=false;
        const id=Number(this.route.snapshot.queryParamMap.get('id'));
        if(id){ const item=this.appointments.find(a=>a.id===id); if(item){ this.editId=id; this.clientId=item.client; this.barberId=item.barber; this.service=item.service; this.status=item.status; this.date=item.dateTime.slice(0,10); this.time=item.dateTime.slice(11,16); } }
        this.changeDetector.markForCheck();
      },
      error: err => { this.loading=false; this.error=err.status===0?'No se pudo conectar con el backend. Inicia los servicios con Docker y vuelve a cargar.':'No se pudieron cargar clientes, barberos y citas.'; this.changeDetector.markForCheck(); }
    });
  }
  get selectedService(){ return this.services.find(s=>s.id===this.service)!; }
  private localDateKey(date:Date){ return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`; }
  clientNameFor(id:number){ const c=this.clients.find(x=>x.id===id); return c?`${c.name} ${c.lastName}`:'Cliente'; }
  barberNameFor(id:number){ const b=this.barbers.find(x=>x.id===id); return b?`${b.name} ${b.lastName}`:'Barbero'; }
  serviceName(id:ServiceType){ return this.services.find(s=>s.id===id)?.name ?? id; }
  statusName(s:AppointmentStatus){ return s==='PENDING'?'Pendiente':s==='DONE'?'Completada':'Cancelada'; }
  save():void {
    this.error=''; this.notice='';
    if(!this.date || !this.time || !this.barberId || (!this.newClient && !this.clientId)){this.error='Completa la fecha, hora, barbero y cliente.';return;}
    const scheduled=new Date(`${this.date}T${this.time}:00`); const now=new Date(); const endMinutes=scheduled.getHours()*60+scheduled.getMinutes()+this.selectedService.duration;
    if(scheduled.getTime()<now.getTime()+30*60*1000){this.error='Agenda con al menos 30 minutos de anticipación.';return;}
    if(scheduled.getTime()>now.getTime()+60*24*60*60*1000){this.error='Solo se pueden agendar citas dentro de los próximos 60 días.';return;}
    if(scheduled.getHours()<8||scheduled.getHours()>=20||endMinutes>20*60){this.error='El horario de atención es de 08:00 a 20:00. Ajusta la hora o el servicio.';return;}
    const persist=(client:number)=>{
      const payload:AppointmentPayload={client,barber:this.barberId!,service:this.service,status:this.status,dateTime:`${this.date}T${this.time}:00`};
      const request=this.editId?this.api.updateAppointment(this.editId,payload):this.api.createAppointment(payload);
      request.subscribe({next:()=>{this.saving=false;this.notice=this.editId?'Los cambios de la cita se guardaron.':'La cita se creó correctamente.';this.editId=null;this.changeDetector.markForCheck();void this.router.navigate(['/home']);},error:e=>{this.saving=false;this.error=e.status===0?'No se pudo conectar con el backend.':(e.error||'No se pudo guardar la cita. Revisa que la fecha sea futura.');this.changeDetector.markForCheck();}});
    };
    this.saving=true;
    if(this.newClient){
      if(!this.clientName.trim()||!this.clientLastName.trim()||!/^\+?[0-9]{10,15}$/.test(this.clientPhone.trim())){this.saving=false;this.error='Captura nombre, apellido y un teléfono válido (10 a 15 dígitos).';return;}
      this.api.createClient({name:this.clientName.trim(),lastName:this.clientLastName.trim(),phone:this.clientPhone.trim()}).subscribe({next:c=>{this.clients=[...this.clients,c];this.changeDetector.markForCheck();persist(c.id);},error:e=>{this.saving=false;this.error=e.error||'No se pudo registrar al cliente.';this.changeDetector.markForCheck();}});
    }else persist(this.clientId!);
  }
  changeStatus(item:Appointment,status:AppointmentStatus):void {
    const payload:AppointmentPayload={barber:item.barber,client:item.client,service:item.service,status,dateTime:item.dateTime};
    this.api.updateAppointment(item.id,payload).subscribe({next:updated=>{this.appointments=this.appointments.map(a=>a.id===item.id?updated:a);this.notice='Estado actualizado.';this.changeDetector.markForCheck();},error:()=>{this.error='No se pudo actualizar el estado de la cita.';this.changeDetector.markForCheck();}});
  }
  cancel(item:Appointment):void {
    if(!confirm(`¿Cancelar la cita de ${this.clientNameFor(item.client)}?`))return;
    const payload:AppointmentPayload={barber:item.barber,client:item.client,service:item.service,status:'CANCELLED',dateTime:item.dateTime};
    this.api.updateAppointment(item.id,payload).subscribe({next:updated=>{this.appointments=this.appointments.map(a=>a.id===updated.id?updated:a);this.notice='La cita se canceló y quedó en el historial.';this.changeDetector.markForCheck();},error:()=>{this.error='No se pudo cancelar la cita.';this.changeDetector.markForCheck();}});
  }
  logout():void {this.api.logout();void this.router.navigate(['/login']);}
}
