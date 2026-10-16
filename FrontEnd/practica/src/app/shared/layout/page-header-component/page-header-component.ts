import { Component } from '@angular/core';
import { ButtonComponent } from '../../ui/button-component/button-component';

@Component({
  imports: [ButtonComponent],
  selector: 'app-page-header',
  host: { class: 'flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between' },
  styleUrl: './page-header-component.css',
  templateUrl: './page-header-component.html',
})
export class PageHeaderComponent {}
