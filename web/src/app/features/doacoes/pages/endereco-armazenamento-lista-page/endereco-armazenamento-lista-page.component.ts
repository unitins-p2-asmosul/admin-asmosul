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
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { EnderecoArmazenamentoFiltroDialogComponent } from '../../components/endereco-armazenamento-filtro-dialog/endereco-armazenamento-filtro-dialog.component';
import {
  AlternarEstadoEnderecoArmazenamentoEvento,
  EnderecoArmazenamentoTabelaComponent,
} from '../../components/endereco-armazenamento-tabela/endereco-armazenamento-tabela.component';
import {
  EnderecoArmazenamentoConsultaParametros,
  EnderecoArmazenamentoFiltros,
  EnderecoArmazenamentoResumo,
} from '../../models/endereco-armazenamento.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nome,asc';
const PARAMETROS_PADRAO: EnderecoArmazenamentoConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as EnderecoArmazenamentoResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-endereco-armazenamento-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    EnderecoArmazenamentoTabelaComponent,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './endereco-armazenamento-lista-page.component.html',
})
export class EnderecoArmazenamentoListaPageComponent {
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): EnderecoArmazenamentoConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      apenasInativos: params.get('apenasInativos') === 'true' || undefined,
      nome: params.get('nome') || undefined,
      bairro: params.get('bairro') || undefined,
      logradouro: params.get('logradouro') || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.enderecoService.listar(parametros).pipe(
        catchError((erro: HttpErrorResponse) => {
          this.notificacao.erro(
            this.mensagemErro(erro, 'Não foi possível carregar os endereços de armazenamento.'),
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
    return Number(!!filtros.nome) + Number(!!filtros.bairro) + Number(!!filtros.logradouro);
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
      .open<
        EnderecoArmazenamentoFiltroDialogComponent,
        EnderecoArmazenamentoFiltros,
        EnderecoArmazenamentoFiltros
      >(EnderecoArmazenamentoFiltroDialogComponent, {
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
          bairro: filtros.bairro ?? null,
          logradouro: filtros.logradouro ?? null,
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
      bairro: null,
      logradouro: null,
      apenasInativos: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoEnderecoArmazenamentoEvento): Promise<void> {
    const reativar = evento.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} endereço de armazenamento`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} o endereço "${evento.nome}"?`,
    );
    if (!confirmou) return;

    const operacao = reativar
      ? this.enderecoService.reativar(evento.id)
      : this.enderecoService.desativar(evento.id);
    operacao.subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Endereço de armazenamento ${reativar ? 'reativado' : 'inativado'} com sucesso.`,
        );
        this.atualizarUrl({ _refresh: Date.now() });
      },
      error: (erro: HttpErrorResponse) => {
        this.notificacao.erro(
          this.mensagemErro(
            erro,
            'Não foi possível alterar o status do endereço de armazenamento.',
          ),
        );
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

  private mensagemErro(erro: HttpErrorResponse, padrao: string): string {
    const corpo = erro.error as { detail?: string } | null;
    return corpo?.detail || padrao;
  }
}
