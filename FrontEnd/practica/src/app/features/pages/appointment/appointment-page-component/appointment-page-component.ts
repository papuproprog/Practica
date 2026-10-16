import { Component } from '@angular/core';
import { SidebarComponent } from '../../../../shared/layout/sidebar-component/sidebar-component';
import { PageHeaderComponent } from '../../../../shared/layout/page-header-component/page-header-component';
import { ScheduleCardComponent } from '../cards/schedule-card-component/schedule-card-component';
import { ClientCardComponent } from '../cards/client-card-component/client-card-component';
import { ServiceCardComponent } from '../cards/service-card-component/service-card-component';
import { SummaryCardComponent } from '../../../../shared/ui/summary-card-component/summary-card-component';
import { StatusCardComponent } from '../cards/status-card-component/status-card-component';
import { ButtonComponent } from '../../../../shared/ui/button-component/button-component';

@Component({
  imports: [
    SidebarComponent, PageHeaderComponent, ScheduleCardComponent, ClientCardComponent, ServiceCardComponent, SummaryCardComponent, StatusCardComponent, ButtonComponent],
  selector: 'app-appointment-page',
  styleUrl: './appointment-page-component.css',
  templateUrl: './appointment-page-component.html',
})
export class AppointmentPageComponent {}
