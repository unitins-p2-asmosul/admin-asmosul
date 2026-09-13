import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ComorbidadeFiltros } from '../../models/comorbidade.model';

@Component({
  selector: 'app-comorbidade-filtro-dialog',
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
  templateUrl: './comorbidade-filtro-dialog.component.html',
})
export class ComorbidadeFiltroDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(
    MatDialogRef<ComorbidadeFiltroDialogComponent, ComorbidadeFiltros>,
  );
  private readonly filtrosAtuais = inject<ComorbidadeFiltros>(MAT_DIALOG_DATA, { optional: true });

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais?.nome ?? ''],
    apenasInativos: [this.filtrosAtuais?.apenasInativos ?? false],
  });

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.dialogRef.close({
      nome: valores.nome.trim() || undefined,
      apenasInativos: valores.apenasInativos || undefined,
    });
  }

  protected limpar(): void {
    this.form.reset();
    this.dialogRef.close({ nome: undefined, apenasInativos: undefined });
  }
}
