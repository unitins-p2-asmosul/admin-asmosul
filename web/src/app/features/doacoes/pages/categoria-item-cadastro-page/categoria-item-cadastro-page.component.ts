import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs/operators';
import { CategoriaItemDetalhe, CategoriaItemRequisicao } from '../../models/categoria-item.model';
import { CategoriaItemService } from '../../services/categoria-item.service';

@Component({
  selector: 'app-categoria-item-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './categoria-item-cadastro-page.component.html',
})
export class CategoriaItemCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly categoriaItemService = inject(CategoriaItemService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly carregando = signal(false);
  protected readonly registro = signal<CategoriaItemDetalhe | null>(null);
  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(50)]],
    descricao: [''],
  });

  constructor() {
    effect(() => {
      const idCategoria = this.id();
      if (idCategoria) this.carregar(Number(idCategoria));
    });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes da categoria de item';
    return this.modoEdicao() ? 'Editar categoria de item' : 'Criar categoria de item';
  }

  protected get descricao(): string {
    if (this.visualizacao()) return 'Consulte os dados desta categoria de item.';
    return this.modoEdicao()
      ? 'Altere o nome e a descrição da categoria de item.'
      : 'Preencha os dados para cadastrar uma nova categoria de item.';
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
    const requisicao: CategoriaItemRequisicao = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || undefined,
    };
    const operacao = this.modoEdicao()
      ? this.categoriaItemService.atualizar(Number(this.id()), requisicao)
      : this.categoriaItemService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Categoria de item ${this.modoEdicao() ? 'atualizada' : 'cadastrada'} com sucesso.`,
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
    this.router.navigate(['/doacoes/categorias', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async alternarEstado(): Promise<void> {
    const categoria = this.registro();
    if (!categoria) return;

    const reativar = !categoria.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} categoria`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} esta categoria de item?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    const operacao = reativar
      ? this.categoriaItemService.reativar(categoria.id)
      : this.categoriaItemService.desativar(categoria.id);
    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(`Categoria ${reativar ? 'reativada' : 'inativada'} com sucesso.`);
        this.carregar(categoria.id);
      },
    });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.categoriaItemService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (categoria) => {
          this.registro.set(categoria);
          this.form.patchValue({
            nome: categoria.nome,
            descricao: categoria.descricao ?? '',
          });
          if (this.visualizacao() || !categoria.ativo) this.form.disable();
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar a categoria de item.');
          this.router.navigate(['/doacoes/categorias']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as { detail?: string; erros?: { campo: string; mensagem: string }[] };
    for (const campo of corpo?.erros ?? []) {
      this.form.get(campo.campo)?.setErrors({ backend: campo.mensagem });
    }

    if (erro.status === 409) {
      const mensagem = corpo?.detail || 'Já existe uma categoria de item com este nome.';
      this.form.controls.nome.setErrors({ backend: mensagem });
      this.notificacao.alerta(mensagem);
    } else if (erro.status !== 400 || !corpo?.erros?.length) {
      this.notificacao.erro(corpo?.detail || 'Não foi possível salvar a categoria de item.');
    }
    this.form.markAllAsTouched();
  }
}
