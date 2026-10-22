import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { catchError, distinctUntilChanged, finalize, of, startWith, switchMap, tap } from 'rxjs';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import { ItemDetalhe, ItemEstoqueLocal, ItemResumo } from '../../models/item.model';
import {
  ItemMovimentacaoRequisicao,
  MovimentacaoDetalhe,
  MovimentacaoRequisicao,
} from '../../models/movimentacao.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { ItemService } from '../../services/item.service';
import { MovimentacaoService } from '../../services/movimentacao.service';

interface ItemIncluido extends ItemMovimentacaoRequisicao {
  nomeItem: string;
  nomeArmazenamentoAntigo: string;
  unidadeMedida: string;
}

@Component({
  selector: 'app-movimentacao-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './movimentacao-cadastro-page.component.html',
})
export class MovimentacaoCadastroPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notificacao = inject(NotificacaoService);
  private readonly movimentacaoService = inject(MovimentacaoService);
  private readonly itemService = inject(ItemService);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly carregando = signal(false);
  protected readonly carregandoItem = signal(false);
  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly enderecos = signal<EnderecoArmazenamentoResumo[]>([]);
  protected readonly itemSelecionado = signal<ItemDetalhe | null>(null);
  protected readonly registro = signal<MovimentacaoDetalhe | null>(null);
  protected readonly itensIncluidos = signal<ItemIncluido[]>([]);
  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());

  protected readonly form = this.fb.group({
    idNovoArmazenamento: [null as number | null, Validators.required],
    idItem: [null as number | null],
    idArmazenamentoAntigo: [null as number | null],
    quantidade: [null as number | null, Validators.min(1)],
    descricao: ['', Validators.maxLength(2000)],
  });

  constructor() {
    this.itemService.listarTodos().subscribe({
      next: (itens) => this.itens.set(itens),
      error: (erro: HttpErrorResponse) =>
        this.notificarErro(erro, 'Não foi possível carregar os itens.'),
    });
    this.enderecoService.listarTodas().subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
      error: (erro: HttpErrorResponse) =>
        this.notificarErro(erro, 'Não foi possível carregar os endereços de armazenamento.'),
    });

    this.form.controls.idItem.valueChanges
      .pipe(
        startWith(this.form.controls.idItem.value),
        distinctUntilChanged(),
        tap(() => {
          this.itemSelecionado.set(null);
          this.form.controls.idArmazenamentoAntigo.reset(null);
          this.form.controls.quantidade.reset(null);
        }),
        switchMap((idItem) => {
          if (idItem === null) return of(null);
          this.carregandoItem.set(true);
          return this.itemService.buscarPorId(idItem).pipe(
            tap({
              next: (item) => this.itemSelecionado.set(item),
            }),
            catchError((erro: HttpErrorResponse) => {
              this.notificarErro(erro, 'Não foi possível carregar os saldos deste item.');
              return of(null);
            }),
            finalize(() => this.carregandoItem.set(false)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe();

    effect(() => {
      const idMovimentacao = this.id();
      const visualizacao = this.visualizacao();
      if (idMovimentacao) this.carregarMovimentacao(Number(idMovimentacao), visualizacao);
    });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes da movimentação de estoque';
    return this.modoEdicao()
      ? 'Editar descrição da movimentação'
      : 'Cadastrar movimentação de estoque';
  }

  protected get descricaoPagina(): string {
    if (this.visualizacao()) return 'Consulte os itens, locais e quantidades transferidos.';
    return this.modoEdicao()
      ? 'A edição permite alterar somente a descrição/justificativa.'
      : 'Selecione um destino e inclua um ou mais itens com saldo disponível.';
  }

  protected get locaisComSaldo(): ItemEstoqueLocal[] {
    const destino = this.form.controls.idNovoArmazenamento.value;
    return (this.itemSelecionado()?.locais ?? []).filter(
      (local) => local.quantidade > 0 && local.idEndereco !== destino,
    );
  }

  protected get saldoDisponivel(): number {
    const idItem = this.form.controls.idItem.value;
    const idOrigem = this.form.controls.idArmazenamentoAntigo.value;
    if (idItem === null || idOrigem === null) return 0;
    const saldo = this.itemSelecionado()?.locais?.find(
      (local) => local.idEndereco === idOrigem,
    )?.quantidade;
    return Math.max(0, (saldo ?? 0) - this.quantidadeJaIncluida(idItem, idOrigem));
  }

  protected get origemIgualDestino(): boolean {
    const destino = this.form.controls.idNovoArmazenamento.value;
    const origemAtual = this.form.controls.idArmazenamentoAntigo.value;
    return (
      destino !== null &&
      (origemAtual === destino ||
        this.itensIncluidos().some((item) => item.idArmazenamentoAntigo === destino))
    );
  }

  protected get nomeDestinoSelecionado(): string {
    const idDestino = this.form.controls.idNovoArmazenamento.value;
    return this.enderecos().find((endereco) => endereco.id === idDestino)?.nome ?? 'destino';
  }

  protected get unidadeMedidaSelecionada(): string {
    const unidade = this.itemSelecionado()?.unidadeMedida;
    if (!unidade) return '';
    if (typeof unidade === 'string') return this.abreviarUnidade(unidade);
    return this.abreviarUnidade(unidade.codigo || unidade.descricao);
  }

  protected adicionarItem(): void {
    const { idItem, idArmazenamentoAntigo, quantidade, idNovoArmazenamento } =
      this.form.getRawValue();
    const item = this.itemSelecionado();
    const origem = this.itemSelecionado()?.locais?.find(
      (local) => local.idEndereco === idArmazenamentoAntigo,
    );
    if (
      idItem === null ||
      idArmazenamentoAntigo === null ||
      quantidade === null ||
      quantidade <= 0 ||
      !Number.isInteger(quantidade) ||
      !item ||
      !origem ||
      idNovoArmazenamento === null
    ) {
      this.notificacao.alerta('Informe o item, a origem, uma quantidade válida e o destino.');
      return;
    }
    if (idArmazenamentoAntigo === idNovoArmazenamento) {
      this.notificacao.alerta('O endereço de origem não pode ser igual ao destino.');
      return;
    }
    if (quantidade > this.saldoDisponivel) {
      this.notificacao.alerta('A quantidade excede o saldo disponível neste local de origem.');
      return;
    }
    this.itensIncluidos.update((itens) => [
      ...itens,
      {
        idItem,
        idArmazenamentoAntigo,
        quantidade,
        nomeItem: item.nome,
        nomeArmazenamentoAntigo: origem.nomeEndereco || origem.nomeLocalArmazenamento || 'Local',
        unidadeMedida: this.unidadeMedidaSelecionada,
      },
    ]);
    this.form.controls.quantidade.reset(null);
  }

  protected removerItem(indice: number): void {
    this.itensIncluidos.update((itens) => itens.filter((_, index) => index !== indice));
  }

  protected salvar(): void {
    if (this.visualizacao()) return;
    if (this.modoEdicao()) {
      this.atualizarDescricao();
      return;
    }
    const destino = this.form.controls.idNovoArmazenamento.value;
    if (destino === null) {
      this.form.controls.idNovoArmazenamento.markAsTouched();
      this.notificacao.alerta('Selecione o endereço de destino.');
      return;
    }
    if (this.itensIncluidos().length === 0) {
      this.notificacao.alerta('Inclua ao menos um item na movimentação.');
      return;
    }
    if (this.origemIgualDestino) {
      this.notificacao.alerta('O endereço de destino não pode ser a origem de nenhum item.');
      return;
    }

    const requisicao: MovimentacaoRequisicao = {
      idNovoArmazenamento: destino,
      descricao: (this.form.controls.descricao.value ?? '').trim() || undefined,
      itens: this.itensIncluidos().map(({ idItem, idArmazenamentoAntigo, quantidade }) => ({
        idItem,
        idArmazenamentoAntigo,
        quantidade,
      })),
    };
    this.carregando.set(true);
    this.movimentacaoService
      .cadastrar(requisicao)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Movimentação cadastrada com sucesso.');
          this.voltarParaLista();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificarErro(erro, 'Não foi possível cadastrar a movimentação.'),
      });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/doacoes/movimentacoes', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  private atualizarDescricao(): void {
    const id = Number(this.id());
    this.carregando.set(true);
    this.movimentacaoService
      .atualizar(id, {
        descricao: (this.form.controls.descricao.value ?? '').trim() || undefined,
      })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Descrição da movimentação atualizada com sucesso.');
          this.voltarParaLista();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificarErro(erro, 'Não foi possível atualizar a descrição da movimentação.'),
      });
  }

  private carregarMovimentacao(id: number, visualizacao: boolean): void {
    this.carregando.set(true);
    this.movimentacaoService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (movimentacao) => {
          this.registro.set(movimentacao);
          this.form.controls.descricao.setValue(movimentacao.descricao ?? '');
          if (visualizacao) this.form.disable();
          else {
            this.form.controls.idNovoArmazenamento.disable();
            this.form.controls.idItem.disable();
            this.form.controls.idArmazenamentoAntigo.disable();
            this.form.controls.quantidade.disable();
          }
        },
        error: (erro: HttpErrorResponse) =>
          this.notificarErro(erro, 'Não foi possível carregar a movimentação.'),
      });
  }

  private voltarParaLista(): void {
    this.router.navigate(['/doacoes/movimentacoes'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  private quantidadeJaIncluida(idItem: number, idOrigem: number): number {
    return this.itensIncluidos()
      .filter((item) => item.idItem === idItem && item.idArmazenamentoAntigo === idOrigem)
      .reduce((total, item) => total + item.quantidade, 0);
  }

  private abreviarUnidade(unidade: string): string {
    const unidadeNormalizada = unidade.toLocaleLowerCase();
    const abreviaturas: Record<string, string> = {
      quilo: 'kg',
      quilogramas: 'kg',
      unidade: 'un',
      grama: 'g',
      metro: 'm',
      centímetro: 'cm',
      milímetro: 'mm',
      miligrama: 'mg',
      caixa: 'cx',
      pacote: 'pct',
      fardo: 'fd',
      lata: 'lt',
    };
    return abreviaturas[unidadeNormalizada] ?? unidade.toLowerCase();
  }

  private notificarErro(erro: HttpErrorResponse, padrao: string): void {
    const corpo = erro.error as { detail?: string } | null;
    this.notificacao.erro(corpo?.detail || padrao);
  }
}
