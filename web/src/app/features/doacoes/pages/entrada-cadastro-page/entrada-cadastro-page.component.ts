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
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs/operators';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import {
  EntradaDetalhe,
  EntradaRequisicao,
  formatarDataHoraEntrada,
  ItemEntradaDetalhe,
} from '../../models/entrada.model';
import { ItemResumo, obterSiglaUnidade } from '../../models/item.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { EntradaService } from '../../services/entrada.service';
import { ItemService } from '../../services/item.service';

const TAMANHO_LISTA_DOADORES = 1000;
const TAMANHO_MAXIMO_DESCRICAO = 255;

export interface LinhaItemEntrada {
  idItem: number;
  nomeItem: string;
  sigla: string;
  quantidade: number;
  idArmazenamento: number;
  nomeArmazenamento: string;
}

@Component({
  selector: 'app-entrada-cadastro-page',
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
  templateUrl: './entrada-cadastro-page.component.html',
})
export class EntradaCadastroPageComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly entradaService = inject(EntradaService);
  private readonly itemService = inject(ItemService);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly pessoaService = inject(PessoaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly tamanhoMaximoDescricao = TAMANHO_MAXIMO_DESCRICAO;
  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly enderecos = signal<EnderecoArmazenamentoResumo[]>([]);
  protected readonly doadores = signal<PessoaResumo[]>([]);
  protected readonly itensIncluidos = signal<LinhaItemEntrada[]>([]);
  protected readonly carregando = signal(false);
  protected readonly registro = signal<EntradaDetalhe | null>(null);

  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly podeExcluirItem = computed(() => (this.registro()?.itens?.length ?? 0) > 1);

  protected readonly form = this.fb.group({
    idDoador: [null as number | null, [Validators.required]],
    descricao: ['', [Validators.maxLength(TAMANHO_MAXIMO_DESCRICAO)]],
  });

  protected readonly itemForm = this.fb.group({
    idItem: [null as number | null, [Validators.required]],
    quantidade: [null as number | null, [Validators.required, Validators.min(1)]],
    idArmazenamento: [null as number | null, [Validators.required]],
  });

  private readonly idItemSelecionado = toSignal(this.itemForm.controls.idItem.valueChanges, {
    initialValue: null as number | null,
  });

  protected readonly siglaSelecionada = computed(() => {
    const idItem = this.idItemSelecionado();
    if (!idItem) return '';

    const item = this.itens().find((candidato) => candidato.id === idItem);
    return item ? obterSiglaUnidade(item.unidadeMedida) : '';
  });

  constructor() {
    effect(() => {
      const idEntrada = this.id();
      if (idEntrada) {
        this.carregar(Number(idEntrada));
      }
    });
  }

  ngOnInit(): void {
    this.itemService.listarTodos().subscribe({
      next: (itens) => this.itens.set(itens),
    });

    this.enderecoService.listarTodas().subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
    });

    this.pessoaService
      .listar({ page: 0, size: TAMANHO_LISTA_DOADORES, sort: 'nome,asc' })
      .subscribe({
        next: (resposta) => this.doadores.set(resposta.dados),
      });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes da Entrada';
    return this.modoEdicao() ? 'Editar Entrada' : 'Cadastro de Entrada';
  }

  protected get descricaoPagina(): string {
    if (this.visualizacao()) {
      return 'Visualização completa dos itens recebidos, doador e locais de armazenamento.';
    }
    if (this.modoEdicao()) {
      return 'Edição de entrada: altere o doador e a descrição. A lista de itens recebidos é protegida e não pode ser modificada.';
    }
    return 'Registre os itens recebidos, quantidades com unidade de medida, doador e endereço(s) de armazenamento.';
  }

  protected get dataHoraFormatada(): string {
    return formatarDataHoraEntrada(this.registro()?.dataHora);
  }

  protected incluirItem(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      this.notificacao.alerta('Informe item, quantidade e endereço de armazenamento.');
      return;
    }

    const valores = this.itemForm.getRawValue();
    const item = this.itens().find((candidato) => candidato.id === valores.idItem);
    const endereco = this.enderecos().find((candidato) => candidato.id === valores.idArmazenamento);
    if (!item || !endereco) return;

    this.itensIncluidos.update((atuais) => [
      ...atuais,
      {
        idItem: item.id,
        nomeItem: item.nome,
        sigla: obterSiglaUnidade(item.unidadeMedida),
        quantidade: Number(valores.quantidade),
        idArmazenamento: endereco.id,
        nomeArmazenamento: endereco.nome,
      },
    ]);

    this.itemForm.reset({ idItem: null, quantidade: null, idArmazenamento: null });
  }

  protected removerItemIncluido(indice: number): void {
    this.itensIncluidos.update((atuais) => atuais.filter((_, posicao) => posicao !== indice));
  }

  protected formatarQuantidade(linha: LinhaItemEntrada): string {
    return linha.sigla ? `${linha.quantidade} ${linha.sigla}` : `${linha.quantidade}`;
  }

  protected formatarQuantidadeDetalhe(item: ItemEntradaDetalhe): string {
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
      this.notificacao.alerta('Inclua ao menos um item na entrada.');
      return;
    }

    this.carregando.set(true);
    const valores = this.form.getRawValue();

    if (this.modoEdicao()) {
      this.entradaService
        .atualizar(Number(this.id()), {
          idDoador: valores.idDoador!,
          descricao: valores.descricao?.trim() || undefined,
        })
        .pipe(finalize(() => this.carregando.set(false)))
        .subscribe({
          next: () => {
            this.notificacao.sucesso('Entrada atualizada com sucesso.');
            this.location.back();
          },
          error: (erro: HttpErrorResponse) => this.tratarErro(erro),
        });
      return;
    }

    const requisicao: EntradaRequisicao = {
      idDoador: valores.idDoador!,
      descricao: valores.descricao?.trim() || undefined,
      itens: this.itensIncluidos().map((linha) => ({
        idItem: linha.idItem,
        idArmazenamento: linha.idArmazenamento,
        quantidade: linha.quantidade,
      })),
    };

    this.entradaService
      .cadastrar(requisicao)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Entrada cadastrada com sucesso.');
          this.location.back();
        },
        error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/doacoes/entradas', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async excluirItemEntrada(item: ItemEntradaDetalhe): Promise<void> {
    const entrada = this.registro();
    if (!entrada) return;

    if (entrada.itens.length <= 1) {
      this.notificacao.alerta(
        'A entrada precisa manter ao menos um item. Para removê-lo, exclua a entrada inteira.',
      );
      return;
    }

    const confirmou = await this.confirmacao.confirmar(
      'Excluir item da entrada',
      `A quantidade de ${item.nomeItem} será devolvida ao estoque. Deseja remover este item da entrada?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    this.entradaService
      .excluirItem(entrada.id, item.id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Item removido da entrada.');
          this.carregar(entrada.id);
        },
        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as { detail?: string };
          this.notificacao.erro(corpo?.detail || 'Não foi possível remover o item da entrada.');
        },
      });
  }

  protected async excluirEntrada(): Promise<void> {
    const entrada = this.registro();
    if (!entrada) return;

    const confirmou = await this.confirmacao.confirmar(
      'Excluir entrada',
      'A exclusão é permanente e devolve as quantidades ao estoque. Deseja excluir esta entrada?',
    );
    if (!confirmou) return;

    this.carregando.set(true);
    this.entradaService
      .excluir(entrada.id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Entrada excluída com sucesso.');
          this.router.navigate(['/doacoes/entradas']);
        },
        error: () => {
          this.notificacao.erro('Não foi possível excluir a entrada.');
        },
      });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.entradaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (entrada) => {
          this.registro.set(entrada);
          this.form.patchValue({
            idDoador: entrada.idDoador ?? null,
            descricao: entrada.descricao ?? '',
          });

          if (this.visualizacao()) {
            this.form.disable();
          }
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar a entrada.');
          this.router.navigate(['/doacoes/entradas']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as { detail?: string; erros?: { campo: string; mensagem: string }[] };
    for (const campo of corpo?.erros ?? []) {
      this.form.get(campo.campo)?.setErrors({ backend: campo.mensagem });
    }

    if (erro.status !== 400 || !corpo?.erros?.length) {
      this.notificacao.erro(corpo?.detail || 'Não foi possível salvar a entrada.');
    }
    this.form.markAllAsTouched();
  }
}
