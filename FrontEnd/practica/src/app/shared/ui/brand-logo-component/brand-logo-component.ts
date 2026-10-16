import { Component } from '@angular/core';
import { RoundedImageComponent } from '../rounded-image-component/rounded-image-component';

@Component({
  imports: [RoundedImageComponent],
  selector: 'app-brand-logo',
  host: { class: 'flex items-center gap-3' },
  templateUrl: './brand-logo-component.html',
})
export class BrandLogoComponent {}
