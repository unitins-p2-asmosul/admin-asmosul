import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ItemRelacionadoResumo, PessoaResumo, TipoPessoa} from '../../models/pessoa.model';
import { CpfCnpjPipe } from '@features/shared/pipes/cpf-cnpj.pipe';
import { TelefonePipe } from '@features/shared/pipes/telefone.pipe';
import {
  ColunaExportacao,
  SolicitacaoExportacao,
} from '@features/shared/models/exportacao.model';

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
    CpfCnpjPipe,
    TelefonePipe,
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
  readonly aoExportar = output<SolicitacaoExportacao<PessoaResumo>>();

  readonly colunasExportaveis: ColunaExportacao<PessoaResumo>[] = [
    { id: 'indice', titulo: '#', valor: (item) => item.id },
    { id: 'nome', titulo: 'Nome', valor: (item) => item.nome },
    { id: 'cpf', titulo: 'CPF/CNPJ', valor: (item) => item.cpfCnpj },
    { id: 'telefone', titulo: 'Telefone', valor: (item) => item.telefone },
    {
      id: 'tipoPessoa',
      titulo: 'Tipo de pessoa',
      valor: (item) => (this.ehPessoaJuridica(item) ? 'Jurídica' : 'Física'),
    },
    {
      id: 'dataNascimento',
      titulo: 'Data de nascimento',
      valor: (item) => item.dataNascimento?.replace('-', '/') ?? 'Não se aplica',
    },
    { id: 'email', titulo: 'E-mail', valor: (item) => item.email || '-' },
    { id: 'sexo', titulo: 'Sexo', valor: (item) => item.sexo?.descricao || '-' },
    {
      id: 'escolaridade',
      titulo: 'Escolaridade',
      valor: (item) => item.escolaridade?.descricao || '-',
    },
    { id: 'profissao', titulo: 'Profissão', valor: (item) => item.profissao || '-' },
    { id: 'bairro', titulo: 'Bairro', valor: (item) => item.bairro || '-' },
    {
      id: 'rendaFamiliar',
      titulo: 'Renda familiar',
      valor: (item) => item.rendaFamiliar?.descricao || '-',
    },
    {
      id: 'comorbidades',
      titulo: 'Comorbidades',
      valor: (item) => this.nomesRelacionados(item.comorbidades),
    },
    {
      id: 'categorias',
      titulo: 'Categorias',
      valor: (item) => this.nomesRelacionados(item.categorias),
    },
    {
      id: 'quantidadeCoabitantes',
      titulo: 'Quantidade de coabitantes',
      valor: (item) => item.quantidadeCoabitantes ?? 'Não se aplica',
    },
    {
      id: 'ehBeneficiario',
      titulo: 'Beneficiário?',
      valor: (item) =>
        item.ehBeneficiario === undefined ? 'Não se aplica' : item.ehBeneficiario ? 'Sim' : 'Não',
    },
    {
      id: 'ehDoador',
      titulo: 'Doador?',
      valor: (item) => (item.ehDoador === undefined ? 'Não se aplica' : item.ehDoador ? 'Sim' : 'Não'),
    },
    { id: 'status', titulo: 'Status', valor: (item) => (item.ativo ? 'Ativo' : 'Inativo') },
  ];

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

  protected ehPessoaJuridica(item: PessoaResumo): boolean {
    const documento = (item.cpfCnpj ?? '').replace(/\D/g, '');

    return item.tipoPessoa === TipoPessoa.JURIDICA
      || documento.length === 14;
  }

  protected nomesRelacionados(itens?: ItemRelacionadoResumo[]): string {
    if (!itens?.length) {
      return '-';
    }

    return itens.map((item) => item.nome).join(', ');
  }
}
