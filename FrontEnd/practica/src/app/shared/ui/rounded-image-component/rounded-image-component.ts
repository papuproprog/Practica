import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-avatar',
  host: {
    class:
      'inline-flex size-9 shrink-0 items-center justify-center overflow-hidden rounded-full ' +
      'bg-[#00bfe8] text-xs font-semibold text-white',
  },
  templateUrl: './rounded-image-component.html',
})
export class RoundedImageComponent {}
