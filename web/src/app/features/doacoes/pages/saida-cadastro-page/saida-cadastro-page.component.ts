import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { PessoaResumo } from '@features/pessoas/models/pessoa.model';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { ErroApi, ErroCampo } from '@features/shared/models/erro-api.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs/operators';
import { formatarDataHoraEntrada } from '../../models/entrada.model';
import { ItemEstoqueLocal, ItemResumo, obterSiglaUnidade } from '../../models/item.model';
import { ItemSaidaDetalhe, SaidaDetalhe, SaidaRequisicao } from '../../models/saida.model';
import { ItemService } from '../../services/item.service';
import { SaidaService } from '../../services/saida.service';

const TAMANHO_LISTA_PESSOAS = 1000;
const TAMANHO_MAXIMO_DESCRICAO = 255;

export interface LinhaItemSaida {
  idItem: number;
  nomeItem: string;
  sigla: string;
  quantidade: number;
  idArmazenamento: number;
  nomeArmazenamento: string;
}

@Component({
  selector: 'app-saida-cadastro-page',
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
  templateUrl: './saida-cadastro-page.component.html',
  styles: `
    .saida-local {
      border: 1px solid #dfe4ea;
      border-radius: 0.5rem;
      background: #ffffff;
      color: #344054;
      padding: 0.5rem 1rem;
      font-size: 0.875rem;
      font-weight: 500;
      transition:
        background-color 0.15s ease,
        border-color 0.15s ease,
        color 0.15s ease;
    }

    .saida-local:hover {
      border-color: #0b3c5d;
    }

    .saida-local--ativo {
      border-color: #0b3c5d;
      background: #e8eef3;
      color: #0b3c5d;
      font-weight: 600;
    }

    .saida-local:disabled {
      border-color: #dfe4ea;
      background: #f4f6f8;
      color: #98a2b3;
      cursor: not-allowed;
    }
  `,
})
export class SaidaCadastroPageComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly saidaService = inject(SaidaService);
  private readonly itemService = inject(ItemService);
  private readonly pessoaService = inject(PessoaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly tamanhoMaximoDescricao = TAMANHO_MAXIMO_DESCRICAO;
  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly recebedores = signal<PessoaResumo[]>([]);
  protected readonly locaisDoItem = signal<ItemEstoqueLocal[]>([]);
  protected readonly localSelecionado = signal<number | null>(null);
  protected readonly itensIncluidos = signal<LinhaItemSaida[]>([]);
  protected readonly carregando = signal(false);
  protected readonly carregandoLocais = signal(false);
  protected readonly registro = signal<SaidaDetalhe | null>(null);

  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly podeExcluirItem = computed(() => (this.registro()?.itens?.length ?? 0) > 1);

  protected readonly form = this.fb.group({
    idRecebedor: [null as number | null, [Validators.required]],
    descricao: ['', [Validators.maxLength(TAMANHO_MAXIMO_DESCRICAO)]],
  });

  protected readonly itemForm = this.fb.group({
    idItem: [null as number | null, [Validators.required]],
    quantidade: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  private readonly idItemSelecionado = toSignal(this.itemForm.controls.idItem.valueChanges, {
    initialValue: null as number | null,
  });

  protected readonly itemSelecionado = computed(() => {
    const idItem = this.idItemSelecionado();
    if (!idItem) return null;

    return this.itens().find((candidato) => candidato.id === idItem) ?? null;
  });

  protected readonly siglaSelecionada = computed(() => {
    const item = this.itemSelecionado();
    return item ? obterSiglaUnidade(item.unidadeMedida) : '';
  });

  protected readonly saldoDisponivelNoLocal = computed(() => {
    const idItem = this.idItemSelecionado();
    const idLocal = this.localSelecionado();
    if (!idItem || !idLocal) return 0;

    const local = this.locaisDoItem().find((candidato) => candidato.idEndereco === idLocal);
    return local ? this.saldoRestanteDoLocal(local) : 0;
  });

  constructor() {
    effect(() => {
      const idSaida = this.id();
      if (idSaida) {
        this.carregar(Number(idSaida));
      }
    });

    effect(() => {
      const idItem = this.idItemSelecionado();
      this.localSelecionado.set(null);
      this.locaisDoItem.set([]);
      if (!idItem) return;

      this.carregandoLocais.set(true);
      this.itemService
        .buscarPorId(idItem)
        .pipe(finalize(() => this.carregandoLocais.set(false)))
        .subscribe({
          next: (item) => {
            const disponiveis = (item.locais ?? []).filter((local) => (local.quantidade ?? 0) > 0);
            this.locaisDoItem.set(disponiveis);
          },
          error: () => {
            this.notificacao.erro('Não foi possível carregar os locais com estoque deste item.');
          },
        });
    });
  }

  ngOnInit(): void {
    this.itemService.listarTodos().subscribe({
      next: (itens) => this.itens.set(itens),
    });

    this.pessoaService
      .listar({ page: 0, size: TAMANHO_LISTA_PESSOAS, sort: 'nome,asc' })
      .subscribe({
        next: (resposta) => this.recebedores.set(resposta.dados),
      });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes da Saída';
    return this.modoEdicao() ? 'Editar Saída' : 'Cadastro de Saída';
  }

  protected get descricaoPagina(): string {
    if (this.visualizacao()) {
      return 'Visualização completa dos itens entregues, recebedor e locais de retirada.';
    }
    if (this.modoEdicao()) {
      return 'Edição de saída: altere o recebedor e a descrição. A lista de itens entregues é protegida e não pode ser modificada.';
    }
    return 'Selecione itens com estoque disponível, defina o local de origem de cada item e o beneficiário recebedor.';
  }

  protected get dataHoraFormatada(): string {
    return formatarDataHoraEntrada(this.registro()?.dataHora);
  }

  protected rotuloSaldoTotal(item: ItemResumo): string {
    const sigla = obterSiglaUnidade(item.unidadeMedida);
    const saldo = item.estoque ?? 0;
    return sigla
      ? `${item.nome} — Saldo Total: ${saldo} ${sigla}`
      : `${item.nome} — Saldo Total: ${saldo}`;
  }

  private mensagemSaldoInsuficiente(): string {
    const sigla = this.siglaSelecionada();
    const disponivel = this.saldoDisponivelNoLocal();
    return `Saldo insuficiente neste local. Disponível: ${disponivel}${sigla ? ' ' + sigla : ''}.`;
  }

  protected saldoRestanteDoLocal(local: ItemEstoqueLocal): number {
    const idItem = this.idItemSelecionado();
    if (!idItem) return local.quantidade;

    const jaIncluido = this.itensIncluidos()
      .filter((linha) => linha.idItem === idItem && linha.idArmazenamento === local.idEndereco)
      .reduce((total, linha) => total + linha.quantidade, 0);

    return Math.max(0, local.quantidade - jaIncluido);
  }

  protected rotuloLocal(local: ItemEstoqueLocal): string {
    const sigla = this.siglaSelecionada();
    const nome = local.nomeEndereco ?? local.nomeLocalArmazenamento ?? 'Endereço';
    const saldo = this.saldoRestanteDoLocal(local);
    return sigla ? `${nome} · Saldo: ${saldo} ${sigla}` : `${nome} · Saldo: ${saldo}`;
  }

  protected selecionarLocal(local: ItemEstoqueLocal): void {
    this.localSelecionado.set(local.idEndereco);
  }

  protected incluirItem(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      this.notificacao.alerta(
        this.itemForm.controls.quantidade.hasError('max')
          ? this.mensagemSaldoInsuficiente()
          : 'Informe o item e a quantidade a ser retirada.',
      );
      return;
    }

    const idLocal = this.localSelecionado();
    if (!idLocal) {
      this.notificacao.alerta('Selecione o local de origem do estoque.');
      return;
    }

    const item = this.itemSelecionado();
    const local = this.locaisDoItem().find((candidato) => candidato.idEndereco === idLocal);
    if (!item || !local) return;

    const quantidade = Number(this.itemForm.getRawValue().quantidade);
    const disponivel = this.saldoDisponivelNoLocal();

    if (quantidade > disponivel) {
      this.itemForm.controls.quantidade.setErrors({ saldo: true });
      this.itemForm.controls.quantidade.markAsTouched();
      this.notificacao.alerta(this.mensagemSaldoInsuficiente());
      return;
    }

    this.itensIncluidos.update((atuais) => [
      ...atuais,
      {
        idItem: item.id,
        nomeItem: item.nome,
        sigla: obterSiglaUnidade(item.unidadeMedida),
        quantidade,
        idArmazenamento: local.idEndereco,
        nomeArmazenamento: local.nomeEndereco ?? local.nomeLocalArmazenamento ?? 'Endereço',
      },
    ]);

    this.itemForm.reset({ idItem: null, quantidade: null });
    this.localSelecionado.set(null);
  }

  protected removerItemIncluido(indice: number): void {
    this.itensIncluidos.update((atuais) => atuais.filter((_, posicao) => posicao !== indice));
  }

  protected formatarQuantidade(linha: LinhaItemSaida): string {
    return linha.sigla ? `${linha.quantidade} ${linha.sigla}` : `${linha.quantidade}`;
  }

  protected formatarQuantidadeDetalhe(item: ItemSaidaDetalhe): string {
    const encontrado = this.itens().find((candidato) => candidato.id === item.idItem);
    const sigla = encontrado ? obterSiglaUnidade(encontrado.unidadeMedida) : '';
    return sigla ? `${item.quantidade} ${sigla}` : `${item.quantidade}`;
  }

  protected salvar(): void {
    if (this.visualizacao()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacao.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    if (this.modoCriacao() && this.itensIncluidos().length === 0) {
      this.notificacao.alerta('Inclua ao menos um item na lista de saída.');
      return;
    }

    this.carregando.set(true);
    const valores = this.form.getRawValue();

    if (this.modoEdicao()) {
      this.saidaService
        .atualizar(Number(this.id()), {
          idRecebedor: valores.idRecebedor!,
          descricao: valores.descricao?.trim() || undefined,
        })
        .pipe(finalize(() => this.carregando.set(false)))
        .subscribe({
          next: () => {
            this.notificacao.sucesso('Saída atualizada com sucesso.');
            this.location.back();
          },
          error: (erro: HttpErrorResponse) => this.tratarErro(erro),
        });
      return;
    }

    const requisicao: SaidaRequisicao = {
      idRecebedor: valores.idRecebedor!,
      descricao: valores.descricao?.trim() || undefined,
      itens: this.itensIncluidos().map((linha) => ({
        idItem: linha.idItem,
        idArmazenamento: linha.idArmazenamento,
        quantidade: linha.quantidade,
      })),
    };

    this.saidaService
      .cadastrar(requisicao)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Saída cadastrada com sucesso.');
          this.location.back();
        },
        error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/doacoes/saidas', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async excluirItemSaida(item: ItemSaidaDetalhe): Promise<void> {
    const saida = this.registro();
    if (!saida) return;

    if (saida.itens.length <= 1) {
      this.notificacao.alerta(
        'A saída precisa manter ao menos um item. Para removê-lo, exclua a saída inteira.',
      );
      return;
    }

    const confirmou = await this.confirmacao.confirmar(
      'Excluir item da saída',
      `A quantidade de ${item.nomeItem} será estornada ao estoque de ${item.nomeArmazenamento}. Deseja remover este item da saída?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    this.saidaService
      .excluirItem(saida.id, item.id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Item removido da saída.');
          this.carregar(saida.id);
        },
        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as { detail?: string };
          this.notificacao.erro(corpo?.detail || 'Não foi possível remover o item da saída.');
        },
      });
  }

  protected async excluirSaida(): Promise<void> {
    const saida = this.registro();
    if (!saida) return;

    const confirmou = await this.confirmacao.confirmar(
      'Excluir saída',
      'A exclusão é permanente e estorna as quantidades de volta ao estoque. Deseja excluir esta saída?',
    );
    if (!confirmou) return;

    this.carregando.set(true);
    this.saidaService
      .excluir(saida.id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Saída excluída com sucesso.');
          this.router.navigate(['/doacoes/saidas']);
        },
        error: () => {
          this.notificacao.erro('Não foi possível excluir a saída.');
        },
      });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.saidaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (saida) => {
          this.registro.set(saida);
          this.form.patchValue({
            idRecebedor: saida.idRecebedor ?? null,
            descricao: saida.descricao ?? '',
          });

          if (this.visualizacao()) {
            this.form.disable();
          }
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar a saída.');
          this.router.navigate(['/doacoes/saidas']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as (ErroApi & { invalidFields?: Record<string, string> }) | undefined;

    const campos: ErroCampo[] = [
      ...(corpo?.erros ?? []),
      ...Object.entries(corpo?.invalidFields ?? {}).map(([campo, mensagem]) => ({
        campo,
        mensagem,
      })),
    ];

    const semControle: string[] = [];

    for (const { campo, mensagem } of campos) {
      const controle = this.form.get(campo);
      if (controle) {
        controle.setErrors({ backend: mensagem });
      } else {
        semControle.push(mensagem);
      }
    }

    if (semControle.length > 0) {
      this.notificacao.erro(semControle.join(' '));
    } else if (campos.length === 0) {
      this.notificacao.erro(corpo?.detail || 'Não foi possível salvar a saída.');
    }

    this.form.markAllAsTouched();
  }
}
