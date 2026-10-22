import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { MovimentacaoFiltroDialogComponent } from '../../components/movimentacao-filtro-dialog/movimentacao-filtro-dialog.component';
import { MovimentacaoTabelaComponent } from '../../components/movimentacao-tabela/movimentacao-tabela.component';
import {
  MovimentacaoConsultaParametros,
  MovimentacaoFiltros,
  MovimentacaoResumo,
} from '../../models/movimentacao.model';
import { MovimentacaoService } from '../../services/movimentacao.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'dataHora,desc';
const PARAMETROS_PADRAO: MovimentacaoConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as MovimentacaoResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-movimentacao-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    MovimentacaoTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './movimentacao-lista-page.component.html',
})
export class MovimentacaoListaPageComponent {
  private readonly movimentacaoService = inject(MovimentacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): MovimentacaoConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      itemId: this.numeroParametro(params.get('itemId')),
      localAntigoId: this.numeroParametro(params.get('localAntigoId')),
      localNovoId: this.numeroParametro(params.get('localNovoId')),
      quantidade: this.numeroParametro(params.get('quantidade')),
      dataHoraInicio: params.get('dataHoraInicio') || undefined,
      dataHoraFim: params.get('dataHoraFim') || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.movimentacaoService.listar(parametros).pipe(
        catchError((erro: HttpErrorResponse) => {
          this.notificacao.erro(
            this.mensagemErro(erro, 'Não foi possível carregar as movimentações de estoque.'),
          );
          return of(RESPOSTA_VAZIA);
        }),
        finalize(() => this.carregando.set(false)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    const filtros = this.parametrosAtuais();
    return [
      filtros.itemId,
      filtros.localAntigoId,
      filtros.localNovoId,
      filtros.quantidade,
      filtros.dataHoraInicio,
      filtros.dataHoraFim,
    ].filter((valor) => valor !== undefined && valor !== null && valor !== '').length;
  }

  protected irParaAdicionar(): void {
    this.router.navigate(['adicionar'], {
      relativeTo: this.route,
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected visualizar(id: number): void {
    this.router.navigate([id], {
      relativeTo: this.route,
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected editar(id: number): void {
    this.router.navigate([id, 'editar'], {
      relativeTo: this.route,
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active
      ? `${evento.active},${evento.direction || 'desc'}`
      : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    this.dialog
      .open<MovimentacaoFiltroDialogComponent, MovimentacaoFiltros, MovimentacaoFiltros>(
        MovimentacaoFiltroDialogComponent,
        {
          width: '760px',
          maxWidth: 'calc(100vw - 32px)',
          data: this.parametrosAtuais(),
        },
      )
      .afterClosed()
      .subscribe((filtros) => {
        if (filtros === undefined) return;
        this.atualizarUrl({
          page: 0,
          itemId: filtros.itemId ?? null,
          localAntigoId: filtros.localAntigoId ?? null,
          localNovoId: filtros.localNovoId ?? null,
          quantidade: filtros.quantidade ?? null,
          dataHoraInicio: filtros.dataHoraInicio ?? null,
          dataHoraFim: filtros.dataHoraFim ?? null,
        });
      });
  }

  protected limparFiltros(): void {
    this.atualizarUrl({
      page: 0,
      itemId: null,
      localAntigoId: null,
      localNovoId: null,
      quantidade: null,
      dataHoraInicio: null,
      dataHoraFim: null,
    });
  }

  private numeroParametro(valor: string | null): number | undefined {
    if (valor === null || valor === '') return undefined;
    const numero = Number(valor);
    return Number.isFinite(numero) ? numero : undefined;
  }

  private atualizarUrl(novosParametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }

  private mensagemErro(erro: HttpErrorResponse, padrao: string): string {
    const corpo = erro.error as { detail?: string } | null;
    return corpo?.detail || padrao;
  }
}
