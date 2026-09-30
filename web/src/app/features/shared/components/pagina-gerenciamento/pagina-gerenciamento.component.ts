import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-pagina-gerenciamento',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './pagina-gerenciamento.component.html',
})
export class PaginaGerenciamentoComponent {
  readonly titulo = input.required<string>();
  readonly icone = input.required<string>();
  readonly iconeAdicionar = input('add_circle_outline');
  readonly rotuloAdicionar = input.required<string>();
  readonly adicionarNaToolbar = input(false);
  readonly filtrosAtivos = input(0);

  readonly aoAdicionar = output<void>();
  readonly aoFiltrar = output<void>();
}
