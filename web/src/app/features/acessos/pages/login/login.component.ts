import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@core/auth/services/auth.service';
import { of, switchMap } from 'rxjs';
import { RedefinirSenhaModalComponent } from '../../components/redefinir-senha-modal/redefinir-senha-modal.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RedefinirSenhaModalComponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly carregando = signal(false);
  protected readonly mostrarSenha = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);
  protected readonly exibirModalRedefinir = signal(false);

  protected readonly form = this.fb.group({
    nomeUsuario: ['', [Validators.required]],
    senha: ['', [Validators.required]],
  });

  protected alternarVisibilidadeSenha(): void {
    this.mostrarSenha.update((v) => !v);
  }

  protected entrar(): void {
    if (this.form.invalid || this.carregando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set(null);

    const { nomeUsuario, senha } = this.form.getRawValue();

    this.authService
      .login({ nomeUsuario, senha })
      .pipe(
        switchMap((resposta) => {
          if (resposta.redefinirSenha !== undefined) {
            return of(resposta.redefinirSenha);
          }
          return this.authService.consultarRedefinirSenha();
        }),
      )
      .subscribe({
        next: (precisaRedefinir) => {
          this.carregando.set(false);
          if (precisaRedefinir) {
            this.exibirModalRedefinir.set(true);
          } else {
            this.router.navigate(['/']);
          }
        },
        error: (erro: HttpErrorResponse) => {
          this.carregando.set(false);
          this.tratarErroLogin(erro);
        },
      });
  }

  protected aoConcluirRedefinicao(): void {
    this.exibirModalRedefinir.set(false);
    this.router.navigate(['/']);
  }

  private tratarErroLogin(erro: HttpErrorResponse): void {
    if (erro.status === 401) {
      this.mensagemErro.set('Nome de usuário ou senha incorretos, ou conta inativa.');
      return;
    }

    const corpo = erro.error as { detail?: string; title?: string } | undefined;
    if (erro.status === 400) {
      this.mensagemErro.set(corpo?.detail || 'Dados de entrada inválidos. Verifique os campos.');
      return;
    }

    if (erro.status === 0) {
      this.mensagemErro.set('Não foi possível conectar ao servidor. Verifique sua conexão.');
      return;
    }

    this.mensagemErro.set(
      corpo?.detail || 'Ocorreu um erro ao realizar o login. Tente novamente mais tarde.',
    );
  }
}
