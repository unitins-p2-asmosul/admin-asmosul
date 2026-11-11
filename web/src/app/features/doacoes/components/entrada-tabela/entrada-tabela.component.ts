import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EntradaResumo, formatarDataHoraEntrada } from '../../models/entrada.model';
import { obterSiglaUnidade } from '../../models/item.model';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';

export interface ExcluirEntradaEvento {
  id: number;
  descricaoItens: string;
}

@Component({
  selector: 'app-entrada-tabela',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './entrada-tabela.component.html',
})
export class EntradaTabelaComponent {
  readonly dados = input.required<EntradaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('dataHora,desc');
  readonly colunasVisiveis = input<string[] | null>(null);
  readonly siglasUnidade = input<Record<number, string>>({});

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoExcluir = output<ExcluirEntradaEvento>();
  readonly aoExportar = output<SolicitacaoExportacao<EntradaResumo>>();

  readonly colunasExportaveis: ColunaExportacao<EntradaResumo>[] = [
    { id: 'indice', titulo: '#', valor: (entrada) => entrada.id },
    { id: 'itens', titulo: 'Itens', valor: (entrada) => this.formatarItens(entrada) },
    {
      id: 'doador',
      titulo: 'Doador',
      valor: (entrada) => entrada.nomeDoador || 'Sem doador vinculado',
    },
    {
      id: 'dataHora',
      titulo: 'Data hora',
      valor: (entrada) => this.formatarDataHora(entrada),
    },
  ];

  private readonly colunasExibidasPadrao = ['indice', 'acoes', 'itens', 'doador', 'dataHora'];

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

  protected get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.totalElementos() / this.tamanhoPagina()));
  }

  protected get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, indice) => indice);
  }

  protected formatarItens(entrada: EntradaResumo): string {
    const itens = entrada.itens ?? [];
    if (itens.length === 0) return '-';

    return itens
      .map((item) => {
        const sigla = item.unidadeMedida
          ? obterSiglaUnidade(item.unidadeMedida)
          : this.siglasUnidade()[item.idItem];
        const quantidade = sigla ? `${item.quantidade} ${sigla}` : `${item.quantidade}`;
        return `${quantidade} de ${item.nomeItem}`;
      })
      .join(' · ');
  }

  protected formatarDataHora(entrada: EntradaResumo): string {
    return formatarDataHoraEntrada(entrada.dataHora);
  }

  exportar(solicitacao: { formato: 'xlsx' | 'csv'; idsColunas: string[] }): void {
    const colunas = this.colunasExportaveis.filter((coluna) =>
      solicitacao.idsColunas.includes(coluna.id),
    );
    this.aoExportar.emit({ formato: solicitacao.formato, colunas });
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
