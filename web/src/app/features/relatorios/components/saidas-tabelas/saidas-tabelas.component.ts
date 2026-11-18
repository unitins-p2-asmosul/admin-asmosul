import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ColunaExportacao, SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { CpfCnpjPipe } from '@features/shared/pipes/cpf-cnpj.pipe';
import { filtrarColunasExibidas } from '@features/shared/utils/colunas-tabela';
import { ItemSaidaDiscriminada } from '../../models/relatorio-doacoes.model';

@Component({
  selector: 'app-saidas-tabelas',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    CurrencyPipe,
    DatePipe,
    CpfCnpjPipe,
  ],
  templateUrl: './saidas-tabelas.component.html',
})
export class SaidasTabelasComponent {
  readonly dados = input.required<ItemSaidaDiscriminada[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();
  readonly ordenacao = input<string>('dataHora,desc');
  readonly colunasVisiveis = input<string[] | null>(null);

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoExportar = output<SolicitacaoExportacao<ItemSaidaDiscriminada>>();

  readonly colunasExportaveis: ColunaExportacao<ItemSaidaDiscriminada>[] = [
    { id: 'dataHora', titulo: 'Data/Hora', valor: (item) => item.dataHora },
    { id: 'recebedorNome', titulo: 'Beneficiário', valor: (item) => item.recebedorNome },
    { id: 'cpfCnpj', titulo: 'CPF/CNPJ', valor: (item) => item.cpfCnpj },
    { id: 'itemNome', titulo: 'Item', valor: (item) => item.itemNome },
    { id: 'categoriaNome', titulo: 'Categoria', valor: (item) => item.categoriaNome },
    { id: 'localArmazenamento', titulo: 'Local de Origem', valor: (item) => item.localArmazenamento },
    { id: 'quantidade', titulo: 'Quantidade', valor: (item) => item.quantidade },
    { id: 'valorEstimado', titulo: 'Valor Estimado (R$)', valor: (item) => item.valorEstimado },
  ];

  private readonly colunasExibidasPadrao = [
    'dataHora',
    'recebedorNome',
    'cpfCnpj',
    'itemNome',
    'categoriaNome',
    'localArmazenamento',
    'quantidade',
    'valorEstimado',
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
    return this.ordenacao().split(',')[0] ?? 'dataHora';
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
