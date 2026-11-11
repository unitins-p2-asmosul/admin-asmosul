import {Component, computed, inject, OnInit, Signal, signal} from '@angular/core';
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
import { ComorbidadesFiltroComponent } from '../../components/comorbidades-filtro/comorbidades-filtro.component';
import { ComorbidadesTabelaComponent } from '../../components/comorbidades-tabela/comorbidades-tabela.component';
import { GraficoRoscaComponent } from '../../components/grafico-rosca/grafico-rosca.component';
import { RankingCardsComponent } from '../../components/ranking-cards/ranking-cards.component';
import {
  PessoaComorbidadeConsultaParametros,
  PessoaComorbidadeFiltros,
  PessoaComorbidadeItem,
  ResumoComorbidades,
} from '../../models/relatorio-pessoas.model';
import { RelatorioPessoasService } from '../../services/relatorio-pessoas.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';

const RESUMO_INICIAL: ResumoComorbidades = {
  totalOcorrencias: 0,
  top5MaisFrequentes: [],
  distribuicao: [],
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as PessoaComorbidadeItem[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

const PARAMETROS_PADRAO: PessoaComorbidadeConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};

@Component({
  selector: 'app-comorbidades-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    RankingCardsComponent,
    GraficoRoscaComponent,
    ComorbidadesFiltroComponent,
    ControlesExportacaoComponent,
    ComorbidadesTabelaComponent,
  ],
  templateUrl: './comorbidades-relatorio-page.component.html',
})
export class ComorbidadesRelatorioPageComponent implements OnInit {
  private readonly relatorioService = inject(RelatorioPessoasService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly resumo = signal<ResumoComorbidades>(RESUMO_INICIAL);
  protected readonly carregandoResumo = signal(false);
  protected readonly carregandoTabela = signal(false);
  protected readonly idsColunasVisiveis = signal<string[] | null>(null);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): PessoaComorbidadeConsultaParametros => {
      const idsRaw = params.getAll('comorbidadeIds');
      const comorbidadeIds = idsRaw
        .flatMap((v) => v.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);

      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        termoBusca: params.get('termoBusca') || undefined,
        comorbidadeIds: comorbidadeIds.length > 0 ? comorbidadeIds : undefined,
      };
    }),
  );

  protected readonly parametrosAtuais = toSignal(
    this.parametros$,
    { initialValue: PARAMETROS_PADRAO }
  );

  protected readonly filtrosAtuais = computed<PessoaComorbidadeFiltros>(() => {
    const params = this.parametrosAtuais();
    return {
      termoBusca: params.termoBusca,
      comorbidadeIds: params.comorbidadeIds,
    };
  });



  private readonly respostaTabela$ = this.parametros$.pipe(
    tap(() => this.carregandoTabela.set(true)),
    switchMap((parametros) =>
      this.relatorioService.listarPessoasComorbidades(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar a listagem de comorbidades.');
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
    this.relatorioService
      .obterResumoComorbidades()
      .pipe(finalize(() => this.carregandoResumo.set(false)))
      .subscribe({
        next: (dados) => this.resumo.set(dados),
        error: () => this.notificacao.erro('Não foi possível carregar o resumo de comorbidades.'),
      });
  }

  protected aplicarFiltros(filtros: PessoaComorbidadeFiltros): void {
    this.atualizarUrl({
      page: 0,
      termoBusca: filtros.termoBusca ?? null,
      comorbidadeIds: filtros.comorbidadeIds?.length ? filtros.comorbidadeIds.join(',') : null,
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<PessoaComorbidadeItem>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.relatorioService.listarPessoasComorbidades(parametros).pipe(map((res) => res.dados)),
      solicitacao,
      'relatorio-comorbidades',
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
