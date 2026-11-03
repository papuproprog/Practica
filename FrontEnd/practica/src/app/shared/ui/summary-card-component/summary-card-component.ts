import { Component } from '@angular/core';
import { CardComponent } from '../card-component/card-component';
import { CardHeaderComponent } from '../card-header-component/card-header-component';
import { BadgeComponent } from '../badge-component/badge-component';
import { DateHighlightComponent } from '../date-highlight-component/date-highlight-component';

@Component({
  imports: [CardComponent, CardHeaderComponent, BadgeComponent, DateHighlightComponent],
  selector: 'app-summary-card',
  host: { class: 'block' },
  templateUrl: './summary-card-component.html',
})
export class SummaryCardComponent {}
