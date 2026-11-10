import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { ComorbidadeFiltroDialogComponent } from '../../components/comorbidade-filtro-dialog/comorbidade-filtro-dialog.component';
import {
  AlternarEstadoEvento,
  ComorbidadeTabelaComponent,
} from '../../components/comorbidade-tabela/comorbidade-tabela.component';
import {
  ComorbidadeConsultaParametros,
  ComorbidadeFiltros,
  ComorbidadeResumo,
} from '../../models/comorbidade.model';
import { ComorbidadeService } from '../../services/comorbidade.service';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';

const ORDENACAO_PADRAO = 'nome,asc';
const TAMANHO_PAGINA_PADRAO = 20;

const PARAMETROS_PADRAO: ComorbidadeConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA_PADRAO,
  sort: ORDENACAO_PADRAO,
};

const RESPOSTA_VAZIA: RespostaPaginada<ComorbidadeResumo> = {
  dados: [],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA_PADRAO,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-comorbidade-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    ComorbidadeTabelaComponent,
    ControlesExportacaoComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './comorbidade-lista-page.component.html',
})
export class ComorbidadeListaPageComponent {
  readonly idsColunasVisiveis = signal<string[] | null>(null);
  private readonly comorbidadeService = inject(ComorbidadeService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  /** A URL é a fonte da verdade: paginação, ordenação e filtros vêm dos query params. */
  private readonly parametros$ = this.route.queryParams.pipe(
    map((params): ComorbidadeConsultaParametros => ({
      page: params['page'] ? Number(params['page']) : 0,
      size: TAMANHO_PAGINA_PADRAO,
      sort: params['sort'] || ORDENACAO_PADRAO,
      incluirInativos: params['incluirInativos'] === 'true',
      nome: params['nome'] || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.comorbidadeService.listar(parametros).pipe(
        finalize(() => this.carregando.set(false)),
        catchError(() => of(RESPOSTA_VAZIA)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    return Number(!!this.parametrosAtuais().nome);
  }

  protected irParaAdicionar(): void {
    this.router.navigate(['/pessoas/comorbidades/adicionar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected visualizar(id: number): void {
    this.router.navigate(['/pessoas/comorbidades', id], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected editar(id: number): void {
    this.router.navigate(['/pessoas/comorbidades', id, 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex, size: TAMANHO_PAGINA_PADRAO });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    const { nome } = this.parametrosAtuais();

    const dialogRef = this.dialog.open<
      ComorbidadeFiltroDialogComponent,
      ComorbidadeFiltros,
      ComorbidadeFiltros
    >(ComorbidadeFiltroDialogComponent, {
      width: '740px',
      maxWidth: 'calc(100vw - 32px)',
      panelClass: 'comorbidade-filtro-dialog',
      data: { nome },
    });

    dialogRef.afterClosed().subscribe((filtros) => {
      if (filtros === undefined) {
        return;
      }

      // Campos vazios vão como null para o Angular remover a chave da URL.
      this.atualizarUrl({
        page: 0,
        nome: filtros.nome ?? null,
        apenasInativos: null,
      });
    });
  }

  protected alternarInativos(): void {
    this.atualizarUrl({
      page: 0,
      incluirInativos: this.parametrosAtuais().incluirInativos ? null : 'true',
      apenasInativos: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoEvento): Promise<void> {
    const reativar = evento.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Desativar'} comorbidade`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'desativar'} este registro?`,
    );

    if (!confirmou) {
      return;
    }

    const requisicao$ = reativar
      ? this.comorbidadeService.reativar(evento.id)
      : this.comorbidadeService.desativar(evento.id);

    requisicao$.subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Comorbidade ${reativar ? 'reativada' : 'desativada'} com sucesso!`,
        );
        this.atualizarUrl({ _refresh: Date.now() });
      },
    });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<ComorbidadeResumo>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.comorbidadeService.listar(parametros).pipe(map((resposta) => resposta.dados)),
      solicitacao,
      'comorbidades',
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
