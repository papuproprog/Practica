import { Component } from '@angular/core';
import { RoundedImageComponent } from '../rounded-image-component/rounded-image-component';

@Component({
  imports: [RoundedImageComponent],
  selector: 'app-user-menu',
  host: { class: 'flex items-center gap-3' },
  templateUrl: './user-menu-component.html',
})
export class UserMenuComponent {}
