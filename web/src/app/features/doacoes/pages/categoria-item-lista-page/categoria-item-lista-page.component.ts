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
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CategoriaItemFiltroDialogComponent } from '../../components/categoria-item-filtro-dialog/categoria-item-filtro-dialog.component';
import {
  AlternarEstadoCategoriaItemEvento,
  CategoriaItemTabelaComponent,
} from '../../components/categoria-item-tabela/categoria-item-tabela.component';
import {
  CategoriaItemConsultaParametros,
  CategoriaItemFiltros,
  CategoriaItemResumo,
} from '../../models/categoria-item.model';
import { CategoriaItemService } from '../../services/categoria-item.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';
const PARAMETROS_PADRAO: CategoriaItemConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as CategoriaItemResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-categoria-item-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    CategoriaItemTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './categoria-item-lista-page.component.html',
})
export class CategoriaItemListaPageComponent {
  private readonly categoriaItemService = inject(CategoriaItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): CategoriaItemConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      apenasInativos: params.get('apenasInativos') === 'true' || undefined,
      nome: params.get('nome') || undefined,
      descricao: params.get('descricao') || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.categoriaItemService.listar(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar as categorias de itens.');
          return of(RESPOSTA_VAZIA);
        }),
        finalize(() => this.carregando.set(false)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    const filtros = this.parametrosAtuais();
    return Number(!!filtros.nome) + Number(!!filtros.descricao);
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
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    this.dialog
      .open<CategoriaItemFiltroDialogComponent, CategoriaItemFiltros, CategoriaItemFiltros>(
        CategoriaItemFiltroDialogComponent,
        {
          width: '720px',
          maxWidth: 'calc(100vw - 32px)',
          data: this.parametrosAtuais(),
        },
      )
      .afterClosed()
      .subscribe((filtros) => {
        if (filtros === undefined) return;
        this.atualizarUrl({
          page: 0,
          nome: filtros.nome ?? null,
          descricao: filtros.descricao ?? null,
        });
      });
  }

  protected alternarInativos(): void {
    this.atualizarUrl({
      page: 0,
      apenasInativos: this.parametrosAtuais().apenasInativos ? null : 'true',
    });
  }

  protected limparFiltros(): void {
    this.atualizarUrl({
      page: 0,
      nome: null,
      descricao: null,
      apenasInativos: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoCategoriaItemEvento): Promise<void> {
    const reativar = evento.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} categoria`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} esta categoria de item?`,
    );
    if (!confirmou) return;

    const requisicao = reativar
      ? this.categoriaItemService.reativar(evento.id)
      : this.categoriaItemService.desativar(evento.id);
    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Categoria ${reativar ? 'reativada' : 'inativada'} com sucesso.`);
        this.atualizarUrl({ _refresh: Date.now() });
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
