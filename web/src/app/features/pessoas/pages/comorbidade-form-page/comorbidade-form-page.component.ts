import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import {
  FormGroupDirective,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { ComorbidadeDetalhe, ComorbidadeRequisicao } from '../../models/comorbidade.model';
import { ComorbidadeService } from '../../services/comorbidade.service';

const TAMANHO_MAXIMO_NOME = 50;

@Component({
  selector: 'app-comorbidade-form-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
  templateUrl: './comorbidade-form-page.component.html',
})
export class ComorbidadeFormPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly comorbidadeService = inject(ComorbidadeService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly formDirective = viewChild(FormGroupDirective);

  /** Preenchidos pela rota (withComponentInputBinding): `:id` e `data.visualizacao`. */
  readonly id = input<string>();
  readonly visualizacao = input<boolean>(false);

  protected readonly carregando = signal(false);
  protected readonly registro = signal<ComorbidadeDetalhe | null>(null);

  protected readonly modoVisualizacao = computed(() => this.visualizacao());
  protected readonly modoEdicao = computed(() => !this.visualizacao() && !!this.id());
  protected readonly modoCriacao = computed(() => !this.id());

  protected readonly tamanhoMaximoNome = TAMANHO_MAXIMO_NOME;

  protected readonly form = this.fb.group({
    nome: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.maxLength(TAMANHO_MAXIMO_NOME)],
    ],
    descricao: [''],
  });

  constructor() {
    effect(() => {
      const idRegistro = this.id();
      if (idRegistro) {
        this.carregarDados(Number(idRegistro));
      }
    });
  }

  protected salvar(): void {
    if (this.modoVisualizacao()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacaoService.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    const requisicao = this.montarRequisicao();

    const requisicao$ = this.modoEdicao()
      ? this.comorbidadeService.atualizar(Number(this.id()), requisicao)
      : this.comorbidadeService.cadastrar(requisicao);

    requisicao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(
          `Comorbidade ${this.modoEdicao() ? 'atualizada' : 'cadastrada'} com sucesso!`,
        );
        this.formDirective()?.resetForm();
        this.voltar();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  /** Volta para a listagem preservando página, ordenação e filtros da URL anterior. */
  protected voltar(): void {
    this.location.back();
  }

  protected irParaEdicao(): void {
    this.router.navigate(['/pessoas/comorbidades', this.id(), 'editar']);
  }

  protected async alternarStatus(): Promise<void> {
    const atual = this.registro();
    if (!atual || atual.ativo === undefined) {
      return;
    }

    const reativar = !atual.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Desativar'} comorbidade`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'desativar'} "${atual.nome}"?`,
    );

    if (!confirmou) {
      return;
    }

    this.carregando.set(true);
    const requisicao$ = reativar
      ? this.comorbidadeService.reativar(atual.id)
      : this.comorbidadeService.desativar(atual.id);

    requisicao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(
          `Comorbidade ${reativar ? 'reativada' : 'desativada'} com sucesso!`,
        );
        this.carregarDados(atual.id);
      },
      error: () => this.carregando.set(false),
    });
  }

  private carregarDados(id: number): void {
    this.carregando.set(true);
    this.comorbidadeService.buscarPorId(id).subscribe({
      next: (comorbidade) => {
        this.registro.set(comorbidade);
        this.form.patchValue({
          nome: comorbidade.nome,
          descricao: comorbidade.descricao ?? '',
        });

        if (this.modoVisualizacao()) {
          this.form.disable();
        }
      },
      error: () => this.voltar(),
      complete: () => this.carregando.set(false),
    });
  }

  private montarRequisicao(): ComorbidadeRequisicao {
    const valores = this.form.getRawValue();
    return {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || undefined,
    };
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    if (corpo?.erros?.length) {
      corpo.erros.forEach(({ campo, mensagem }) => {
        this.form.get(campo)?.setErrors({ backend: mensagem });
      });
    } else if (erro.status === 409) {
      this.form.controls.nome.setErrors({
        backend: corpo?.detail ?? 'Já existe uma comorbidade com este nome.',
      });
    }

    this.form.markAllAsTouched();
  }
}
