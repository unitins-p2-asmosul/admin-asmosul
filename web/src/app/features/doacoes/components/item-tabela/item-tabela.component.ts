import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  formatarEstoqueComUnidade,
  formatarPrecoUnitario,
  ItemResumo,
} from '../../models/item.model';
import { UNIDADE_MEDIDA_OPCOES } from '../../models/unidade-medida.model';

export interface AlternarEstadoItemEvento {
  id: number;
  ativo: boolean;
}

@Component({
  selector: 'app-item-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './item-tabela.component.html',
})
export class ItemTabelaComponent {
  readonly dados = input.required<ItemResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoItemEvento>();

  protected readonly colunasExibidas = [
    'indice',
    'acoes',
    'nome',
    'categoria',
    'estoque',
    'precoUnitario',
    'unidadeMedida',
    'descricao',
    'status',
  ];

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nome';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'desc' ? 'desc' : 'asc';
  }

  protected get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.totalElementos() / this.tamanhoPagina()));
  }

  protected get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, indice) => indice);
  }

  protected formatarEstoque(item: ItemResumo): string {
    return formatarEstoqueComUnidade(item.estoque ?? 0, item.unidadeMedida);
  }

  protected formatarPreco(item: ItemResumo): string {
    return formatarPrecoUnitario(item.precoUnitario);
  }

  protected obterDescricaoUnidade(item: ItemResumo): string {
    if (!item.unidadeMedida) return '-';
    if (typeof item.unidadeMedida === 'object' && item.unidadeMedida.descricao) {
      return item.unidadeMedida.descricao;
    }
    const codigo =
      typeof item.unidadeMedida === 'string' ? item.unidadeMedida : item.unidadeMedida.codigo;
    const opcao = UNIDADE_MEDIDA_OPCOES.find((op) => op.codigo === codigo);
    return opcao ? opcao.descricao : codigo;
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
