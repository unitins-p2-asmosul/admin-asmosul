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
import { ItemFiltroDialogComponent } from '../../components/item-filtro-dialog/item-filtro-dialog.component';
import {
  AlternarEstadoItemEvento,
  ItemTabelaComponent,
} from '../../components/item-tabela/item-tabela.component';
import { ItemConsultaParametros, ItemFiltros, ItemResumo } from '../../models/item.model';
import { ItemService } from '../../services/item.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';
const PARAMETROS_PADRAO: ItemConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as ItemResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-item-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    ItemTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSortModule,
  ],
  templateUrl: './item-lista-page.component.html',
})
export class ItemListaPageComponent {
  private readonly itemService = inject(ItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): ItemConsultaParametros => {
      const categoriasRaw = params.getAll('categoriasIds');
      const valores = categoriasRaw
        .flatMap((v) => v.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);
      const categoriasIds = valores.length > 0 ? valores : undefined;

      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        apenasInativos: params.get('apenasInativos') === 'true' || undefined,
        nome: params.get('nome') || undefined,
        categoriasIds: categoriasIds && categoriasIds.length > 0 ? categoriasIds : undefined,
        estoque: params.has('estoque') ? Number(params.get('estoque')) : undefined,
        precoUnitarioMinimo: params.has('precoUnitarioMinimo')
          ? Number(params.get('precoUnitarioMinimo'))
          : undefined,
        precoUnitarioMaximo: params.has('precoUnitarioMaximo')
          ? Number(params.get('precoUnitarioMaximo'))
          : undefined,
      };
    }),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.itemService.listar(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar os itens.');
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
      Number(!!filtros.nome) +
      Number(!!filtros.categoriasIds?.length) +
      Number(filtros.estoque !== undefined) +
      Number(filtros.precoUnitarioMinimo !== undefined || filtros.precoUnitarioMaximo !== undefined)
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
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    this.dialog
      .open<ItemFiltroDialogComponent, ItemFiltros, ItemFiltros>(ItemFiltroDialogComponent, {
        width: '720px',
        maxWidth: 'calc(100vw - 32px)',
        data: this.parametrosAtuais(),
      })
      .afterClosed()
      .subscribe((filtros) => {
        if (filtros === undefined) return;
        this.atualizarUrl({
          page: 0,
          nome: filtros.nome ?? null,
          categoriasIds: filtros.categoriasIds?.length ? filtros.categoriasIds.join(',') : null,
          estoque: filtros.estoque !== undefined ? filtros.estoque : null,
          precoUnitarioMinimo:
            filtros.precoUnitarioMinimo !== undefined ? filtros.precoUnitarioMinimo : null,
          precoUnitarioMaximo:
            filtros.precoUnitarioMaximo !== undefined ? filtros.precoUnitarioMaximo : null,
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
      categoriasIds: null,
      estoque: null,
      precoUnitarioMinimo: null,
      precoUnitarioMaximo: null,
      apenasInativos: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoItemEvento): Promise<void> {
    const reativar = evento.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} item`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} este item?`,
    );
    if (!confirmou) return;

    const requisicao = reativar
      ? this.itemService.reativar(evento.id)
      : this.itemService.desativar(evento.id);
    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Item ${reativar ? 'reativado' : 'inativado'} com sucesso.`);
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
