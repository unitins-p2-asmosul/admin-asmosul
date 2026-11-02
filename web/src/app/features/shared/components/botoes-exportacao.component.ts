import { Component, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormatoExportacao } from '../models/exportacao.model';

@Component({
  selector: 'app-botoes-exportacao',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './botoes-exportacao.component.html',
})
export class BotoesExportacaoComponent {
  readonly aoExportar = output<FormatoExportacao>();
}
