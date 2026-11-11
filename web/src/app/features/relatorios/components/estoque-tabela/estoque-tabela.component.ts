import { CurrencyPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';
import { ItemInventarioEstoque } from '../../models/relatorio-doacoes.model';

@Component({
  selector: 'app-estoque-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, CurrencyPipe],
  templateUrl: './estoque-tabela.component.html',
})
export class EstoqueTabelaComponent {
  readonly dados = input.required<ItemInventarioEstoque[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nomeItem,asc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoExportar = output<SolicitacaoExportacao<ItemInventarioEstoque>>();

  readonly colunasExportaveis: ColunaExportacao<ItemInventarioEstoque>[] = [
    { id: 'nomeItem', titulo: 'Item', valor: (item) => item.nomeItem },
    { id: 'nomeCategoria', titulo: 'Categoria', valor: (item) => item.nomeCategoria },
    { id: 'localArmazenamento', titulo: 'Local de Armazenamento', valor: (item) => item.localArmazenamento },
    {
      id: 'quantidade',
      titulo: 'Quantidade',
      valor: (item) => `${item.quantidade} ${item.unidadeMedida ?? ''}`.trim(),
    },
    { id: 'valorUnitarioEstimado', titulo: 'Valor Unitário Est. (R$)', valor: (item) => item.valorUnitarioEstimado },
    { id: 'valorTotalEstimado', titulo: 'Valor Total Est. (R$)', valor: (item) => item.valorTotalEstimado },
  ];

  private readonly colunasExibidasPadrao = [
    'nomeItem',
    'nomeCategoria',
    'localArmazenamento',
    'quantidade',
    'valorUnitarioEstimado',
    'valorTotalEstimado',
  ];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((c) => c.id),
      this.colunasVisiveis(),
    );
  }

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nomeItem';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'desc' ? 'desc' : 'asc';
  }

  exportar(solicitacao: { formato: 'xlsx' | 'csv'; idsColunas: string[] }): void {
    const colunas = this.colunasExportaveis.filter((c) => solicitacao.idsColunas.includes(c.id));
    this.aoExportar.emit({ formato: solicitacao.formato, colunas });
  }

  protected get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.totalElementos() / this.tamanhoPagina()));
  }

  protected get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }

  protected mudarPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaAtual()) {
      return;
    }

    this.aoMudarPagina.emit({
      pageIndex: pagina,
      pageSize: this.tamanhoPagina(),
      length: this.totalElementos(),
      previousPageIndex: this.paginaAtual(),
    });
  }
}
