import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { KitFiltroDialogComponent } from '../../components/kit-filtro-dialog/kit-filtro-dialog.component';
import {
  AlternarEstadoKitEvento,
  KitTabelaComponent,
} from '../../components/kit-tabela/kit-tabela.component';
import { KitConsultaParametros, KitFiltros, KitResumo } from '../../models/kit.model';
import { KitService } from '../../services/kit.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';
const PARAMETROS_PADRAO: KitConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as KitResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-kit-gerenciamento-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    KitTabelaComponent,
    ControlesExportacaoComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSortModule,
  ],
  templateUrl: './kit-gerenciamento-page.component.html',
})
export class KitGerenciamentoPageComponent {
  readonly idsColunasVisiveis = signal<string[] | null>(null);
  private readonly kitService = inject(KitService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): KitConsultaParametros => {
      const itensRaw = params.getAll('itensIds');
      const valores = itensRaw
        .flatMap((v) => v.split(','))
        .map((id) => Number(id.trim()))
        .filter((id) => !isNaN(id) && id > 0);
      const itensIds = valores.length > 0 ? valores : undefined;

      return {
        page: params.has('page') ? Number(params.get('page')) : 0,
        size: TAMANHO_PAGINA,
        sort: params.get('sort') || ORDENACAO_PADRAO,
        apenasInativos: params.get('apenasInativos') === 'true' || undefined,
        nome: params.get('nome') || undefined,
        descricao: params.get('descricao') || undefined,
        itensIds: itensIds && itensIds.length > 0 ? itensIds : undefined,
      };
    }),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.kitService.listar(parametros).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar os kits.');
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
      Number(!!filtros.nome) + Number(!!filtros.descricao) + Number(!!filtros.itensIds?.length)
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
      .open<KitFiltroDialogComponent, KitFiltros, KitFiltros>(KitFiltroDialogComponent, {
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
          descricao: filtros.descricao ?? null,
          itensIds: filtros.itensIds?.length ? filtros.itensIds.join(',') : null,
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
      itensIds: null,
      apenasInativos: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoKitEvento): Promise<void> {
    const reativar = evento.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} kit`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} este kit?`,
    );
    if (!confirmou) return;

    const requisicao = reativar
      ? this.kitService.reativar(evento.id)
      : this.kitService.desativar(evento.id);
    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Kit ${reativar ? 'reativado' : 'inativado'} com sucesso.`);
        this.atualizarUrl({ _refresh: Date.now() });
      },
    });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<KitResumo>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.kitService.listar(parametros).pipe(map((resposta) => resposta.dados)),
      solicitacao,
      'kits',
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
