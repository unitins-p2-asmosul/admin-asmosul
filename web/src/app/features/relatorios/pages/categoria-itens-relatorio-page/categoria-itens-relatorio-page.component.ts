import { Component, computed, inject, OnInit, Signal, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
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
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CategoriaItensTabelaComponent } from '../../components/categoria-itens-tabela/categoria-itens-tabela.component';
import { GraficoRoscaComponent } from '../../components/grafico-rosca/grafico-rosca.component';
import { RankingCardsComponent } from '../../components/ranking-cards/ranking-cards.component';
import {
  CategoriaItemRelatorioConsultaParametros,
  ItemTabelaCategoriaRelatorio,
  ResumoCategoriasItens,
} from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';

const RESUMO_INICIAL: ResumoCategoriasItens = {
  totalItensCatalogo: 0,
  top5MaisVariedade: [],
  distribuicaoParticipacao: [],
};

const RESPOSTA_TABELA_VAZIA = {
  dados: [] as ItemTabelaCategoriaRelatorio[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

const PARAMETROS_PADRAO: CategoriaItemRelatorioConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};

@Component({
  selector: 'app-categoria-itens-relatorio-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    RankingCardsComponent,
    GraficoRoscaComponent,
    ControlesExportacaoComponent,
    CategoriaItensTabelaComponent,
  ],
  templateUrl: './categoria-itens-relatorio-page.component.html',
})
export class CategoriaItensRelatorioPageComponent implements OnInit {
  private readonly doacoesService = inject(RelatorioDoacoesService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly resumo = signal<ResumoCategoriasItens>(RESUMO_INICIAL);
  protected readonly carregandoResumo = signal(false);
  protected readonly carregandoTabela = signal(false);
  protected readonly idsColunasVisiveis = signal<string[] | null>(null);

  protected readonly controleBusca = new FormControl('');

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): CategoriaItemRelatorioConsultaParametros => {
      const termo = params.get('termoBusca') || undefined;
      if (this.controleBusca.value !== (termo ?? '')) {
        this.controleBusca.setValue(termo ?? '', { emitEvent: false });
      }
      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        termoBusca: termo,
      };
    }),
  );

  protected readonly parametrosAtuais: Signal<CategoriaItemRelatorioConsultaParametros> = toSignal(
    this.parametros$,
    { initialValue: PARAMETROS_PADRAO },
  );

  private readonly respostaTabela$ = this.parametros$.pipe(
    tap(() => this.carregandoTabela.set(true)),
    switchMap((parametros) =>
      this.doacoesService.listarTabelaCategorias(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar a tabela de categorias.');
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
      .obterResumoCategoriasItens()
      .pipe(finalize(() => this.carregandoResumo.set(false)))
      .subscribe({
        next: (dados) => this.resumo.set(dados),
        error: () => this.notificacao.erro('Não foi possível carregar o resumo de categorias.'),
      });
  }

  protected aplicarBusca(): void {
    const valor = this.controleBusca.value?.trim();
    this.atualizarUrl({
      page: 0,
      termoBusca: valor ? valor : null,
    });
  }

  protected limparBusca(): void {
    this.controleBusca.setValue('');
    this.aplicarBusca();
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<ItemTabelaCategoriaRelatorio>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.doacoesService.listarTabelaCategorias(parametros).pipe(map((res) => res.dados)),
      solicitacao,
      'relatorio-categorias-itens',
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
