import { Component } from '@angular/core';
import { CardComponent } from '../../../../../shared/ui/card-component/card-component';
import { CardHeaderComponent } from '../../../../../shared/ui/card-header-component/card-header-component';
import { FormFieldComponent } from '../../../../../shared/ui/form-field-component/form-field-component';
import { AlertComponent } from '../../../../../shared/ui/alert-component/alert-component';
import { FormControlDirective } from '../../../../../shared/ui/form-control-directive';

@Component({
  imports: [CardComponent, CardHeaderComponent, FormFieldComponent,FormControlDirective, AlertComponent],
  selector: 'app-schedule-card',
  host: { class: 'block' },
  styleUrl: './schedule-card-component.css',
  templateUrl: './schedule-card-component.html',
})
export class ScheduleCardComponent {}
