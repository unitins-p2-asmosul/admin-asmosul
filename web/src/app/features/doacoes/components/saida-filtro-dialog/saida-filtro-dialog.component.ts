import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { PessoaResumo } from '@features/pessoas/models/pessoa.model';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { ItemResumo } from '../../models/item.model';
import { SaidaFiltros } from '../../models/saida.model';
import { ItemService } from '../../services/item.service';

const TAMANHO_LISTA_PESSOAS = 1000;

@Component({
  selector: 'app-saida-filtro-dialog',
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
  templateUrl: './saida-filtro-dialog.component.html',
})
export class SaidaFiltroDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly itemService = inject(ItemService);
  private readonly pessoaService = inject(PessoaService);
  private readonly dialogRef = inject(MatDialogRef<SaidaFiltroDialogComponent, SaidaFiltros>);
  private readonly filtrosAtuais = inject<SaidaFiltros>(MAT_DIALOG_DATA);

  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly recebedores = signal<PessoaResumo[]>([]);

  protected readonly form = this.fb.group({
    itensIds: [this.filtrosAtuais.itensIds ?? ([] as number[])],
    recebedorId: [this.filtrosAtuais.recebedorId ?? (null as number | null)],
    quantidade: [
      this.filtrosAtuais.quantidade !== undefined
        ? this.filtrosAtuais.quantidade
        : (null as number | null),
    ],
    data: [this.filtrosAtuais.data ?? ''],
  });

  ngOnInit(): void {
    this.itemService.listarTodos().subscribe({
      next: (itens) => this.itens.set(itens),
    });

    this.pessoaService
      .listar({ page: 0, size: TAMANHO_LISTA_PESSOAS, sort: 'nome,asc' })
      .subscribe({
        next: (resposta) => this.recebedores.set(resposta.dados),
      });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    const filtros: SaidaFiltros = {
      itensIds: valores.itensIds.length > 0 ? valores.itensIds : undefined,
      recebedorId: valores.recebedorId ?? undefined,
      quantidade:
        valores.quantidade !== null && valores.quantidade !== undefined
          ? Number(valores.quantidade)
          : undefined,
      data: valores.data || undefined,
    };
    this.dialogRef.close(filtros);
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
