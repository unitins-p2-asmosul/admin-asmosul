import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ComorbidadeResumo } from '../../models/comorbidade.model';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';

export interface AlternarEstadoEvento {
  id: number;
  ativo: boolean;
}

@Component({
  selector: 'app-comorbidade-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './comorbidade-tabela.component.html',
})
export class ComorbidadeTabelaComponent {
  readonly dados = input.required<ComorbidadeResumo[]>();
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
  readonly aoAlternarEstado = output<AlternarEstadoEvento>();
  readonly aoExportar = output<SolicitacaoExportacao<ComorbidadeResumo>>();

  readonly colunasExportaveis: ColunaExportacao<ComorbidadeResumo>[] = [
    { id: 'id', titulo: '#', valor: (item) => item.id },
    { id: 'nome', titulo: 'Nome', valor: (item) => item.nome },
    { id: 'descricao', titulo: 'Descrição', valor: (item) => item.descricao || '-' },
    { id: 'status', titulo: 'Status', valor: (item) => (item.ativo ? 'Ativo' : 'Desativado') },
  ];

  private readonly colunasExibidasPadrao = ['id', 'acoes', 'nome', 'descricao', 'status'];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((coluna) => coluna.id),
      this.colunasVisiveis(),
    );
  }

  /** Campo e direção atuais, extraídos de "campo,direcao", para o mat-sort refletir a URL. */
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
