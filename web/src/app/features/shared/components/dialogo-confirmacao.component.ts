import { Component, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';

export interface OptionDialogoConfirmacaoData {
  titulo: string;
  mensagem: string;
}

@Component({
  selector: 'app-dialogo-confirmacao',
  templateUrl: 'dialogo-confirmacao.component.html',
  imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton],
})
export class DialogoConfirmacaoComponent {
  readonly dialogRef = inject(MatDialogRef<DialogoConfirmacaoComponent>);
  readonly data = inject<OptionDialogoConfirmacaoData>(MAT_DIALOG_DATA);

  fechar(): void {
    this.dialogRef.close();
  }
}
