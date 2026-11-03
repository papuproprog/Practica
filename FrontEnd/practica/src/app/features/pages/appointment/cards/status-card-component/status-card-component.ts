import { Component } from '@angular/core';
import { CardComponent } from '../../../../../shared/ui/card-component/card-component';
import { BadgeComponent } from '../../../../../shared/ui/badge-component/badge-component';

@Component({
  imports: [CardComponent, BadgeComponent],
  selector: 'app-status-card',
  host: { class: 'block' },
  styleUrl: './status-card-component.css',
  templateUrl: './status-card-component.html',
})
export class StatusCardComponent {}
