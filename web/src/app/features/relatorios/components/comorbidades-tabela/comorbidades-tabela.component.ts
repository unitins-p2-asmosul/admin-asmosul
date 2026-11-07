import {Component, input, output} from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { CpfCnpjPipe } from '@features/shared/pipes/cpf-cnpj.pipe';
import { TelefonePipe } from '@features/shared/pipes/telefone.pipe';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';
import { PessoaComorbidadeItem } from '../../models/relatorio-pessoas.model';

@Component({
  selector: 'app-comorbidades-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatTooltipModule,
    CpfCnpjPipe,
    TelefonePipe,
  ],
  templateUrl: './comorbidades-tabela.component.html',
})
export class ComorbidadesTabelaComponent {
  readonly dados = input.required<PessoaComorbidadeItem[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('nome,asc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoExportar = output<SolicitacaoExportacao<PessoaComorbidadeItem>>();

  readonly colunasExportaveis: ColunaExportacao<PessoaComorbidadeItem>[] = [
    { id: 'nome', titulo: 'Pessoa Assistida', valor: (item) => item.nome },
    { id: 'cpf', titulo: 'CPF', valor: (item) => item.cpf || '-' },
    { id: 'contato', titulo: 'Contato', valor: (item) => item.contato || '-' },
    { id: 'genero', titulo: 'Gênero', valor: (item) => item.genero || '-' },
    {
      id: 'comorbidades',
      titulo: 'Comorbidades Diagnosticadas',
      valor: (item) => item.comorbidadesDiagnosticadas?.join(', ') || '-',
    },
  ];

  private readonly colunasExibidasPadrao = [
    'indice',
    'nome',
    'cpf',
    'contato',
    'genero',
    'comorbidades',
  ];

  protected get colunasExibidas(): string[] {
    return filtrarColunasExibidas(
      this.colunasExibidasPadrao,
      this.colunasExportaveis.map((c) => c.id),
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
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
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
