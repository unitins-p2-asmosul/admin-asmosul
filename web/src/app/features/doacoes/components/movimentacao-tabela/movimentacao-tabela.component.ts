import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MovimentacaoResumo } from '../../models/movimentacao.model';

@Component({
  selector: 'app-movimentacao-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './movimentacao-tabela.component.html',
})
export class MovimentacaoTabelaComponent {
  readonly dados = input.required<MovimentacaoResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('dataHora,desc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();

  protected readonly colunasExibidas = [
    'indice',
    'acoes',
    'item',
    'localAntigo',
    'localNovo',
    'quantidade',
    'dataHora',
  ];

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'dataHora';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'asc' ? 'asc' : 'desc';
  }

  protected get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.totalElementos() / this.tamanhoPagina()));
  }

  protected get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, indice) => indice);
  }

  protected mudarPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaAtual()) return;
    this.aoMudarPagina.emit({
      pageIndex: pagina,
      pageSize: this.tamanhoPagina(),
      length: this.totalElementos(),
      previousPageIndex: this.paginaAtual(),
    });
  }
}
