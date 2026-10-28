import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { formatarDataHoraEntrada } from '../../models/entrada.model';
import { SaidaResumo } from '../../models/saida.model';

export interface ExcluirSaidaEvento {
  id: number;
  recebedor: string;
}

@Component({
  selector: 'app-saida-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './saida-tabela.component.html',
})
export class SaidaTabelaComponent {
  readonly dados = input.required<SaidaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('dataHora,desc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoExcluir = output<ExcluirSaidaEvento>();

  protected readonly colunasExibidas = [
    'indice',
    'acoes',
    'recebedor',
    'itens',
    'quantidadeTotal',
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

  protected formatarItens(saida: SaidaResumo): string {
    const descricoes = saida.itensOuKitsDescricao ?? [];
    return descricoes.length > 0 ? descricoes.join(' · ') : '-';
  }

  protected formatarDataHora(saida: SaidaResumo): string {
    return formatarDataHoraEntrada(saida.dataHora);
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
