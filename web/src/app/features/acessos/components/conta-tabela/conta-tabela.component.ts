import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ContaResumo } from '../../models/conta.model';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';

export interface AlternarEstadoContaEvento {
  id: number;
  ativo: boolean;
  nomePessoa: string;
}

@Component({
  selector: 'app-conta-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './conta-tabela.component.html',
})
export class ContaTabelaComponent {
  readonly dados = input.required<ContaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nomeUsuario,asc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoContaEvento>();
  readonly aoRedefinirSenha = output<ContaResumo>();
  readonly aoExportar = output<SolicitacaoExportacao<ContaResumo>>();

  readonly colunasExportaveis: ColunaExportacao<ContaResumo>[] = [
    { id: 'nomePessoa', titulo: 'Nome da pessoa associada', valor: (conta) => conta.nomePessoa },
    { id: 'nomeUsuario', titulo: 'Nome de usuário', valor: (conta) => conta.nomeUsuario },
    { id: 'email', titulo: 'E-mail', valor: (conta) => conta.email || '-' },
    {
      id: 'perfis',
      titulo: 'Perfis',
      valor: (conta) =>
        `${conta.perfis.map((perfil) => perfil.descricao).join(', ')} (${
          conta.redefinirSenha ? 'Senha pendente' : 'Sem pendência'
        })`,
    },
    { id: 'dataCriacao', titulo: 'Data de criação', valor: (conta) => this.formatarData(conta.dataCriacao) },
    { id: 'dataInativo', titulo: 'Data inativo', valor: (conta) => this.formatarDataInativo(conta) },
    { id: 'status', titulo: 'Status', valor: (conta) => (conta.ativo ? 'Ativo' : 'Desativado') },
  ];

  private readonly colunasExibidasPadrao = [
    'indice',
    'acoes',
    'nomePessoa',
    'nomeUsuario',
    'email',
    'perfis',
    'dataCriacao',
    'dataInativo',
    'status',
  ];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((coluna) => coluna.id),
      this.colunasVisiveis(),
    );
  }

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nomeUsuario';
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

  protected formatarData(data?: string): string {
    return data?.replaceAll('-', '/') ?? 'Não se aplica';
  }

  protected formatarDataInativo(conta: ContaResumo): string {
    const data = 'dataInativo' in conta && typeof conta.dataInativo === 'string'
      ? conta.dataInativo
      : undefined;
    return data ? this.formatarData(data) : conta.ativo ? 'Não se aplica' : 'Não disponível';
  }
}