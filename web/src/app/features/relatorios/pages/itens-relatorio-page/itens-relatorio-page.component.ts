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
import { ItensFiltroComponent } from '../../components/itens-filtro/itens-filtro.component';
import { ItensTabelaComponent } from '../../components/itens-tabela/itens-tabela.component';
import { RankingCardsComponent } from '../../components/ranking-cards/ranking-cards.component';
import {
  GiroEstoqueConsultaParametros,
  GiroEstoqueFiltros,
  GiroEstoqueItem,
  ResumoItensGiro,
} from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nomeItem,asc';

const RESUMO_INICIAL: ResumoItensGiro = {
  itensMaisSaem: [],
  itensMenosSaem: [],
  itensMaisEntram: [],
  itensMenosEntram: [],
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as GiroEstoqueItem[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

const PARAMETROS_PADRAO: GiroEstoqueConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};

@Component({
  selector: 'app-itens-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    RankingCardsComponent,
    ItensFiltroComponent,
    ControlesExportacaoComponent,
    ItensTabelaComponent,
  ],
  templateUrl: './itens-relatorio-page.component.html',
})
export class ItensRelatorioPageComponent implements OnInit {
  private readonly doacoesService = inject(RelatorioDoacoesService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly resumo = signal<ResumoItensGiro>(RESUMO_INICIAL);
  protected readonly carregandoResumo = signal(false);
  protected readonly carregandoTabela = signal(false);
  protected readonly idsColunasVisiveis = signal<string[] | null>(null);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): GiroEstoqueConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      nomeItem: params.get('nomeItem') || undefined,
      categoriaId: params.has('categoriaId') ? Number(params.get('categoriaId')) : undefined,
      unidadeMedida: params.get('unidadeMedida') || undefined,
      saldoEstoqueMin: params.has('saldoMin') ? Number(params.get('saldoMin')) : undefined,
      saldoEstoqueMax: params.has('saldoMax') ? Number(params.get('saldoMax')) : undefined,
      totalEntradasMin: params.has('entradasMin') ? Number(params.get('entradasMin')) : undefined,
      totalEntradasMax: params.has('entradasMax') ? Number(params.get('entradasMax')) : undefined,
      totalSaidasMin: params.has('saidasMin') ? Number(params.get('saidasMin')) : undefined,
      totalSaidasMax: params.has('saidasMax') ? Number(params.get('saidasMax')) : undefined,
    })),
  );

  protected readonly parametrosAtuais: Signal<GiroEstoqueConsultaParametros> = toSignal(
    this.parametros$,
    { initialValue: PARAMETROS_PADRAO },
  );

  protected readonly filtrosAtuais = computed<GiroEstoqueFiltros>(() => {
    const params = this.parametrosAtuais();
    return {
      nomeItem: params.nomeItem,
      categoriaId: params.categoriaId,
      unidadeMedida: params.unidadeMedida,
      saldoEstoqueMin: params.saldoEstoqueMin,
      saldoEstoqueMax: params.saldoEstoqueMax,
      totalEntradasMin: params.totalEntradasMin,
      totalEntradasMax: params.totalEntradasMax,
      totalSaidasMin: params.totalSaidasMin,
      totalSaidasMax: params.totalSaidasMax,
    };
  });

  private readonly respostaTabela$ = this.parametros$.pipe(
    tap(() => this.carregandoTabela.set(true)),
    switchMap((parametros) =>
      this.doacoesService.buscarGiroEstoque(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar o relatório de giro de itens.');
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
    this.carregarResumo();
  }

  private carregarResumo(): void {
    this.carregandoResumo.set(true);
    this.doacoesService
      .obterResumoItens()
      .pipe(finalize(() => this.carregandoResumo.set(false)))
      .subscribe({
        next: (dados) => this.resumo.set(dados),
        error: () => this.notificacao.erro('Não foi possível carregar os rankings de movimentação.'),
      });
  }

  protected aplicarFiltros(filtros: GiroEstoqueFiltros): void {
    this.atualizarUrl({
      page: 0,
      nomeItem: filtros.nomeItem ?? null,
      categoriaId: filtros.categoriaId ?? null,
      unidadeMedida: filtros.unidadeMedida ?? null,
      saldoMin: filtros.saldoEstoqueMin ?? null,
      saldoMax: filtros.saldoEstoqueMax ?? null,
      entradasMin: filtros.totalEntradasMin ?? null,
      entradasMax: filtros.totalEntradasMax ?? null,
      saidasMin: filtros.totalSaidasMin ?? null,
      saidasMax: filtros.totalSaidasMax ?? null,
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<GiroEstoqueItem>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.doacoesService.buscarGiroEstoque(parametros).pipe(map((res) => res.dados)),
      solicitacao,
      'relatorio-giro-estoque',
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
