import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { effect, input } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { CategoriaService } from '@features/pessoas/services/categoria.service';
import { CategoriaRequisicao } from '@features/pessoas/models/categoria.model';

@Component({
  selector: 'app-categoria-cadastro-page',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './categoria-cadastro-page.component.html',
})
export class CategoriaCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly categoriaService = inject(CategoriaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacaoService = inject(NotificacaoService);

  protected readonly carregando = signal(false);
  protected readonly visualizacao = input(false);
  protected readonly id = input<string>();
  protected readonly categoria = signal<{ nome: string; descricao?: string; ativo?: boolean } | null>(null);
  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/)]],
    descricao: [''],
  });

  constructor() {
    effect(() => {
      const id = this.id();
      if (id) this.carregar(Number(id));
    });
  }

  protected get modoEdicao(): boolean {
    return !!this.id() && !this.visualizacao();
  }

  protected get titulo(): string {
    return this.visualizacao() ? 'Detalhes da Categoria' : this.modoEdicao ? 'Atualização de Categoria' : 'Cadastro de Categoria';
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    const requisicao$ = this.modoEdicao
      ? this.categoriaService.atualizar(Number(this.id()), this.montarRequisicao())
      : this.categoriaService.cadastrar(this.montarRequisicao());

    requisicao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(`Categoria ${this.modoEdicao ? 'atualizada' : 'cadastrada'} com sucesso!`);
        this.location.back();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected cancelar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/pessoas/categorias', this.id(), 'editar']);
  }

  protected async excluir(): Promise<void> {
    const confirmou = await this.confirmacao.confirmar(
      'Excluir categoria',
      'Esta ação removerá definitivamente a categoria. Deseja continuar?',
    );
    if (!confirmou || !this.id()) return;

    this.carregando.set(true);
    this.categoriaService.excluir(Number(this.id())).subscribe({
      next: () => {
        this.notificacaoService.sucesso('Categoria excluída com sucesso!');
        this.location.back();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.categoriaService.buscarPorId(id).subscribe({
      next: (categoria) => {
        this.categoria.set(categoria);
        this.form.patchValue({ nome: categoria.nome, descricao: categoria.descricao ?? '' });
        if (this.visualizacao()) this.form.disable();
      },
      error: () => this.location.back(),
      complete: () => this.carregando.set(false),
    });
  }

  private montarRequisicao(): CategoriaRequisicao {
    const valores = this.form.getRawValue();
    return {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || undefined,
    };
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    corpo?.erros?.forEach(({ campo, mensagem }) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });
  }
}
