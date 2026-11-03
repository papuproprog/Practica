import { Component } from '@angular/core';
import { CardComponent } from '../../../../../shared/ui/card-component/card-component';
import { CardHeaderComponent } from '../../../../../shared/ui/card-header-component/card-header-component';
import { FormFieldComponent } from '../../../../../shared/ui/form-field-component/form-field-component';
import { FormControlDirective } from '../../../../../shared/ui/form-control-directive';

@Component({
  imports: [CardComponent, CardHeaderComponent, FormFieldComponent, FormControlDirective],
  selector: 'app-service-card',
  host: { class: 'block' },
  styleUrl: './service-card-component.css',
  templateUrl: './service-card-component.html',
})
export class ServiceCardComponent {}
