import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card-resumo',
  standalone: true,
  templateUrl: './card-resumo.component.html',
})
export class CardResumoComponent {
  readonly titulo = input.required<string>();
  readonly valor = input.required<number | string>();
}
