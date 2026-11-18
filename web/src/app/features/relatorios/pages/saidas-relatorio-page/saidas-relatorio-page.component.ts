import { Component, computed, inject, Signal, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriaItemService } from '@features/doacoes/services/categoria-item.service';
import { EnderecoArmazenamentoService } from '@features/doacoes/services/endereco-armazenamento.service';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CardResumoComponent } from '../../components/card-resumo/card-resumo.component';
import { GraficoBarrasComponent } from '../../components/grafico-barras/grafico-barras.component';
import { RankingCardsComponent } from '../../components/ranking-cards/ranking-cards.component';
import { SaidasTabelasComponent } from '../../components/saidas-tabelas/saidas-tabelas.component';
import { ItemRanking } from '../../models/relatorio-comum.model';
import {
  ItemSaidaDiscriminada,
  ResumoSaidas,
  SaidasConsultaParametros,
} from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'dataHora,desc';
const ANO_ATUAL = new Date().getFullYear();

const RESUMO_INICIAL: ResumoSaidas = {
  totalItensEntregues: 0,
  valorEstimadoEntregue: 0,
  saidasPorMes: [],
  saidasPorAno: [],
  topMaisRecebem: [],
  topMenosRecebem: [],
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as ItemSaidaDiscriminada[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

const PARAMETROS_PADRAO: SaidasConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
  ano: ANO_ATUAL,
};

@Component({
  selector: 'app-saidas-relatorio-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    CardResumoComponent,
    GraficoBarrasComponent,
    RankingCardsComponent,
    ControlesExportacaoComponent,
    SaidasTabelasComponent,
  ],
  templateUrl: './saidas-relatorio-page.component.html',
})
export class SaidasRelatorioPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly doacoesService = inject(RelatorioDoacoesService);
  private readonly categoriaService = inject(CategoriaItemService);
  private readonly localService = inject(EnderecoArmazenamentoService);
  private readonly pessoaService = inject(PessoaService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly carregandoResumo = signal(false);
  protected readonly carregandoTabela = signal(false);
  protected readonly idsColunasVisiveis = signal<string[] | null>(null);

  protected readonly categorias = toSignal(
    this.categoriaService
      .listar({ page: 0, size: 1000 })
      .pipe(map((r) => r?.dados ?? [])),
    { initialValue: [] },
  );

  protected readonly locais = toSignal(
    this.localService
      .listar({ page: 0, size: 1000 })
      .pipe(map((r) => r?.dados ?? [])),
    { initialValue: [] },
  );

  protected readonly beneficiarios = toSignal(
    this.pessoaService
      .listar({ page: 0, size: 1000, ehBeneficiario: true })
      .pipe(map((r) => r?.dados ?? [])),
    { initialValue: [] },
  );

  protected readonly formFiltro = this.fb.group({
    dataInicio: [''],
    dataFim: [''],
    recebedorId: [null as number | null],
    localArmazenamentoId: [null as number | null],
    categoriaItemId: [null as number | null],
    termoBusca: [''],
  });

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): SaidasConsultaParametros => {
      const p: SaidasConsultaParametros = {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        dataInicio: params.get('dataInicio') || undefined,
        dataFim: params.get('dataFim') || undefined,
        recebedorId: params.has('recebedorId') ? Number(params.get('recebedorId')) : undefined,
        localArmazenamentoId: params.has('localArmazenamentoId')
          ? Number(params.get('localArmazenamentoId'))
          : undefined,
        categoriaItemId: params.has('categoriaItemId')
          ? Number(params.get('categoriaItemId'))
          : undefined,
        termoBusca: params.get('termoBusca') || undefined,
        ano: params.has('ano') ? Number(params.get('ano')) : ANO_ATUAL,
      };

      this.formFiltro.patchValue(
        {
          dataInicio: p.dataInicio ?? '',
          dataFim: p.dataFim ?? '',
          recebedorId: p.recebedorId ?? null,
          localArmazenamentoId: p.localArmazenamentoId ?? null,
          categoriaItemId: p.categoriaItemId ?? null,
          termoBusca: p.termoBusca ?? '',
        },
        { emitEvent: false },
      );

      return p;
    }),
  );

  protected readonly parametrosAtuais: Signal<SaidasConsultaParametros> = toSignal(
    this.parametros$,
    { initialValue: PARAMETROS_PADRAO },
  );

  private readonly resumo$ = this.parametros$.pipe(
    tap(() => this.carregandoResumo.set(true)),
    switchMap((parametros) =>
      this.doacoesService.obterResumoSaidas(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar o resumo de saídas.');
          return of(RESUMO_INICIAL);
        }),
        finalize(() => this.carregandoResumo.set(false)),
      ),
    ),
  );

  protected readonly resumo = toSignal(this.resumo$, { initialValue: RESUMO_INICIAL });

  private readonly respostaTabela$ = this.parametros$.pipe(
    tap(() => this.carregandoTabela.set(true)),
    switchMap((parametros) =>
      this.doacoesService.listarSaidasDiscriminadas(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar a listagem de saídas.');
          return of(RESPOSTA_TABELA_VAZIA);
        }),
        finalize(() => this.carregandoTabela.set(false)),
      ),
    ),
  );

  protected readonly respostaTabela = toSignal(this.respostaTabela$, {
    initialValue: RESPOSTA_TABELA_VAZIA,
  });

  protected readonly valorEstimadoFormatado = computed(() =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(
      this.resumo().valorEstimadoEntregue ?? 0,
    ),
  );

  protected readonly rankingMaisRecebem = computed<ItemRanking[]>(() =>
    this.resumo().topMaisRecebem.map((item) => ({
      posicao: item.posicao,
      nome: item.nomeRecebedor,
      quantidade: item.totalItensRecebidos,
      subtitulo: `${item.totalEntregasRecebidas} entrega(s) recebida(s)`,
    })),
  );

  protected readonly rankingMenosRecebem = computed<ItemRanking[]>(() =>
    this.resumo().topMenosRecebem.map((item) => ({
      posicao: item.posicao,
      nome: item.nomeRecebedor,
      quantidade: item.totalItensRecebidos,
      subtitulo: `${item.membrosFamilia ?? 0} membro(s) na família`,
    })),
  );

  protected submeterFiltros(): void {
    const v = this.formFiltro.value;
    this.atualizarUrl({
      page: 0,
      dataInicio: v.dataInicio || null,
      dataFim: v.dataFim || null,
      recebedorId: v.recebedorId ?? null,
      localArmazenamentoId: v.localArmazenamentoId ?? null,
      categoriaItemId: v.categoriaItemId ?? null,
      termoBusca: v.termoBusca?.trim() || null,
    });
  }

  protected limparFiltros(): void {
    this.formFiltro.reset({
      dataInicio: '',
      dataFim: '',
      recebedorId: null,
      localArmazenamentoId: null,
      categoriaItemId: null,
      termoBusca: '',
    });
    this.submeterFiltros();
  }

  protected mudarAnoGrafico(ano: number): void {
    this.atualizarUrl({ ano });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<ItemSaidaDiscriminada>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.doacoesService.listarSaidasDiscriminadas(parametros).pipe(map((r) => r.dados)),
      solicitacao,
      'relatorio-saidas-doacoes',
    );
  }

  private atualizarUrl(novosParametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }
}
