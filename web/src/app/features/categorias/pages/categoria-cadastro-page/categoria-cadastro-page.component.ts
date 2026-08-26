import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { CategoriaService } from '../../services/categoria.service';
import { CategoriaRequisicao } from '../../models/categoria.model';

@Component({
  selector: 'app-categoria-cadastro-page',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './categoria-cadastro-page.component.html',
})
export class CategoriaCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly categoriaService = inject(CategoriaService);
  private readonly notificacaoService = inject(NotificacaoService);

  protected readonly carregando = signal(false);
  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/)]],
    descricao: [''],
  });

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.categoriaService.cadastrar(this.montarRequisicao()).subscribe({
      next: () => {
        this.notificacaoService.sucesso('Categoria cadastrada com sucesso!');
        this.router.navigate(['/categorias']);
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected cancelar(): void {
    this.router.navigate(['/categorias']);
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
