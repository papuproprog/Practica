import { Component } from '@angular/core';
import { CardComponent } from '../../../../../shared/ui/card-component/card-component';
import { CardHeaderComponent } from '../../../../../shared/ui/card-header-component/card-header-component';
import { FormFieldComponent } from '../../../../../shared/ui/form-field-component/form-field-component';
import { FormControlDirective } from '../../../../../shared/ui/form-control-directive';

@Component({
  selector: 'app-client-card',
  imports: [CardComponent, CardHeaderComponent, FormFieldComponent, FormControlDirective],
  host: { class: 'block' },
  styleUrl: './client-card-component.css',
  templateUrl: './client-card-component.html',
})
export class ClientCardComponent {}
