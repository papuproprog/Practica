import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-home-page-component',
  styleUrl: './home-page-component.css',
  templateUrl: './home-page-component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})

export class HomePageComponent {
  contenido = "El perro";
}
