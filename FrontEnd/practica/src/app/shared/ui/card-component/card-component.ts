import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-card',
  host: {
    class:
      'block rounded-2xl border border-stone-200/60 bg-white p-5 sm:p-6 ' +
      'shadow-[0_1px_2px_rgba(33,29,26,0.04),0_10px_24px_-10px_rgba(33,29,26,0.14)]',
  },
  templateUrl: './card-component.html',
})
export class CardComponent {}
