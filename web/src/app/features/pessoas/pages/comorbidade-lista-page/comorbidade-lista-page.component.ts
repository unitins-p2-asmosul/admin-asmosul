import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
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

const ORDENACAO_PADRAO = 'nome,asc';
const TAMANHO_PAGINA_PADRAO = 10;

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
  imports: [ComorbidadeTabelaComponent, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './comorbidade-lista-page.component.html',
})
export class ComorbidadeListaPageComponent {
  private readonly comorbidadeService = inject(ComorbidadeService);
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
      size: params['size'] ? Number(params['size']) : TAMANHO_PAGINA_PADRAO,
      sort: params['sort'] || ORDENACAO_PADRAO,
      incluirInativos: params['incluirInativos'] === 'true',
      apenasInativos: params['apenasInativos'] === 'true',
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
    const { nome, apenasInativos } = this.parametrosAtuais();
    return Number(!!nome) + Number(!!apenasInativos);
  }

  protected irParaAdicionar(): void {
    this.router.navigate(['/pessoas/comorbidades/adicionar']);
  }

  protected visualizar(id: number): void {
    this.router.navigate(['/pessoas/comorbidades', id]);
  }

  protected editar(id: number): void {
    this.router.navigate(['/pessoas/comorbidades', id, 'editar']);
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex, size: evento.pageSize });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    const { nome, apenasInativos } = this.parametrosAtuais();

    const dialogRef = this.dialog.open<
      ComorbidadeFiltroDialogComponent,
      ComorbidadeFiltros,
      ComorbidadeFiltros
    >(ComorbidadeFiltroDialogComponent, {
      width: '440px',
      data: { nome, apenasInativos },
    });

    dialogRef.afterClosed().subscribe((filtros) => {
      if (filtros === undefined) {
        return;
      }

      // Campos vazios vão como null para o Angular remover a chave da URL.
      this.atualizarUrl({
        page: 0,
        nome: filtros.nome ?? null,
        apenasInativos: filtros.apenasInativos ? 'true' : null,
      });
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

  private atualizarUrl(novosParametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }
}
