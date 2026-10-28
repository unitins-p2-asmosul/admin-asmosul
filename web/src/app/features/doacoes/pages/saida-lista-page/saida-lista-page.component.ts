import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { SaidaFiltroDialogComponent } from '../../components/saida-filtro-dialog/saida-filtro-dialog.component';
import {
  ExcluirSaidaEvento,
  SaidaTabelaComponent,
} from '../../components/saida-tabela/saida-tabela.component';
import { SaidaConsultaParametros, SaidaFiltros, SaidaResumo } from '../../models/saida.model';
import { SaidaService } from '../../services/saida.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'dataHora,desc';
const PARAMETROS_PADRAO: SaidaConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as SaidaResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-saida-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    SaidaTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSortModule,
  ],
  templateUrl: './saida-lista-page.component.html',
})
export class SaidaListaPageComponent {
  private readonly saidaService = inject(SaidaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): SaidaConsultaParametros => {
      const itensRaw = params.getAll('itensIds');
      const itensIds = itensRaw
        .flatMap((valor) => valor.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);

      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        itensIds: itensIds.length > 0 ? itensIds : undefined,
        recebedorId: params.has('recebedorId') ? Number(params.get('recebedorId')) : undefined,
        quantidade: params.has('quantidade') ? Number(params.get('quantidade')) : undefined,
        data: params.get('data') || undefined,
      };
    }),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.saidaService.listar(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar as saídas.');
          return of(RESPOSTA_VAZIA);
        }),
        finalize(() => this.carregando.set(false)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    const filtros = this.parametrosAtuais();
    return (
      Number(!!filtros.itensIds?.length) +
      Number(filtros.recebedorId !== undefined) +
      Number(filtros.quantidade !== undefined) +
      Number(!!filtros.data)
    );
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
      .open<SaidaFiltroDialogComponent, SaidaFiltros, SaidaFiltros>(SaidaFiltroDialogComponent, {
        width: '720px',
        maxWidth: 'calc(100vw - 32px)',
        data: this.parametrosAtuais(),
      })
      .afterClosed()
      .subscribe((filtros) => {
        if (filtros === undefined) return;
        this.atualizarUrl({
          page: 0,
          itensIds: filtros.itensIds?.length ? filtros.itensIds.join(',') : null,
          recebedorId: filtros.recebedorId ?? null,
          quantidade: filtros.quantidade ?? null,
          data: filtros.data ?? null,
        });
      });
  }

  protected limparFiltros(): void {
    this.atualizarUrl({
      page: 0,
      itensIds: null,
      recebedorId: null,
      quantidade: null,
      data: null,
    });
  }

  protected async excluir(evento: ExcluirSaidaEvento): Promise<void> {
    const confirmou = await this.confirmacao.confirmar(
      'Excluir saída',
      `A exclusão é permanente e estorna as quantidades de volta ao estoque. Deseja excluir a saída de ${evento.recebedor}?`,
    );
    if (!confirmou) return;

    this.saidaService.excluir(evento.id).subscribe({
      next: () => {
        this.notificacao.sucesso('Saída excluída com sucesso.');
        this.atualizarUrl({ _refresh: Date.now() });
      },
      error: () => {
        this.notificacao.erro('Não foi possível excluir a saída.');
      },
    });
  }

  private atualizarUrl(novosParametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }
}
