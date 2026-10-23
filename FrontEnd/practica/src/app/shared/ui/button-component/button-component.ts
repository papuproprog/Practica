import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'button[app-button]',
  host: {
    class:
      'inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm transition ' +
      'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#00bfe8]',
  },
  templateUrl: './button-component.html',
})
export class ButtonComponent {}
