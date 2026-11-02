import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CategoriaResumo } from '../../models/categoria.model';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';

export interface AlternarEstadoCategoriaEvento {
  id: number;
  ativo: boolean;
}

@Component({
  selector: 'app-categoria-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './categoria-tabela.component.html',
})
export class CategoriaTabelaComponent {
  readonly dados = input.required<CategoriaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoCategoriaEvento>();
  readonly aoExportar = output<SolicitacaoExportacao<CategoriaResumo>>();

  readonly colunasExportaveis: ColunaExportacao<CategoriaResumo>[] = [
    { id: 'id', titulo: '#', valor: (item) => item.id },
    { id: 'nome', titulo: 'Nome', valor: (item) => item.nome },
    { id: 'descricao', titulo: 'Descrição', valor: (item) => item.descricao || '-' },
    { id: 'status', titulo: 'Status', valor: (item) => (item.ativo ? 'Ativo' : 'Desativado') },
  ];

  protected readonly colunasExibidas = ['id', 'acoes', 'nome', 'descricao', 'status'];

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
    return Math.max(1, Math.ceil(this.totalElementos() / 20));
  }

  protected get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, indice) => indice);
  }

  protected mudarPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaAtual()) {
      return;
    }

    this.aoMudarPagina.emit({
      pageIndex: pagina,
      pageSize: 20,
      length: this.totalElementos(),
      previousPageIndex: this.paginaAtual(),
    });
  }
}
