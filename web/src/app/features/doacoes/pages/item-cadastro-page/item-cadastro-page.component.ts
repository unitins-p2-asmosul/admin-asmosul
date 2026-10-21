import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs/operators';
import { CategoriaItemResumo } from '../../models/categoria-item.model';
import { ItemDetalhe, ItemRequisicao } from '../../models/item.model';
import { UNIDADE_MEDIDA_OPCOES, UnidadeMedidaCodigo } from '../../models/unidade-medida.model';
import { CategoriaItemService } from '../../services/categoria-item.service';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-item-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './item-cadastro-page.component.html',
})
export class ItemCadastroPageComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly itemService = inject(ItemService);
  private readonly categoriaItemService = inject(CategoriaItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly unidadesMedida = UNIDADE_MEDIDA_OPCOES;
  protected readonly categorias = signal<CategoriaItemResumo[]>([]);
  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly carregando = signal(false);
  protected readonly registro = signal<ItemDetalhe | null>(null);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    idCategoria: [null as number | null, [Validators.required]],
    precoUnitario: [0, [Validators.required, Validators.min(0)]],
    unidadeMedida: [UnidadeMedidaCodigo.UNIDADE as UnidadeMedidaCodigo, [Validators.required]],
    descricao: [''],
    estoque: [{ value: 0, disabled: true }],
  });

  constructor() {
    effect(() => {
      const idItem = this.id();
      if (idItem) {
        this.carregar(Number(idItem));
      }
    });
  }

  ngOnInit(): void {
    this.carregarCategorias();
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes do item';
    return this.modoEdicao() ? 'Editar item' : 'Cadastrar item';
  }

  protected get descricao(): string {
    if (this.visualizacao()) return 'Consulte os dados deste item.';
    return this.modoEdicao()
      ? 'Atualize os dados cadastrais do item.'
      : 'Preencha os campos para cadastrar um novo item no estoque.';
  }

  private carregarCategorias(): void {
    this.categoriaItemService.listar({ page: 0, size: 100, sort: 'nome,asc' }).subscribe({
      next: (resposta) => {
        this.categorias.set(resposta.dados);
      },
    });
  }

  protected salvar(): void {
    if (this.visualizacao()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacao.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    const valores = this.form.getRawValue();
    const precoCentavos = Math.round(Number(valores.precoUnitario) * 100);

    const requisicao: ItemRequisicao = {
      nome: valores.nome.trim(),
      idCategoria: valores.idCategoria!,
      precoUnitario: precoCentavos,
      unidadeMedida: valores.unidadeMedida,
      descricao: valores.descricao?.trim() || undefined,
    };

    const operacao = this.modoEdicao()
      ? this.itemService.atualizar(Number(this.id()), requisicao)
      : this.itemService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Item ${this.modoEdicao() ? 'atualizado' : 'cadastrado'} com sucesso.`,
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
    this.router.navigate(['/doacoes/itens', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async alternarEstado(): Promise<void> {
    const item = this.registro();
    if (!item) return;

    const reativar = !item.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} item`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} este item?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    const operacao = reativar
      ? this.itemService.reativar(item.id)
      : this.itemService.desativar(item.id);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(`Item ${reativar ? 'reativado' : 'inativado'} com sucesso.`);
        this.carregar(item.id);
      },
      error: () => {
        this.notificacao.erro(`Não foi possível ${reativar ? 'reativar' : 'inativar'} o item.`);
      },
    });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.itemService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (item) => {
          this.registro.set(item);
          const unidadeCodigo =
            typeof item.unidadeMedida === 'string'
              ? (item.unidadeMedida as UnidadeMedidaCodigo)
              : (item.unidadeMedida?.codigo as UnidadeMedidaCodigo);

          this.form.patchValue({
            nome: item.nome,
            idCategoria: item.categoria?.id ?? null,
            precoUnitario: item.precoUnitario ? item.precoUnitario / 100 : 0,
            unidadeMedida: unidadeCodigo,
            descricao: item.descricao ?? '',
            estoque: item.estoque ?? 0,
          });

          if (this.visualizacao() || !item.ativo) {
            this.form.disable();
          }
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar o item.');
          this.router.navigate(['/doacoes/itens']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as { detail?: string; erros?: { campo: string; mensagem: string }[] };
    for (const campo of corpo?.erros ?? []) {
      this.form.get(campo.campo)?.setErrors({ backend: campo.mensagem });
    }

    if (erro.status === 409) {
      const mensagem = corpo?.detail || 'Já existe um item cadastrado com este nome.';
      this.form.controls.nome.setErrors({ backend: mensagem });
      this.notificacao.alerta(mensagem);
    } else if (erro.status !== 400 || !corpo?.erros?.length) {
      this.notificacao.erro(corpo?.detail || 'Não foi possível salvar o item.');
    }
    this.form.markAllAsTouched();
  }
}
