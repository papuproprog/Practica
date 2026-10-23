import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-alert',
  host: { class: 'flex items-start gap-3 rounded-xl bg-rose-100 px-4 py-3 text-xs', role: 'alert' },
  templateUrl: './alert-component.html',
})
export class AlertComponent {}
