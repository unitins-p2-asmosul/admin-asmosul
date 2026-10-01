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
import { ContaFiltroDialogComponent } from '../../components/conta-filtro-dialog/conta-filtro-dialog.component';
import {
  ContaRedefinirSenhaDialogComponent,
  ContaRedefinirSenhaDialogData,
} from '../../components/conta-redefinir-senha-dialog/conta-redefinir-senha-dialog.component';
import {
  AlternarEstadoContaEvento,
  ContaTabelaComponent,
} from '../../components/conta-tabela/conta-tabela.component';
import {
  ContaConsultaParametros,
  ContaFiltros,
  ContaResumo,
} from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';

const TAMANHO_PAGINA = 10;
const ORDENACAO_PADRAO = 'nomeUsuario,asc';
const PARAMETROS_PADRAO: ContaConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA,
  sort: ORDENACAO_PADRAO,
};
const RESPOSTA_VAZIA = {
  dados: [] as ContaResumo[],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-conta-lista-page',
  standalone: true,
  imports: [PaginaGerenciamentoComponent, ContaTabelaComponent, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './conta-lista-page.component.html',
})
export class ContaListaPageComponent {
  private readonly contaService = inject(ContaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParamMap.pipe(
    map((params): ContaConsultaParametros => ({
      page: params.has('page') ? Number(params.get('page')) : 0,
      size: TAMANHO_PAGINA,
      sort: params.get('sort') || ORDENACAO_PADRAO,
      apenasInativos: params.get('apenasInativos') === 'true' || undefined,
      nomePessoa: params.get('nomePessoa') || undefined,
      nomeUsuario: params.get('nomeUsuario') || undefined,
      email: params.get('email') || undefined,
      perfis: (params.getAll('perfis').filter(Boolean) as ContaFiltros['perfis']) || undefined,
      redefinirSenha: params.has('redefinirSenha')
        ? params.get('redefinirSenha') === 'true'
        : undefined,
      dataCriacao: params.get('dataCriacao') || undefined,
      dataInativo: params.get('dataInativo') || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.contaService.listar(parametros).pipe(
        finalize(() => this.carregando.set(false)),
        catchError(() => of(RESPOSTA_VAZIA)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    const filtros = this.parametrosAtuais();
    return Object.entries(filtros).filter(([chave, valor]) =>
      !['page', 'size', 'sort', 'apenasInativos'].includes(chave)
      && valor !== undefined && valor !== null && valor !== '' && (!Array.isArray(valor) || valor.length > 0),
    ).length;
  }

  protected irParaAdicionar(): void {
    this.router.navigate(['adicionar'], {
      relativeTo: this.route,
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected visualizar(id: number): void {
    this.router.navigate([id], { relativeTo: this.route, queryParams: this.route.snapshot.queryParams });
  }

  protected editar(id: number): void {
    this.router.navigate([id, 'editar'], { relativeTo: this.route, queryParams: this.route.snapshot.queryParams });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.atualizarUrl({ page: evento.pageIndex });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    this.dialog.open<ContaFiltroDialogComponent, ContaFiltros, ContaFiltros>(
      ContaFiltroDialogComponent,
      {
        width: '720px',
        maxWidth: 'calc(100vw - 32px)',
        data: this.parametrosAtuais(),
      },
    ).afterClosed().subscribe((filtros) => {
      if (filtros === undefined) return;
      this.atualizarUrl({
        page: 0,
        nomePessoa: filtros.nomePessoa ?? null,
        nomeUsuario: filtros.nomeUsuario ?? null,
        email: filtros.email ?? null,
        perfis: filtros.perfis?.length ? filtros.perfis : null,
        redefinirSenha: filtros.redefinirSenha === undefined ? null : String(filtros.redefinirSenha),
        dataCriacao: filtros.dataCriacao ?? null,
        dataInativo: filtros.dataInativo ?? null,
        apenasInativos: filtros.dataInativo || this.parametrosAtuais().apenasInativos
          ? 'true'
          : null,
      });
    });
  }

  protected alternarInativos(): void {
    this.atualizarUrl({ page: 0, apenasInativos: this.parametrosAtuais().apenasInativos ? null : 'true' });
  }

  protected limparFiltros(): void {
    this.atualizarUrl({
      page: 0,
      nomePessoa: null,
      nomeUsuario: null,
      email: null,
      perfis: null,
      redefinirSenha: null,
      apenasInativos: null,
      dataCriacao: null,
      dataInativo: null,
    });
  }

  protected async alternarEstado(evento: AlternarEstadoContaEvento): Promise<void> {
    const acao = evento.ativo ? 'reativar' : 'desativar';
    const confirmou = await this.confirmacao.confirmar(
      `${evento.ativo ? 'Reativar' : 'Desativar'} conta`,
      `Tem certeza que deseja ${acao} a conta de "${evento.nomePessoa}"?`,
    );
    if (!confirmou) return;

    const requisicao$ = evento.ativo
      ? this.contaService.reativar(evento.id)
      : this.contaService.desativar(evento.id);
    requisicao$.subscribe({
      next: () => {
        this.notificacao.sucesso(`Conta ${evento.ativo ? 'reativada' : 'desativada'} com sucesso.`);
        this.atualizarUrl({ _refresh: Date.now() });
      },
    });
  }

  protected redefinirSenha(conta: ContaResumo): void {
    const data: ContaRedefinirSenhaDialogData = {
      contaId: conta.id,
      nomePessoa: conta.nomePessoa,
      nomeUsuario: conta.nomeUsuario,
    };
    this.dialog.open<ContaRedefinirSenhaDialogComponent, ContaRedefinirSenhaDialogData, boolean>(
      ContaRedefinirSenhaDialogComponent,
      { width: '480px', maxWidth: 'calc(100vw - 32px)', data },
    ).afterClosed().subscribe((atualizada) => {
      if (atualizada) this.atualizarUrl({ _refresh: Date.now() });
    });
  }

  private atualizarUrl(novosParametros: Record<string, string | number | string[] | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParametros,
      queryParamsHandling: 'merge',
    });
  }
}