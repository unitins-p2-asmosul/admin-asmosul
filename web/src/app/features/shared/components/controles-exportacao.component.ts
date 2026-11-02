import { Component, input, output, signal } from '@angular/core';
import { BotoesExportacaoComponent } from './botoes-exportacao.component';
import { ColunaSeletor, SeletorColunasComponent } from './seletor-colunas.component';
import { FormatoExportacao } from '../models/exportacao.model';

export interface SolicitacaoControlesExportacao {
  formato: FormatoExportacao;
  idsColunas: string[];
}

@Component({
  selector: 'app-controles-exportacao',
  standalone: true,
  imports: [BotoesExportacaoComponent, SeletorColunasComponent],
  templateUrl: './controles-exportacao.component.html',
})
export class ControlesExportacaoComponent {
  readonly colunas = input.required<ColunaSeletor[]>();
  readonly chaveStorage = input.required<string>();
  readonly aoExportar = output<SolicitacaoControlesExportacao>();
  readonly aoMudarColunas = output<string[]>();

  private readonly idsSelecionados = signal<string[]>([]);

  protected selecionarColunas(ids: string[]): void {
    this.idsSelecionados.set(ids);
    this.aoMudarColunas.emit(ids);
  }

  protected solicitarExportacao(formato: FormatoExportacao): void {
    this.aoExportar.emit({ formato, idsColunas: this.idsSelecionados() });
  }
}
