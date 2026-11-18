import { CurrencyPipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';
import { ItemFinanceiroTabela } from '../../models/relatorio-doacoes.model';

@Component({
  selector: 'app-financeiro-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    CurrencyPipe,
  ],
  templateUrl: './financeiro-tabela.component.html',
})
export class FinanceiroTabelaComponent {
  readonly tipo = input<'saida' | 'entrada'>('saida');
  readonly dados = input.required<ItemFinanceiroTabela[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('valorTotalFinanceiro,desc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoExportar = output<SolicitacaoExportacao<ItemFinanceiroTabela>>();

  protected readonly rotuloItem = computed(() =>
    this.tipo() === 'entrada' ? 'Item Arrecadado' : 'Item Entregue',
  );

  protected readonly rotuloQuantidade = computed(() =>
    this.tipo() === 'entrada' ? 'Qtd. Recebida' : 'Qtd. Entregue',
  );

  protected readonly rotuloValorTotal = computed(() =>
    this.tipo() === 'entrada' ? 'Valor Total Recebido (R$)' : 'Valor Total Gasto (R$)',
  );

  readonly colunasExportaveis: ColunaExportacao<ItemFinanceiroTabela>[] = [
    { id: 'itemNome', titulo: 'Item', valor: (item) => item.itemNome },
    { id: 'categoriaNome', titulo: 'Categoria', valor: (item) => item.categoriaNome },
    {
      id: 'quantidade',
      titulo: 'Quantidade',
      valor: (item) => `${item.quantidade} ${item.unidadeMedida ?? ''}`.trim(),
    },
    {
      id: 'precoUnitarioMedio',
      titulo: 'Preço Unit. Médio (R$)',
      valor: (item) => item.precoUnitarioMedio,
    },
    {
      id: 'valorTotalFinanceiro',
      titulo: 'Valor Total (R$)',
      valor: (item) => item.valorTotalFinanceiro,
    },
  ];

  private readonly colunasExibidasPadrao = [
    'itemNome',
    'categoriaNome',
    'quantidade',
    'precoUnitarioMedio',
    'valorTotalFinanceiro',
  ];

  protected get colunasExibidas(): string[] {
    const selecionadas = this.colunasVisiveis();
    if (!selecionadas || selecionadas.length === 0) {
      return this.colunasExibidasPadrao;
    }
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((c) => c.id),
      selecionadas,
    );
  }

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'valorTotalFinanceiro';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'asc' ? 'asc' : 'desc';
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

