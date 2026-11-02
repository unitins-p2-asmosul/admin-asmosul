import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CategoriaItemResumo } from '../../models/categoria-item.model';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';

export interface AlternarEstadoCategoriaItemEvento {
  id: number;
  ativo: boolean;
}

@Component({
  selector: 'app-categoria-item-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './categoria-item-tabela.component.html',
})
export class CategoriaItemTabelaComponent {
  readonly dados = input.required<CategoriaItemResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoCategoriaItemEvento>();
  readonly aoExportar = output<SolicitacaoExportacao<CategoriaItemResumo>>();

  readonly colunasExportaveis: ColunaExportacao<CategoriaItemResumo>[] = [
    { id: 'nome', titulo: 'Nome', valor: (categoria) => categoria.nome },
    { id: 'descricao', titulo: 'Descrição', valor: (categoria) => categoria.descricao || '-' },
    {
      id: 'status',
      titulo: 'Status',
      valor: (categoria) => (categoria.ativo ? 'Ativo' : 'Inativo'),
    },
  ];

  private readonly colunasExibidasPadrao = ['indice', 'acoes', 'nome', 'descricao', 'status'];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((coluna) => coluna.id),
      this.colunasVisiveis(),
    );
  }

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nome';
  }

  protected get direcaoOrdenada(): 'asc' | 'desc' {
    return this.ordenacao().split(',')[1] === 'desc' ? 'desc' : 'asc';
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
