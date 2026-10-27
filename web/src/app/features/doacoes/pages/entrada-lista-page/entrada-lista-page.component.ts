import { Component, inject, OnInit, signal } from '@angular/core';
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
import { EntradaFiltroDialogComponent } from '../../components/entrada-filtro-dialog/entrada-filtro-dialog.component';
import {
  EntradaTabelaComponent,
  ExcluirEntradaEvento,
} from '../../components/entrada-tabela/entrada-tabela.component';
import {
  EntradaConsultaParametros,
  EntradaFiltros,
  EntradaResumo,
} from '../../models/entrada.model';
import { obterSiglaUnidade } from '../../models/item.model';
import { EntradaService } from '../../services/entrada.service';
import { ItemService } from '../../services/item.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'dataHora,desc';
const PARAMETROS_PADRAO: EntradaConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as EntradaResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-entrada-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    EntradaTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSortModule,
  ],
  templateUrl: './entrada-lista-page.component.html',
})
export class EntradaListaPageComponent implements OnInit {
  private readonly entradaService = inject(EntradaService);
  private readonly itemService = inject(ItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);
  protected readonly siglasUnidade = signal<Record<number, string>>({});

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): EntradaConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      itemId: params.has('itemId') ? Number(params.get('itemId')) : undefined,
      doadorId: params.has('doadorId') ? Number(params.get('doadorId')) : undefined,
      enderecoId: params.has('enderecoId') ? Number(params.get('enderecoId')) : undefined,
      quantidade: params.has('quantidade') ? Number(params.get('quantidade')) : undefined,
      data: params.get('data') || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.entradaService.listar(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar as entradas.');
          return of(RESPOSTA_VAZIA);
        }),
        finalize(() => this.carregando.set(false)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  ngOnInit(): void {
    this.itemService.listarTodos(true).subscribe({
      next: (itens) => {
        const siglas: Record<number, string> = {};
        for (const item of itens) {
          siglas[item.id] = obterSiglaUnidade(item.unidadeMedida);
        }
        this.siglasUnidade.set(siglas);
      },
    });
  }

  protected get filtrosAtivos(): number {
    const filtros = this.parametrosAtuais();
    return (
      Number(filtros.itemId !== undefined) +
      Number(filtros.doadorId !== undefined) +
      Number(filtros.enderecoId !== undefined) +
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
      .open<EntradaFiltroDialogComponent, EntradaFiltros, EntradaFiltros>(
        EntradaFiltroDialogComponent,
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
          itemId: filtros.itemId ?? null,
          doadorId: filtros.doadorId ?? null,
          enderecoId: filtros.enderecoId ?? null,
          quantidade: filtros.quantidade ?? null,
          data: filtros.data ?? null,
        });
      });
  }

  protected limparFiltros(): void {
    this.atualizarUrl({
      page: 0,
      itemId: null,
      doadorId: null,
      enderecoId: null,
      quantidade: null,
      data: null,
    });
  }

  protected async excluir(evento: ExcluirEntradaEvento): Promise<void> {
    const confirmou = await this.confirmacao.confirmar(
      'Excluir entrada',
      `A exclusão é permanente e devolve as quantidades ao estoque. Deseja excluir a entrada com ${evento.descricaoItens}?`,
    );
    if (!confirmou) return;

    this.entradaService.excluir(evento.id).subscribe({
      next: () => {
        this.notificacao.sucesso('Entrada excluída com sucesso.');
        this.atualizarUrl({ _refresh: Date.now() });
      },
      error: () => {
        this.notificacao.erro('Não foi possível excluir a entrada.');
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
