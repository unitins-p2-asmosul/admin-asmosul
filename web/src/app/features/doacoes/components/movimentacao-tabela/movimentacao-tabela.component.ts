import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { formatarDataHoraMovimentacao, MovimentacaoResumo } from '../../models/movimentacao.model';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';

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
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoExportar = output<SolicitacaoExportacao<MovimentacaoResumo>>();

  readonly colunasExportaveis: ColunaExportacao<MovimentacaoResumo>[] = [
    { id: 'indice', titulo: '#', valor: (movimentacao) => movimentacao.id },
    { id: 'item', titulo: 'Item', valor: (movimentacao) => movimentacao.nomeItem || '—' },
    {
      id: 'localAntigo',
      titulo: 'Local antigo',
      valor: (movimentacao) => movimentacao.localAntigo || '—',
    },
    {
      id: 'localNovo',
      titulo: 'Local novo',
      valor: (movimentacao) => movimentacao.localNovo || '—',
    },
    { id: 'quantidade', titulo: 'Quantidade', valor: (movimentacao) => movimentacao.quantidade },
    {
      id: 'dataHora',
      titulo: 'Data hora',
      valor: (movimentacao) => formatarDataHoraMovimentacao(movimentacao.dataHora),
    },
  ];

  private readonly colunasExibidasPadrao = [
    'indice',
    'acoes',
    'item',
    'localAntigo',
    'localNovo',
    'quantidade',
    'dataHora',
  ];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((coluna) => coluna.id),
      this.colunasVisiveis(),
    );
  }

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'dataHora';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'asc' ? 'asc' : 'desc';
  }

  protected formatarDataHora(dataHora: string): string {
    return formatarDataHoraMovimentacao(dataHora);
  }

  exportar(solicitacao: { formato: 'xlsx' | 'csv'; idsColunas: string[] }): void {
    const colunas = this.colunasExportaveis.filter((coluna) =>
      solicitacao.idsColunas.includes(coluna.id),
    );
    this.aoExportar.emit({ formato: solicitacao.formato, colunas });
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
