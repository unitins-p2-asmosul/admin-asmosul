import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PessoaResumo } from '../../models/pessoa.model';

export interface AlternarEstadoPessoaEvento {
  id: number;
  ativo: boolean;
}

@Component({
  selector: 'app-pessoa-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './pessoa-tabela.component.html',
})
export class PessoaTabelaComponent {
  readonly dados = input.required<PessoaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<AlternarEstadoPessoaEvento>();

  protected readonly colunasExibidas = [
    'indice',
    'acoes',
    'nome',
    'cpf',
    'telefone',
    'tipoPessoa',
    'dataNascimento',
    'email',
    'sexo',
    'escolaridade',
    'profissao',
    'bairro',
    'rendaFamiliar',
    'comorbidades',
    'categorias',
    'quantidadeCoabitantes',
    'ehBeneficiario',
    'ehDoador',
    'status',
  ];

  protected get campoOrdenado(): string {
    return this.ordenacao().split(',')[0] ?? 'nome';
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

  protected aoRolarTabela(evento: WheelEvent): void {
    const tabela = evento.currentTarget as HTMLElement;

    if (tabela.scrollWidth <= tabela.clientWidth || evento.shiftKey) {
      return;
    }

    const deslocamento = Math.abs(evento.deltaY) > Math.abs(evento.deltaX)
      ? evento.deltaY
      : evento.deltaX;

    if (deslocamento === 0) {
      return;
    }

    tabela.scrollLeft += deslocamento;
    evento.preventDefault();
  }
}
