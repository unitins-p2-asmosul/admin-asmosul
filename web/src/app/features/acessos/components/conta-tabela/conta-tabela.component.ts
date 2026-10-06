import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ContaResumo } from '../../models/conta.model';

export interface AlternarEstadoContaEvento {
  id: number;
  ativo: boolean;
  nomePessoa: string;
}

@Component({
  selector: 'app-conta-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './conta-tabela.component.html',
})
export class ContaTabelaComponent {
  readonly dados = input.required<ContaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nomeUsuario,asc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoContaEvento>();
  readonly aoRedefinirSenha = output<ContaResumo>();

  protected readonly colunasExibidas = [
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

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nomeUsuario';
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