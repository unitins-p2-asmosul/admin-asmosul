import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { ComorbidadeService } from '../../services/comorbidade.service';
import { ComorbidadePayload } from '../../models/comorbidade.model';

@Component({
  selector: 'app-comorbidade-cadastro-page',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatCardModule],
  templateUrl: './comorbidade-cadastro-page.component.html',
})
export class ComorbidadeCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly comorbidadeService = inject(ComorbidadeService);
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
    this.comorbidadeService.cadastrar(this.montarRequisicao()).subscribe({
      next: () => {
        this.notificacaoService.sucesso('Comorbidade cadastrada com sucesso!');
        this.router.navigate(['/comorbidades/novo']); // Mantém na mesma página por enquanto ou redireciona
        this.form.reset(); // Limpa o formulário simulando conclusão
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected cancelar(): void {
    this.form.reset();
  }

  private montarRequisicao(): ComorbidadePayload {
    const valores = this.form.getRawValue();
    return {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || undefined,
    };
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    if (corpo?.erros && corpo.erros.length > 0) {
      corpo.erros.forEach(({ campo, mensagem }) => {
        this.form.get(campo)?.setErrors({ backend: mensagem });
      });
    } else if (corpo?.detail) {
      this.notificacaoService.erro(corpo.detail);
    } else {
      this.notificacaoService.erro('Ocorreu um erro inesperado.');
    }
  }
}
