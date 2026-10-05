import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { PessoaFiltroDialogComponent } from '../../components/pessoa-filtro-dialog/pessoa-filtro-dialog.component';
import { AlternarEstadoPessoaEvento, PessoaTabelaComponent } from '../../components/pessoa-tabela/pessoa-tabela.component';
import { PessoaConsultaParametros, PessoaFiltros, PessoaResumo } from '../../models/pessoa.model';
import { PessoaService } from '../../services/pessoa.service';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';

const ORDENACAO_PADRAO = 'nome,asc';
const TAMANHO_PAGINA_PADRAO = 20;

const PARAMETROS_PADRAO: PessoaConsultaParametros = {
  page: 0,
  size: TAMANHO_PAGINA_PADRAO,
  sort: ORDENACAO_PADRAO,
};

const RESPOSTA_VAZIA: RespostaPaginada<PessoaResumo> = {
  dados: [],
  paginaAtual: 0,
  tamanhoPagina: TAMANHO_PAGINA_PADRAO,
  totalElementos: 0,
  totalPaginas: 0,
};

@Component({
  selector: 'app-pessoa-lista-page',
  standalone: true,
  imports: [PaginaGerenciamentoComponent, PessoaTabelaComponent],
  templateUrl: './pessoa-lista-page.component.html',
})
export class PessoaListaPageComponent {
  private readonly pessoaService = inject(PessoaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);

  private readonly parametros$ = this.route.queryParams.pipe(
    map((params): PessoaConsultaParametros => ({
      page: params['page'] ? Number(params['page']) : 0,
      size: TAMANHO_PAGINA_PADRAO,
      sort: params['sort'] || ORDENACAO_PADRAO,
      incluirInativos: params['incluirInativos'] === 'true',
      apenasInativos: params['apenasInativos'] === 'true',
      nome: params['nome'] || undefined,
      cpfCnpj: params['cpfCnpj'] || undefined,
      tipoPessoa: params['tipoPessoa'] || undefined,
      dataNascimento: params['dataNascimento'] || undefined,
      sexo: params['sexo'] || undefined,
      telefone: params['telefone'] || undefined,
      email: params['email'] || undefined,
      escolaridade: params['escolaridade'] || undefined,
      profissao: params['profissao'] || undefined,
      bairro: params['bairro'] || undefined,
      rendaFamiliar: params['rendaFamiliar'] || undefined,
      comorbidadeId: params['comorbidadeId'] ? Number(params['comorbidadeId']) : undefined,
      categoriaId: params['categoriaId'] ? Number(params['categoriaId']) : undefined,
      quantidadeCoabitantes: params['quantidadeCoabitantes']
        ? Number(params['quantidadeCoabitantes'])
        : undefined,
      ehBeneficiario: params['ehBeneficiario'] === undefined ? undefined : params['ehBeneficiario'] === 'true',
      ehDoador: params['ehDoador'] === undefined ? undefined : params['ehDoador'] === 'true',
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: PARAMETROS_PADRAO,
  });

  private readonly resposta$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((parametros) =>
      this.pessoaService.listar(parametros).pipe(
        finalize(() => this.carregando.set(false)),
        catchError(() => of(RESPOSTA_VAZIA)),
      ),
    ),
  );

  protected readonly resposta = toSignal(this.resposta$, { initialValue: RESPOSTA_VAZIA });

  protected get filtrosAtivos(): number {
    const parametros = this.parametrosAtuais();
    return Object.entries(parametros).filter(([chave, valor]) =>
      !['page', 'size', 'sort', 'incluirInativos'].includes(chave)
      && valor !== undefined
      && valor !== null
      && valor !== false
      && valor !== '',
    ).length;
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
    this.atualizarUrl({ page: evento.pageIndex, size: TAMANHO_PAGINA_PADRAO });
  }

  protected mudarOrdem(evento: Sort): void {
    const sort = evento.active ? `${evento.active},${evento.direction || 'asc'}` : ORDENACAO_PADRAO;
    this.atualizarUrl({ page: 0, sort });
  }

  protected abrirFiltros(): void {
    const parametros = this.parametrosAtuais();

    const dialogRef = this.dialog.open<
      PessoaFiltroDialogComponent,
      PessoaFiltros,
      PessoaFiltros
    >(PessoaFiltroDialogComponent, {
      width: '740px',
      maxWidth: 'calc(100vw - 32px)',
      panelClass: 'pessoa-filtro-dialog',
      data: parametros,
    });

    dialogRef.afterClosed().subscribe((filtros) => {
      if (filtros === undefined) {
        return;
      }

      this.atualizarUrl({
        page: 0,
        nome: filtros.nome ?? null,
        cpfCnpj: filtros.cpfCnpj ?? null,
        tipoPessoa: filtros.tipoPessoa ?? null,
        dataNascimento: filtros.dataNascimento ?? null,
        sexo: filtros.sexo ?? null,
        telefone: filtros.telefone ?? null,
        email: filtros.email ?? null,
        escolaridade: filtros.escolaridade ?? null,
        profissao: filtros.profissao ?? null,
        bairro: filtros.bairro ?? null,
        rendaFamiliar: filtros.rendaFamiliar ?? null,
        comorbidadeId: filtros.comorbidadeId ?? null,
        categoriaId: filtros.categoriaId ?? null,
        quantidadeCoabitantes: filtros.quantidadeCoabitantes ?? null,
        ehBeneficiario: filtros.ehBeneficiario === undefined ? null : String(filtros.ehBeneficiario),
        ehDoador: filtros.ehDoador === undefined ? null : String(filtros.ehDoador),
        apenasInativos: filtros.apenasInativos ? 'true' : null,
      });
    });
  }

  protected async alternarEstado(evento: AlternarEstadoPessoaEvento): Promise<void> {
    const confirmou = await this.confirmacao.confirmar(
      `${evento.ativo ? 'Reativar' : 'Desativar'} pessoa`,
      `Tem certeza que deseja ${evento.ativo ? 'reativar' : 'desativar'} esta pessoa?`,
    );

    if (!confirmou) return;

    const requisicao$ = evento.ativo
      ? this.pessoaService.reativar(evento.id)
      : this.pessoaService.desativar(evento.id);

    requisicao$.subscribe({
      next: () => {
        this.notificacao.sucesso(`Pessoa ${evento.ativo ? 'reativada' : 'desativada'} com sucesso!`);
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
