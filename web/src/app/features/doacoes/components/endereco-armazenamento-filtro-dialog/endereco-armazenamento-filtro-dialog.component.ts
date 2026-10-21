import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EnderecoArmazenamentoFiltros } from '../../models/endereco-armazenamento.model';

@Component({
  selector: 'app-endereco-armazenamento-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './endereco-armazenamento-filtro-dialog.component.html',
})
export class EnderecoArmazenamentoFiltroDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(
    MatDialogRef<EnderecoArmazenamentoFiltroDialogComponent, EnderecoArmazenamentoFiltros>,
  );
  private readonly filtrosAtuais = inject<EnderecoArmazenamentoFiltros>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais.nome ?? ''],
    bairro: [this.filtrosAtuais.bairro ?? ''],
    logradouro: [this.filtrosAtuais.logradouro ?? ''],
  });

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.dialogRef.close({
      nome: valores.nome.trim() || undefined,
      bairro: valores.bairro.trim() || undefined,
      logradouro: valores.logradouro.trim() || undefined,
    });
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
