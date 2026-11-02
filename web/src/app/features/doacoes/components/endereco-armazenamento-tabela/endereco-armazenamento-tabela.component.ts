import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';

export interface AlternarEstadoEnderecoArmazenamentoEvento {
  id: number;
  ativo: boolean;
  nome: string;
}

@Component({
  selector: 'app-endereco-armazenamento-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './endereco-armazenamento-tabela.component.html',
})
export class EnderecoArmazenamentoTabelaComponent {
  readonly dados = input.required<EnderecoArmazenamentoResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoEnderecoArmazenamentoEvento>();
  readonly aoExportar = output<SolicitacaoExportacao<EnderecoArmazenamentoResumo>>();

  readonly colunasExportaveis: ColunaExportacao<EnderecoArmazenamentoResumo>[] = [
    { id: 'nome', titulo: 'Nome', valor: (endereco) => endereco.nome },
    { id: 'bairro', titulo: 'Bairro', valor: (endereco) => endereco.bairro },
    { id: 'logradouro', titulo: 'Logradouro', valor: (endereco) => endereco.logradouro },
    { id: 'status', titulo: 'Status', valor: (endereco) => (endereco.ativo ? 'Ativo' : 'Inativo') },
  ];

  protected readonly colunasExibidas = [
    'indice',
    'acoes',
    'nome',
    'bairro',
    'logradouro',
    'status',
  ];

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
