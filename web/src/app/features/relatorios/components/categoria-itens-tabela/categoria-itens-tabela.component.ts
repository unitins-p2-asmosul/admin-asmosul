import { Component, input, output } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';
import { ItemTabelaCategoriaRelatorio } from '../../models/relatorio-doacoes.model';

@Component({
  selector: 'app-categoria-itens-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule],
  templateUrl: './categoria-itens-tabela.component.html',
})
export class CategoriaItensTabelaComponent {
  readonly dados = input.required<ItemTabelaCategoriaRelatorio[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoExportar = output<SolicitacaoExportacao<ItemTabelaCategoriaRelatorio>>();

  readonly colunasExportaveis: ColunaExportacao<ItemTabelaCategoriaRelatorio>[] = [
    { id: 'nomeCategoria', titulo: 'Categoria', valor: (item) => item.nomeCategoria },
    { id: 'descricao', titulo: 'Descrição', valor: (item) => item.descricao || '-' },
    { id: 'itensDistintos', titulo: 'Itens Distintos', valor: (item) => item.itensDistintos },
    { id: 'saldoFisico', titulo: 'Saldo Físico Total', valor: (item) => item.saldoFisico },
  ];

  private readonly colunasExibidasPadrao = [
    'nomeCategoria',
    'descricao',
    'itensDistintos',
    'saldoFisico',
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
    return this.ordenacao().split(',')[0] ?? 'nome';
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
