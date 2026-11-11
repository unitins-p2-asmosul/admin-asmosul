import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, OnInit, Signal, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CardResumoComponent } from '../../components/card-resumo/card-resumo.component';
import { EstoqueFiltroComponent } from '../../components/estoque-filtro/estoque-filtro.component';
import { EstoqueTabelaComponent } from '../../components/estoque-tabela/estoque-tabela.component';
import {
  InventarioEstoqueConsultaParametros,
  InventarioEstoqueFiltros,
  ItemInventarioEstoque,
  ResumoEstoqueGeral,
} from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nomeItem,asc';

const RESUMO_INICIAL: ResumoEstoqueGeral = {
  saldoFisicoGeral: 0,
  valorTotalEstimado: 0,
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as ItemInventarioEstoque[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

const PARAMETROS_PADRAO: InventarioEstoqueConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};

@Component({
  selector: 'app-estoque-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    CardResumoComponent,
    EstoqueFiltroComponent,
    ControlesExportacaoComponent,
    EstoqueTabelaComponent,
  ],
  providers: [CurrencyPipe],
  templateUrl: './estoque-relatorio-page.component.html',
})
export class EstoqueRelatorioPageComponent implements OnInit {
  private readonly doacoesService = inject(RelatorioDoacoesService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly currencyPipe = inject(CurrencyPipe);

  protected readonly resumo = signal<ResumoEstoqueGeral>(RESUMO_INICIAL);
  protected readonly carregandoResumo = signal(false);
  protected readonly carregandoTabela = signal(false);
  protected readonly idsColunasVisiveis = signal<string[] | null>(null);

  protected readonly valorEstimadoFormatado = computed(() =>
    this.currencyPipe.transform(
      this.resumo().valorTotalEstimado || 0,
      'BRL',
      'symbol',
      '1.2-2',
      'pt-BR',
    ) ?? 'R$ 0,00',
  );

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): InventarioEstoqueConsultaParametros => {
      const categorias = params
        .getAll('categoriasIds')
        .flatMap((v) => v.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);

      const locais = params
        .getAll('locaisIds')
        .flatMap((v) => v.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);

      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        nomeItem: params.get('nomeItem') || undefined,
        categoriasIds: categorias.length ? categorias : undefined,
        locaisIds: locais.length ? locais : undefined,
        quantidadeMinima: params.has('quantidadeMinima') ? Number(params.get('quantidadeMinima')) : undefined,
        valorMinimo: params.has('valorMinimo') ? Number(params.get('valorMinimo')) : undefined,
      };
    }),
  );

  protected readonly parametrosAtuais: Signal<InventarioEstoqueConsultaParametros> = toSignal(
    this.parametros$,
    { initialValue: PARAMETROS_PADRAO },
  );

  protected readonly filtrosAtuais = computed<InventarioEstoqueFiltros>(() => {
    const params = this.parametrosAtuais();
    return {
      nomeItem: params.nomeItem,
      categoriasIds: params.categoriasIds,
      locaisIds: params.locaisIds,
      quantidadeMinima: params.quantidadeMinima,
      valorMinimo: params.valorMinimo,
    };
  });

  private readonly respostaTabela$ = this.parametros$.pipe(
    tap(() => this.carregandoTabela.set(true)),
    switchMap((parametros) =>
      this.doacoesService.listarInventarioEstoque(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar o inventário de estoque.');
          return of(RESPOSTA_TABELA_VAZIA);
        }),
        finalize(() => this.carregandoTabela.set(false)),
      ),
    ),
  );

  protected readonly respostaTabela = toSignal(this.respostaTabela$, {
    initialValue: RESPOSTA_TABELA_VAZIA,
  });

  ngOnInit(): void {
    this.carregarTotais();
  }

  private carregarTotais(): void {
    this.carregandoResumo.set(true);
    this.doacoesService
      .obterTotaisEstoqueGeral()
      .pipe(finalize(() => this.carregandoResumo.set(false)))
      .subscribe({
        next: (dados) => this.resumo.set(dados),
        error: () => this.notificacao.erro('Não foi possível carregar os totais de estoque.'),
      });
  }

  protected aplicarFiltros(filtros: InventarioEstoqueFiltros): void {
    this.atualizarUrl({
      page: 0,
      nomeItem: filtros.nomeItem ?? null,
      categoriasIds: filtros.categoriasIds?.length ? filtros.categoriasIds.join(',') : null,
      locaisIds: filtros.locaisIds?.length ? filtros.locaisIds.join(',') : null,
      quantidadeMinima: filtros.quantidadeMinima ?? null,
      valorMinimo: filtros.valorMinimo ?? null,
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<ItemInventarioEstoque>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.doacoesService.listarInventarioEstoque(parametros).pipe(map((res) => res.dados)),
      solicitacao,
      'inventario-estoque-consolidado',
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
