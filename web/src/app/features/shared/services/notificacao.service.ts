import {inject, Injectable} from '@angular/core';
import {MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition} from '@angular/material/snack-bar';

export interface NotificacaoOpcoes {
  duracao?: number;
  posicaoVertical?: MatSnackBarVerticalPosition;
  posicaoHorizontal?: MatSnackBarHorizontalPosition;
}

@Injectable({
  providedIn: 'root'
})
export class NotificacaoService{
  private readonly snackbar = inject(MatSnackBar);

  sucesso(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Fechar', { ...opcoes });
  }

  alerta(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Atenção', { ...opcoes });
  }

  erro(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Fechar', { duracao: 5000, ...opcoes });
  }

  private exibir(mensagem: string, acao: string, opcoes?: NotificacaoOpcoes): void {
    this.snackbar.open(mensagem, acao, {
      duration: opcoes?.duracao ?? 3500,
      verticalPosition: opcoes?.posicaoVertical ?? 'top',
      horizontalPosition: opcoes?.posicaoHorizontal ?? 'center'
    });
  }
}
