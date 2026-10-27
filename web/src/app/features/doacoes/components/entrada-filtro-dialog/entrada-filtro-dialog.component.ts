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
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import { EntradaFiltros } from '../../models/entrada.model';
import { ItemResumo } from '../../models/item.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { ItemService } from '../../services/item.service';

const TAMANHO_LISTA_DOADORES = 1000;

@Component({
  selector: 'app-entrada-filtro-dialog',
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
  templateUrl: './entrada-filtro-dialog.component.html',
})
export class EntradaFiltroDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly itemService = inject(ItemService);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly pessoaService = inject(PessoaService);
  private readonly dialogRef = inject(MatDialogRef<EntradaFiltroDialogComponent, EntradaFiltros>);
  private readonly filtrosAtuais = inject<EntradaFiltros>(MAT_DIALOG_DATA);

  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly enderecos = signal<EnderecoArmazenamentoResumo[]>([]);
  protected readonly doadores = signal<PessoaResumo[]>([]);

  protected readonly form = this.fb.group({
    itemId: [this.filtrosAtuais.itemId ?? (null as number | null)],
    doadorId: [this.filtrosAtuais.doadorId ?? (null as number | null)],
    enderecoId: [this.filtrosAtuais.enderecoId ?? (null as number | null)],
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

    this.enderecoService.listarTodas().subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
    });

    this.pessoaService
      .listar({ page: 0, size: TAMANHO_LISTA_DOADORES, sort: 'nome,asc' })
      .subscribe({
        next: (resposta) => this.doadores.set(resposta.dados),
      });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    const filtros: EntradaFiltros = {
      itemId: valores.itemId ?? undefined,
      doadorId: valores.doadorId ?? undefined,
      enderecoId: valores.enderecoId ?? undefined,
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
