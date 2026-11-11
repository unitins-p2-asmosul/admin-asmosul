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
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs/operators';
import { ItemResumo, obterSiglaUnidade } from '../../models/item.model';
import { ItemKitDetalhe, KitDetalhe, KitRequisicao } from '../../models/kit.model';
import { ItemService } from '../../services/item.service';
import { KitService } from '../../services/kit.service';

export interface LinhaItemKit {
  idItem: number;
  nomeItem: string;
  sigla: string;
  quantidade: number;
  sobraEstoque?: number;
}

@Component({
  selector: 'app-kit-cadastro-page',
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
  templateUrl: './kit-cadastro-page.component.html',
})
export class KitCadastroPageComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly kitService = inject(KitService);
  private readonly itemService = inject(ItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly itensCatalogo = signal<ItemResumo[]>([]);
  protected readonly itensNoKit = signal<LinhaItemKit[]>([]);
  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly carregando = signal(false);
  protected readonly registro = signal<KitDetalhe | null>(null);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    descricao: [''],
    quantidadeDistribuivel: [{ value: 0, disabled: true }],
  });

  protected readonly itemForm = this.fb.group({
    idItem: [null as number | null, [Validators.required]],
    quantidade: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  private readonly idItemSelecionado = toSignal(this.itemForm.controls.idItem.valueChanges, {
    initialValue: null as number | null,
  });

  protected readonly siglaItemSelecionado = computed(() => {
    const idItem = this.idItemSelecionado();
    if (!idItem) return '';
    const item = this.itensCatalogo().find((c) => c.id === idItem);
    return item ? obterSiglaUnidade(item.unidadeMedida) : '';
  });

  constructor() {
    effect(() => {
      const idKit = this.id();
      if (idKit) {
        this.carregar(Number(idKit));
      }
    });
  }

  ngOnInit(): void {
    this.itemService.listarTodos(false).subscribe({
      next: (itens) => this.itensCatalogo.set(itens),
    });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes do Kit';
    return this.modoEdicao() ? 'Editar Kit' : 'Cadastrar Kit';
  }

  protected get descricaoPagina(): string {
    if (this.visualizacao()) {
      return 'Consulte a composição deste kit e a estimativa de montagem conforme o estoque.';
    }
    return this.modoEdicao()
      ? 'Atualize o nome, descrição e os itens que compõem este kit.'
      : 'Defina um nome e os itens padronizados que compõem este kit para agilizar o lançamento de saídas.';
  }

  protected itemJaAdicionado(idItem: number): boolean {
    return this.itensNoKit().some((linha) => linha.idItem === idItem);
  }

  protected rotuloItemOption(item: ItemResumo): string {
    const sigla = obterSiglaUnidade(item.unidadeMedida);
    const sufixo = this.itemJaAdicionado(item.id) ? ' (já adicionado)' : '';
    return sigla ? `${item.nome} (${sigla})${sufixo}` : `${item.nome}${sufixo}`;
  }

  protected incluirItem(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      this.notificacao.alerta('Selecione um item e informe uma quantidade maior que zero.');
      return;
    }

    const { idItem, quantidade } = this.itemForm.getRawValue();
    if (!idItem || !quantidade || quantidade < 1) return;

    if (this.itemJaAdicionado(idItem)) {
      this.notificacao.alerta('Não é permitido adicionar o mesmo item mais de uma vez no kit.');
      return;
    }

    const item = this.itensCatalogo().find((candidato) => candidato.id === idItem);
    if (!item) return;

    this.itensNoKit.update((atuais) => [
      ...atuais,
      {
        idItem: item.id,
        nomeItem: item.nome,
        sigla: obterSiglaUnidade(item.unidadeMedida),
        quantidade: Number(quantidade),
      },
    ]);

    this.itemForm.reset({ idItem: null, quantidade: null });
  }

  protected removerItem(indice: number): void {
    this.itensNoKit.update((atuais) => atuais.filter((_, pos) => pos !== indice));
  }

  protected formatarQuantidadeLinha(linha: LinhaItemKit): string {
    return linha.sigla ? `${linha.quantidade} ${linha.sigla}` : `${linha.quantidade}`;
  }

  protected formatarSobraLinha(linha: LinhaItemKit): string {
    const sobra = linha.sobraEstoque ?? 0;
    return linha.sigla ? `${sobra} ${linha.sigla}` : `${sobra}`;
  }

  protected salvar(): void {
    if (this.visualizacao()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacao.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    if (this.itensNoKit().length === 0) {
      this.notificacao.alerta('Adicione pelo menos um item à composição do kit.');
      return;
    }

    this.carregando.set(true);
    const valores = this.form.getRawValue();
    const nomeNormalizado = valores.nome.trim().replace(/\s+/g, ' ');

    const requisicao: KitRequisicao = {
      nome: nomeNormalizado,
      descricao: valores.descricao?.trim() || undefined,
      itens: this.itensNoKit().map((linha) => ({
        idItem: linha.idItem,
        quantidade: linha.quantidade,
      })),
    };

    const operacao = this.modoEdicao()
      ? this.kitService.atualizar(Number(this.id()), requisicao)
      : this.kitService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Kit ${this.modoEdicao() ? 'atualizado' : 'cadastrado'} com sucesso.`,
        );
        this.location.back();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
    });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/doacoes/kits', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async alternarEstado(): Promise<void> {
    const kit = this.registro();
    if (!kit) return;

    const reativar = !kit.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} kit`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} este kit?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    const operacao = reativar
      ? this.kitService.reativar(kit.id)
      : this.kitService.desativar(kit.id);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(`Kit ${reativar ? 'reativado' : 'inativado'} com sucesso.`);
        this.carregar(kit.id);
      },
      error: () => {
        this.notificacao.erro(`Não foi possível ${reativar ? 'reativar' : 'inativar'} o kit.`);
      },
    });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.kitService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (kit) => {
          this.registro.set(kit);
          this.form.patchValue({
            nome: kit.nome,
            descricao: kit.descricao ?? '',
            quantidadeDistribuivel: kit.quantidadeDistribuivel ?? 0,
          });

          const linhas: LinhaItemKit[] = (kit.itens ?? []).map((itemDetalhe: ItemKitDetalhe) => ({
            idItem: itemDetalhe.idItem,
            nomeItem: itemDetalhe.nomeItem,
            sigla: obterSiglaUnidade(itemDetalhe.unidadeMedida),
            quantidade: itemDetalhe.quantidade,
            sobraEstoque: itemDetalhe.sobraEstoque,
          }));
          this.itensNoKit.set(linhas);

          if (this.visualizacao() || !kit.ativo) {
            this.form.disable();
            this.itemForm.disable();
          }
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar o kit.');
          this.router.navigate(['/doacoes/kits']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as { detail?: string; erros?: { campo: string; mensagem: string }[] };
    for (const campo of corpo?.erros ?? []) {
      this.form.get(campo.campo)?.setErrors({ backend: campo.mensagem });
    }

    if (erro.status === 409) {
      const mensagem = corpo?.detail || 'Já existe um kit cadastrado com este nome.';
      this.form.controls.nome.setErrors({ backend: mensagem });
      this.notificacao.alerta(mensagem);
    } else if (erro.status !== 400 || !corpo?.erros?.length) {
      this.notificacao.erro(corpo?.detail || 'Não foi possível salvar o kit.');
    } else {
      const mensagemCampo = corpo.erros.map((e) => e.mensagem).join(' ');
      this.notificacao.alerta(mensagemCampo);
    }
    this.form.markAllAsTouched();
  }
}
