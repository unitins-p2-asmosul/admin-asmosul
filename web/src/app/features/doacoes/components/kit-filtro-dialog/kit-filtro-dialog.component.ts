import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ItemResumo } from '../../models/item.model';
import { KitFiltros } from '../../models/kit.model';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-kit-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './kit-filtro-dialog.component.html',
})
export class KitFiltroDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly itemService = inject(ItemService);
  private readonly dialogRef = inject(MatDialogRef<KitFiltroDialogComponent, KitFiltros>);
  private readonly filtrosAtuais = inject<KitFiltros>(MAT_DIALOG_DATA);

  protected readonly itens = signal<ItemResumo[]>([]);

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais.nome ?? ''],
    descricao: [this.filtrosAtuais.descricao ?? ''],
    itensIds: [this.filtrosAtuais.itensIds ?? ([] as number[])],
  });

  ngOnInit(): void {
    this.carregarItens();
  }

  private carregarItens(): void {
    this.itemService.listarTodos(false).subscribe({
      next: (itens) => this.itens.set(itens),
    });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    const filtros: KitFiltros = {
      nome: valores.nome.trim() || undefined,
      descricao: valores.descricao.trim() || undefined,
      itensIds: valores.itensIds.length > 0 ? valores.itensIds : undefined,
    };
    this.dialogRef.close(filtros);
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
