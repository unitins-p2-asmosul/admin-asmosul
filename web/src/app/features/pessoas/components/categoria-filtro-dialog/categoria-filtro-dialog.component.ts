import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

export interface CategoriaFiltroData {
  nome?: string;
}

@Component({
  selector: 'app-categoria-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
  templateUrl: './categoria-filtro-dialog.component.html',
})
export class CategoriaFiltroDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CategoriaFiltroDialogComponent>);
  private readonly data = inject<CategoriaFiltroData>(MAT_DIALOG_DATA, { optional: true });

  protected readonly form = this.fb.nonNullable.group({
    nome: [this.data?.nome ?? ''],
  });

  protected aplicar(): void {
    this.dialogRef.close(this.form.getRawValue());
  }

  protected limpar(): void {
    this.dialogRef.close({ nome: '' });
  }
}