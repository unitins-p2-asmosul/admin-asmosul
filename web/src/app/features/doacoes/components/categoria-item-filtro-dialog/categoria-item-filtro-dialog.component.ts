import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { CategoriaItemFiltros } from '../../models/categoria-item.model';

@Component({
  selector: 'app-categoria-item-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './categoria-item-filtro-dialog.component.html',
})
export class CategoriaItemFiltroDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(
    MatDialogRef<CategoriaItemFiltroDialogComponent, CategoriaItemFiltros>,
  );
  private readonly filtrosAtuais = inject<CategoriaItemFiltros>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais.nome ?? ''],
    descricao: [this.filtrosAtuais.descricao ?? ''],
  });

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.dialogRef.close({
      nome: valores.nome.trim() || undefined,
      descricao: valores.descricao.trim() || undefined,
    });
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
