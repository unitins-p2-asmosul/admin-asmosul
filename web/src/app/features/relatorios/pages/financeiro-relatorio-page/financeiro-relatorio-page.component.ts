import { Component, computed, inject, Signal, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, distinctUntilChanged, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CardResumoComponent } from '../../components/card-resumo/card-resumo.component';
import { FinanceiroTabelaComponent } from '../../components/financeiro-tabela/financeiro-tabela.component';
import {
  FinanceiroConsultaParametros,
  FinanceiroFiltros,
  ItemFinanceiroTabela,
  ResumoBalancoFinanceiro,
} from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'valorTotalFinanceiro,desc';

const BALANCO_INICIAL: ResumoBalancoFinanceiro = {
  somatorioSaidas: 0,
  somatorioEntradas: 0,
  balancoPeriodo: 0,
  statusBalanco: 'Superávit',
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as ItemFinanceiroTabela[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

interface EstadoUrlFinanceiro extends FinanceiroFiltros {
  pageSaidas: number;
  sortSaidas: string;
  pageEntradas: number;
  sortEntradas: string;
}

const ESTADO_PADRAO: EstadoUrlFinanceiro = {
  pageSaidas: 0,
  sortSaidas: ORDENACAO_PADRAO,
  pageEntradas: 0,
  sortEntradas: ORDENACAO_PADRAO,
};

@Component({
  selector: 'app-financeiro-relatorio-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    CardResumoComponent,
    ControlesExportacaoComponent,
    FinanceiroTabelaComponent,
  ],
  templateUrl: './financeiro-relatorio-page.component.html',
})
export class FinanceiroRelatorioPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly doacoesService = inject(RelatorioDoacoesService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly carregandoBalanco = signal(false);
  protected readonly carregandoSaidas = signal(false);
  protected readonly carregandoEntradas = signal(false);

  protected readonly idsColunasSaidas = signal<string[] | null>(null);
  protected readonly idsColunasEntradas = signal<string[] | null>(null);

  protected readonly formFiltro = this.fb.group({
    dataInicio: [''],
    dataFim: [''],
    termoItem: [''],
    termoCategoria: [''],
  });

  private readonly estadoUrl$ = this.route.queryParamMap.pipe(
    map((params): EstadoUrlFinanceiro => {
      const estado: EstadoUrlFinanceiro = {
        dataInicio: params.get('dataInicio') || undefined,
        dataFim: params.get('dataFim') || undefined,
        termoItem: params.get('termoItem') || undefined,
        termoCategoria: params.get('termoCategoria') || undefined,
        pageSaidas: params.has('pageSaidas') ? Number(params.get('pageSaidas')) : 0,
        sortSaidas: params.get('sortSaidas') || ORDENACAO_PADRAO,
        pageEntradas: params.has('pageEntradas') ? Number(params.get('pageEntradas')) : 0,
        sortEntradas: params.get('sortEntradas') || ORDENACAO_PADRAO,
      };

      this.formFiltro.patchValue(
        {
          dataInicio: estado.dataInicio ?? '',
          dataFim: estado.dataFim ?? '',
          termoItem: estado.termoItem ?? '',
          termoCategoria: estado.termoCategoria ?? '',
        },
        { emitEvent: false },
      );

      return estado;
    }),
  );

  protected readonly estadoAtual: Signal<EstadoUrlFinanceiro> = toSignal(this.estadoUrl$, {
    initialValue: ESTADO_PADRAO,
  });

  private readonly balanco$ = this.estadoUrl$.pipe(
    map(
      (e): FinanceiroFiltros => ({
        dataInicio: e.dataInicio,
        dataFim: e.dataFim,
        termoItem: e.termoItem,
        termoCategoria: e.termoCategoria,
      }),
    ),
    distinctUntilChanged(
      (a, b) =>
        a.dataInicio === b.dataInicio &&
        a.dataFim === b.dataFim &&
        a.termoItem === b.termoItem &&
        a.termoCategoria === b.termoCategoria,
    ),
    tap(() => this.carregandoBalanco.set(true)),
    switchMap((filtros) =>
      this.doacoesService.obterBalancoFinanceiro(filtros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar o balanço financeiro.');
          return of(BALANCO_INICIAL);
        }),
        finalize(() => this.carregandoBalanco.set(false)),
      ),
    ),
  );

  protected readonly balanco = toSignal(this.balanco$, { initialValue: BALANCO_INICIAL });

  private readonly parametrosSaidas$ = this.estadoUrl$.pipe(
    map(
      (e): FinanceiroConsultaParametros => ({
        page: e.pageSaidas,
        size: TAMANHO_PAGINA,
        sort: e.sortSaidas,
        dataInicio: e.dataInicio,
        dataFim: e.dataFim,
        termoItem: e.termoItem,
        termoCategoria: e.termoCategoria,
      }),
    ),
    distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
  );

  private readonly respostaSaidas$ = this.parametrosSaidas$.pipe(
    tap(() => this.carregandoSaidas.set(true)),
    switchMap((params) =>
      this.doacoesService.listarFinanceiroSaidas(params).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar a tabela financeira de saídas.');
          return of(RESPOSTA_TABELA_VAZIA);
        }),
        finalize(() => this.carregandoSaidas.set(false)),
      ),
    ),
  );

  protected readonly respostaSaidas = toSignal(this.respostaSaidas$, {
    initialValue: RESPOSTA_TABELA_VAZIA,
  });

  private readonly parametrosEntradas$ = this.estadoUrl$.pipe(
    map(
      (e): FinanceiroConsultaParametros => ({
        page: e.pageEntradas,
        size: TAMANHO_PAGINA,
        sort: e.sortEntradas,
        dataInicio: e.dataInicio,
        dataFim: e.dataFim,
        termoItem: e.termoItem,
        termoCategoria: e.termoCategoria,
      }),
    ),
    distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
  );

  private readonly respostaEntradas$ = this.parametrosEntradas$.pipe(
    tap(() => this.carregandoEntradas.set(true)),
    switchMap((params) =>
      this.doacoesService.listarFinanceiroEntradas(params).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar a tabela financeira de entradas.');
          return of(RESPOSTA_TABELA_VAZIA);
        }),
        finalize(() => this.carregandoEntradas.set(false)),
      ),
    ),
  );

  protected readonly respostaEntradas = toSignal(this.respostaEntradas$, {
    initialValue: RESPOSTA_TABELA_VAZIA,
  });

  protected readonly saidasFormatado = computed(() =>
    this.formatarMoeda(this.balanco().somatorioSaidas),
  );

  protected readonly entradasFormatado = computed(() =>
    this.formatarMoeda(this.balanco().somatorioEntradas),
  );

  protected readonly balancoFormatado = computed(() => {
    const valor = this.formatarMoeda(this.balanco().balancoPeriodo);
    const status = this.balanco().statusBalanco;
    return status ? `${valor} (${status})` : valor;
  });

  protected submeterFiltros(): void {
    const v = this.formFiltro.value;
    this.atualizarUrl({
      pageSaidas: 0,
      pageEntradas: 0,
      dataInicio: v.dataInicio || null,
      dataFim: v.dataFim || null,
      termoItem: v.termoItem?.trim() || null,
      termoCategoria: v.termoCategoria?.trim() || null,
    });
  }

  protected limparFiltros(): void {
    this.formFiltro.reset({
      dataInicio: '',
      dataFim: '',
      termoItem: '',
      termoCategoria: '',
    });
    this.submeterFiltros();
  }

  protected mudarPaginaSaidas(evento: PageEvent): void {
    this.atualizarUrl({ pageSaidas: evento.pageIndex });
  }

  protected mudarOrdemSaidas(evento: Sort): void {
    const sortSaidas = evento.active
      ? `${evento.active},${evento.direction || 'desc'}`
      : ORDENACAO_PADRAO;
    this.atualizarUrl({ pageSaidas: 0, sortSaidas });
  }

  protected mudarPaginaEntradas(evento: PageEvent): void {
    this.atualizarUrl({ pageEntradas: evento.pageIndex });
  }

  protected mudarOrdemEntradas(evento: Sort): void {
    const sortEntradas = evento.active
      ? `${evento.active},${evento.direction || 'desc'}`
      : ORDENACAO_PADRAO;
    this.atualizarUrl({ pageEntradas: 0, sortEntradas });
  }

  protected exportarSaidas(solicitacao: SolicitacaoExportacao<ItemFinanceiroTabela>): void {
    const e = this.estadoAtual();
    const parametros: FinanceiroConsultaParametros = {
      page: 0,
      size: 99999,
      sort: e.sortSaidas,
      dataInicio: e.dataInicio,
      dataFim: e.dataFim,
      termoItem: e.termoItem,
      termoCategoria: e.termoCategoria,
    };
    this.exportacao.exportarDados(
      this.doacoesService.listarFinanceiroSaidas(parametros).pipe(map((r) => r.dados)),
      solicitacao,
      'relatorio-financeiro-saidas',
    );
  }

  protected exportarEntradas(solicitacao: SolicitacaoExportacao<ItemFinanceiroTabela>): void {
    const e = this.estadoAtual();
    const parametros: FinanceiroConsultaParametros = {
      page: 0,
      size: 99999,
      sort: e.sortEntradas,
      dataInicio: e.dataInicio,
      dataFim: e.dataFim,
      termoItem: e.termoItem,
      termoCategoria: e.termoCategoria,
    };
    this.exportacao.exportarDados(
      this.doacoesService.listarFinanceiroEntradas(parametros).pipe(map((r) => r.dados)),
      solicitacao,
      'relatorio-financeiro-entradas',
    );
  }

  private formatarMoeda(valor?: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(valor ?? 0);
  }

  private atualizarUrl(novosParametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }
}
