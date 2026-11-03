import { Directive } from '@angular/core';

@Directive({
  selector: 'input[appControl], select[appControl], textarea[appControl]',
  host: {
    class:
      'w-full rounded-xl border border-stone-200 bg-white px-3.5 py-3 text-sm text-[#0b1437] outline-none transition ' +
      'placeholder:text-stone-400 focus:border-[#00bfe8] focus:ring-2 focus:ring-[#00bfe8]/20',
  },
})
export class FormControlDirective {}
