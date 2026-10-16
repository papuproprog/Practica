import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'img[app-img]',
  host: { class: 'block size-full object-cover' },
  templateUrl: './img-component.html',
})
export class ImgComponent {}
