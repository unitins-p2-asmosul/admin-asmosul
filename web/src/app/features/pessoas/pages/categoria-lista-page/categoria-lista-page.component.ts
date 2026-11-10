import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { catchError, finalize, map, of, switchMap, tap } from 'rxjs';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { SolicitacaoExportacao } from '@features/shared/models/exportacao.model';
import {
  CategoriaFiltroData,
  CategoriaFiltroDialogComponent,
} from '../../components/categoria-filtro-dialog/categoria-filtro-dialog.component';
import {
  AlternarEstadoCategoriaEvento,
  CategoriaTabelaComponent,
} from '../../components/categoria-tabela/categoria-tabela.component';
import { CategoriaConsultaParametros, CategoriaResumo } from '../../models/categoria.model';
import { CategoriaService } from '../../services/categoria.service';
import { PaginaGerenciamentoComponent } from '@features/shared/components/pagina-gerenciamento/pagina-gerenciamento.component';
import { ControlesExportacaoComponent } from '@features/shared/components/controles-exportacao.component';

@Component({
  selector: 'app-categoria-lista-page',
  standalone: true,
  imports: [
    PaginaGerenciamentoComponent,
    CategoriaTabelaComponent,
    ControlesExportacaoComponent,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './categoria-lista-page.component.html',
})
export class CategoriaListaPageComponent {
  readonly idsColunasVisiveis = signal<string[] | null>(null);
  private readonly categoriaService = inject(CategoriaService);
  private readonly exportacao = inject(ExportacaoService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal(false);
  private readonly parametros$ = this.route.queryParams.pipe(
    map((params): CategoriaConsultaParametros => ({
      page: params['page'] ? Number(params['page']) : 0,
      size: 20,
      sort: params['sort'] || 'nome,asc',
      incluirInativos: params['incluirInativos'] === 'true',
      nome: params['nome'] || undefined,
    })),
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: {
      page: 0,
      size: 20,
      sort: 'nome,asc',
      incluirInativos: false,
      nome: undefined,
    },
  });
  protected readonly resposta = toSignal(
    this.parametros$.pipe(
      tap(() => this.carregando.set(true)),
      switchMap((parametros) =>
        this.categoriaService.listar(parametros).pipe(
          catchError(() =>
            of({
              dados: [],
              paginaAtual: 0,
              tamanhoPagina: parametros.size,
              totalElementos: 0,
              totalPaginas: 0,
            }),
          ),
          finalize(() => this.carregando.set(false)),
        ),
      ),
    ),
    {
      initialValue: {
        dados: [],
        paginaAtual: 0,
        tamanhoPagina: 20,
        totalElementos: 0,
        totalPaginas: 0,
      },
    },
  );

  protected adicionar(): void {
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

  protected abrirFiltros(): void {
    const parametros = this.parametrosAtuais();
    this.dialog
      .open<CategoriaFiltroDialogComponent, CategoriaFiltroData, CategoriaFiltroData>(
        CategoriaFiltroDialogComponent,
        {
          width: '740px',
          maxWidth: 'calc(100vw - 32px)',
          panelClass: 'categoria-filtro-dialog',
          data: { nome: parametros.nome },
        },
      )
      .afterClosed()
      .subscribe((filtros) => {
        if (filtros) {
          this.atualizarUrl({
            page: 0,
            nome: filtros.nome?.trim() || null,
            apenasDesativados: null,
          });
        }
      });
  }

  protected alternarInativos(): void {
    this.atualizarUrl({
      page: 0,
      incluirInativos: this.parametrosAtuais().incluirInativos ? null : 'true',
      apenasDesativados: null,
    });
  }

  protected irParaPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.resposta().totalPaginas) {
      this.atualizarUrl({ page: pagina });
    }
  }

  protected paginas(): number[] {
    return Array.from({ length: this.resposta().totalPaginas }, (_, indice) => indice);
  }

  protected ordenar(campo: string): void {
    const [campoAtual, direcaoAtual] = (this.parametrosAtuais().sort ?? '').split(',');
    const direcao = campoAtual === campo && direcaoAtual === 'asc' ? 'desc' : 'asc';
    this.atualizarUrl({ page: 0, sort: `${campo},${direcao}` });
  }

  protected async alternarEstado(evento: AlternarEstadoCategoriaEvento): Promise<void> {
    const { id, ativo } = evento;
    const acao = ativo ? 'reativar' : 'desativar';
    const confirmou = await this.confirmacao.confirmar(
      `${ativo ? 'Reativar' : 'Desativar'} categoria`,
      `Deseja realmente ${acao} esta categoria?`,
    );
    if (!confirmou) return;

    const requisicao = ativo
      ? this.categoriaService.reativar(id)
      : this.categoriaService.desativar(id);
    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Categoria ${ativo ? 'reativada' : 'desativada'} com sucesso!`);
        this.atualizarUrl({ _refresh: Date.now() });
      },
    });
  }

  protected exportar(solicitacao: SolicitacaoExportacao<CategoriaResumo>): void {
    const parametros = { ...this.parametrosAtuais(), page: 0, size: 99999 };
    this.exportacao.exportarDados(
      this.categoriaService.listar(parametros).pipe(map((resposta) => resposta.dados)),
      solicitacao,
      'categorias-pessoas',
    );
  }

  private atualizarUrl(parametros: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: parametros,
      queryParamsHandling: 'merge',
    });
  }
}
