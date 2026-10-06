import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, inject, output, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '@core/auth/services/auth.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';

export function senhasConferem(controle: AbstractControl): ValidationErrors | null {
  const novaSenha = controle.get('novaSenha')?.value;
  const confirmarNovaSenha = controle.get('confirmarNovaSenha')?.value;

  if (!novaSenha || !confirmarNovaSenha) {
    return null;
  }

  return novaSenha === confirmarNovaSenha ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-redefinir-senha-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './redefinir-senha-modal.component.html',
})
export class RedefinirSenhaModalComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly dialogRef = inject(MatDialogRef<RedefinirSenhaModalComponent, boolean>, {
    optional: true,
  });

  readonly sucesso = output<void>();

  protected readonly enviando = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);

  protected readonly mostrarSenhaAtual = signal(false);
  protected readonly mostrarNovaSenha = signal(false);
  protected readonly mostrarConfirmarNovaSenha = signal(false);

  protected readonly form = this.fb.group(
    {
      senhaAtual: ['', [Validators.required]],
      novaSenha: ['', [Validators.required, Validators.maxLength(72)]],
      confirmarNovaSenha: ['', [Validators.required]],
    },
    { validators: senhasConferem },
  );

  @HostListener('keydown.escape', ['$event'])
  protected evitarEscape(evento: Event): void {
    evento.preventDefault();
    evento.stopPropagation();
  }

  protected alternarVisibilidadeSenhaAtual(): void {
    this.mostrarSenhaAtual.update((v) => !v);
  }

  protected alternarVisibilidadeNovaSenha(): void {
    this.mostrarNovaSenha.update((v) => !v);
  }

  protected alternarVisibilidadeConfirmarNovaSenha(): void {
    this.mostrarConfirmarNovaSenha.update((v) => !v);
  }

  protected salvarNovaSenha(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.mensagemErro.set(null);

    const { senhaAtual, novaSenha } = this.form.getRawValue();

    this.authService.alterarMinhaSenha({ senhaAtual, novaSenha }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.notificacao.sucesso('Senha redefinida com sucesso.');
        this.sucesso.emit();
        this.dialogRef?.close(true);
      },
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        const erroCorpo = erro.error as
          { detail?: string; erros?: { campo: string; mensagem: string }[] } | undefined;

        const detalhe =
          erroCorpo?.detail ||
          erroCorpo?.erros?.[0]?.mensagem ||
          (erro.status === 400
            ? 'A senha atual está incorreta ou os dados informados são inválidos.'
            : 'Ocorreu um erro ao redefinir a senha. Tente novamente.');

        this.mensagemErro.set(detalhe);
      },
    });
  }
}
