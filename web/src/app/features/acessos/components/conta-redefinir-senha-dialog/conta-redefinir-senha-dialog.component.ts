import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { ContaService } from '../../services/conta.service';

export interface ContaRedefinirSenhaDialogData {
  contaId: number;
  nomePessoa: string;
  nomeUsuario: string;
}

@Component({
  selector: 'app-conta-redefinir-senha-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './conta-redefinir-senha-dialog.component.html',
})
export class ContaRedefinirSenhaDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly contaService = inject(ContaService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly dialogRef = inject(
    MatDialogRef<ContaRedefinirSenhaDialogComponent, boolean>,
  );
  protected readonly data = inject<ContaRedefinirSenhaDialogData>(MAT_DIALOG_DATA);
  protected readonly enviando = signal(false);

  protected readonly form = this.fb.group(
    {
      novaSenhaTemporaria: ['', Validators.required],
      confirmarNovaSenha: ['', Validators.required],
    },
    { validators: senhasConferem },
  );

  protected confirmar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.contaService
      .redefinirSenhaAdmin(this.data.contaId, this.form.controls.novaSenhaTemporaria.value)
      .subscribe({
        next: () => {
          this.notificacao.sucesso('Senha temporária redefinida com sucesso.');
          this.dialogRef.close(true);
        },
        error: (erro: HttpErrorResponse) => {
          this.enviando.set(false);
          this.notificacao.erro(
            (erro.error as { detail?: string } | undefined)?.detail
              ?? 'Não foi possível redefinir a senha temporária.',
          );
        },
      });
  }
}

function senhasConferem(controle: AbstractControl): ValidationErrors | null {
  const novaSenha = controle.get('novaSenhaTemporaria')?.value;
  const confirmacao = controle.get('confirmarNovaSenha')?.value;
  return novaSenha === confirmacao ? null : { senhasDiferentes: true };
}