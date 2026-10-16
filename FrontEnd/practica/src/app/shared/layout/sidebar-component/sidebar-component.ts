import { Component } from '@angular/core';
import { BrandLogoComponent } from '../../ui/brand-logo-component/brand-logo-component';
import { LocalStatusComponent } from '../../ui/local-status-component/local-status-component';
import { UserMenuComponent } from '../../ui/user-menu-component/user-menu-component';

@Component({
  imports: [BrandLogoComponent, LocalStatusComponent, UserMenuComponent],
  selector: 'app-sidebar',
  host: { class: 'block lg:w-60 lg:shrink-0' },
  styleUrl: './sidebar-component.css',
  templateUrl: './sidebar-component.html',
})
export class SidebarComponent {}
