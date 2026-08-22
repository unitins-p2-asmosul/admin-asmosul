import {inject, Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {
  DialogoConfirmacaoComponent,
  OptionDialogoConfirmacaoData
} from '@features/shared/components/dialogo-confirmacao.component';
import {firstValueFrom} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class DialogoConfirmacaoService{
  private readonly dialog = inject(MatDialog);

  async confirmar(titulo: string, mensagem: string): Promise<boolean> {
    const dialogRef = this.dialog.open<DialogoConfirmacaoComponent, OptionDialogoConfirmacaoData, boolean>(
      DialogoConfirmacaoComponent,
      {
        data: { titulo, mensagem },
        width: '400px'
      }
    );

    const resultado = await firstValueFrom(dialogRef.afterClosed());
    return !!resultado;
  }
}
