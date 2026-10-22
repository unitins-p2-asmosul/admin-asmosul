import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CategoriaItemResumo } from '../../models/categoria-item.model';
import { ItemFiltros } from '../../models/item.model';
import { CategoriaItemService } from '../../services/categoria-item.service';

@Component({
  selector: 'app-item-filtro-dialog',
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
  templateUrl: './item-filtro-dialog.component.html',
})
export class ItemFiltroDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly categoriaItemService = inject(CategoriaItemService);
  private readonly dialogRef = inject(MatDialogRef<ItemFiltroDialogComponent, ItemFiltros>);
  private readonly filtrosAtuais = inject<ItemFiltros>(MAT_DIALOG_DATA);

  protected readonly categorias = signal<CategoriaItemResumo[]>([]);

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais.nome ?? ''],
    categoriasIds: [this.filtrosAtuais.categoriasIds ?? ([] as number[])],
    estoque: [
      this.filtrosAtuais.estoque !== undefined
        ? this.filtrosAtuais.estoque
        : (null as number | null),
    ],
    precoUnitarioMinimo: [
      this.filtrosAtuais.precoUnitarioMinimo !== undefined
        ? this.filtrosAtuais.precoUnitarioMinimo / 100
        : (null as number | null),
    ],
    precoUnitarioMaximo: [
      this.filtrosAtuais.precoUnitarioMaximo !== undefined
        ? this.filtrosAtuais.precoUnitarioMaximo / 100
        : (null as number | null),
    ],
  });

  ngOnInit(): void {
    this.carregarCategorias();
  }

  private carregarCategorias(): void {
    this.categoriaItemService.listar({ page: 0, size: 100, sort: 'nome,asc' }).subscribe({
      next: (resposta) => {
        this.categorias.set(resposta.dados);
      },
    });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    const filtros: ItemFiltros = {
      nome: valores.nome.trim() || undefined,
      categoriasIds: valores.categoriasIds.length > 0 ? valores.categoriasIds : undefined,
      estoque:
        valores.estoque !== null && valores.estoque !== undefined
          ? Number(valores.estoque)
          : undefined,
      precoUnitarioMinimo:
        valores.precoUnitarioMinimo !== null && valores.precoUnitarioMinimo !== undefined
          ? Math.round(Number(valores.precoUnitarioMinimo) * 100)
          : undefined,
      precoUnitarioMaximo:
        valores.precoUnitarioMaximo !== null && valores.precoUnitarioMaximo !== undefined
          ? Math.round(Number(valores.precoUnitarioMaximo) * 100)
          : undefined,
    };
    this.dialogRef.close(filtros);
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
