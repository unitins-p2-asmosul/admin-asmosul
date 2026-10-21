import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import { ItemResumo } from '../../models/item.model';
import { MovimentacaoFiltros } from '../../models/movimentacao.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-movimentacao-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './movimentacao-filtro-dialog.component.html',
})
export class MovimentacaoFiltroDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(
    MatDialogRef<MovimentacaoFiltroDialogComponent, MovimentacaoFiltros>,
  );
  private readonly filtrosAtuais = inject<MovimentacaoFiltros>(MAT_DIALOG_DATA);
  private readonly itemService = inject(ItemService);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly notificacao = inject(NotificacaoService);

  protected readonly itens = signal<ItemResumo[]>([]);
  protected readonly enderecos = signal<EnderecoArmazenamentoResumo[]>([]);
  protected readonly form = this.fb.group({
    itemId: [this.filtrosAtuais.itemId ?? (null as number | null)],
    localAntigoId: [this.filtrosAtuais.localAntigoId ?? (null as number | null)],
    localNovoId: [this.filtrosAtuais.localNovoId ?? (null as number | null)],
    quantidade: [
      this.filtrosAtuais.quantidade ?? null,
      [Validators.min(1), Validators.pattern(/^\d+$/)],
    ],
    dataHoraInicio: [this.filtrosAtuais.dataHoraInicio ?? ''],
    dataHoraFim: [this.filtrosAtuais.dataHoraFim ?? ''],
  });

  constructor() {
    this.itemService.listarTodos().subscribe({
      next: (itens) => this.itens.set(itens),
      error: () => this.notificacao.erro('Não foi possível carregar os itens para os filtros.'),
    });
    this.enderecoService.listarTodas().subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
      error: () => this.notificacao.erro('Não foi possível carregar os endereços para os filtros.'),
    });
  }

  protected aplicar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valores = this.form.getRawValue();
    if (
      valores.dataHoraInicio &&
      valores.dataHoraFim &&
      valores.dataHoraInicio > valores.dataHoraFim
    ) {
      this.form.controls.dataHoraFim.setErrors({ periodo: true });
      return;
    }
    this.dialogRef.close({
      itemId: valores.itemId ?? undefined,
      localAntigoId: valores.localAntigoId ?? undefined,
      localNovoId: valores.localNovoId ?? undefined,
      quantidade: valores.quantidade ?? undefined,
      dataHoraInicio: valores.dataHoraInicio || undefined,
      dataHoraFim: valores.dataHoraFim || undefined,
    });
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
