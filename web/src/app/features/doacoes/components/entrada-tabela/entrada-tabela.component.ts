import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EntradaResumo, formatarDataHoraEntrada } from '../../models/entrada.model';

export interface ExcluirEntradaEvento {
  id: number;
  descricaoItens: string;
}

@Component({
  selector: 'app-entrada-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './entrada-tabela.component.html',
})
export class EntradaTabelaComponent {
  readonly dados = input.required<EntradaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('dataHora,desc');
  readonly siglasUnidade = input<Record<number, string>>({});

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoExcluir = output<ExcluirEntradaEvento>();

  protected readonly colunasExibidas = ['indice', 'acoes', 'itens', 'doador', 'dataHora'];

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

  protected formatarItens(entrada: EntradaResumo): string {
    const itens = entrada.itens ?? [];
    if (itens.length === 0) return '-';

    return itens
      .map((item) => {
        const sigla = this.siglasUnidade()[item.idItem];
        const quantidade = sigla ? `${item.quantidade} ${sigla}` : `${item.quantidade}`;
        return `${quantidade} de ${item.nomeItem}`;
      })
      .join(' · ');
  }

  protected formatarDataHora(entrada: EntradaResumo): string {
    return formatarDataHoraEntrada(entrada.dataHora);
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
