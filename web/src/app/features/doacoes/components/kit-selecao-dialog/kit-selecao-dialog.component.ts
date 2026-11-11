import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs/operators';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import { obterSiglaUnidade } from '../../models/item.model';
import { KitDisponibilidade, KitResumo } from '../../models/kit.model';
import { LinhaItemSaida } from '../../pages/saida-cadastro-page/saida-cadastro-page.component';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { KitService } from '../../services/kit.service';

export interface KitSelecaoDialogData {
  itensJaIncluidos: LinhaItemSaida[];
}

export interface ItemKitPreviaLocal {
  idItem: number;
  nomeItem: string;
  sigla: string;
  quantidadePorKit: number;
  saldoRestanteNoLocal: number;
  kitsPossiveisItem: number;
}

@Component({
  selector: 'app-kit-selecao-dialog',
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
  templateUrl: './kit-selecao-dialog.component.html',
})
export class KitSelecaoDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly kitService = inject(KitService);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly dialogRef = inject(
    MatDialogRef<KitSelecaoDialogComponent, LinhaItemSaida[] | undefined>,
  );
  private readonly data = inject<KitSelecaoDialogData | null>(MAT_DIALOG_DATA, { optional: true });

  protected readonly kits = signal<KitResumo[]>([]);
  protected readonly enderecos = signal<EnderecoArmazenamentoResumo[]>([]);
  protected readonly disponibilidadeBruta = signal<KitDisponibilidade | null>(null);
  protected readonly carregandoDisponibilidade = signal(false);

  protected readonly form = this.fb.group({
    idKit: [null as number | null, [Validators.required]],
    idArmazenamento: [null as number | null, [Validators.required]],
    quantidadeKits: [1, [Validators.required, Validators.min(1)]],
  });

  private readonly idKitSelecionado = toSignal(this.form.controls.idKit.valueChanges, {
    initialValue: null as number | null,
  });

  private readonly idArmazenamentoSelecionado = toSignal(
    this.form.controls.idArmazenamento.valueChanges,
    {
      initialValue: null as number | null,
    },
  );

  private readonly quantidadeKitsDigitada = toSignal(
    this.form.controls.quantidadeKits.valueChanges,
    {
      initialValue: 1,
    },
  );

  protected readonly itensPrevia = computed<ItemKitPreviaLocal[]>(() => {
    const disp = this.disponibilidadeBruta();
    const idLocal = this.idArmazenamentoSelecionado();
    if (!disp || !idLocal) return [];

    const jaIncluidos = this.data?.itensJaIncluidos ?? [];

    return disp.itens.map((item) => {
      const reservadoNaLista = jaIncluidos
        .filter((l) => l.idItem === item.idItem && l.idArmazenamento === idLocal)
        .reduce((soma, l) => soma + l.quantidade, 0);

      const saldoRestanteNoLocal = Math.max(0, (item.saldoEstoque ?? 0) - reservadoNaLista);
      const kitsPossiveisItem =
        item.quantidadePorKit > 0
          ? Math.max(0, Math.floor(saldoRestanteNoLocal / item.quantidadePorKit))
          : 0;

      return {
        idItem: item.idItem,
        nomeItem: item.nomeItem,
        sigla: obterSiglaUnidade(item.unidadeMedida),
        quantidadePorKit: item.quantidadePorKit,
        saldoRestanteNoLocal,
        kitsPossiveisItem,
      };
    });
  });

  protected readonly maxKitsPossiveis = computed<number>(() => {
    const itens = this.itensPrevia();
    if (itens.length === 0) return 0;
    return itens.reduce(
      (minimo, item) => Math.min(minimo, item.kitsPossiveisItem),
      Number.MAX_SAFE_INTEGER,
    );
  });

  protected readonly podeConfirmar = computed<boolean>(() => {
    const max = this.maxKitsPossiveis();
    const qtd = Number(this.quantidadeKitsDigitada() ?? 0);
    return (
      !!this.idKitSelecionado() &&
      !!this.idArmazenamentoSelecionado() &&
      !this.carregandoDisponibilidade() &&
      max > 0 &&
      qtd >= 1 &&
      qtd <= max
    );
  });

  constructor() {
    effect(() => {
      const idKit = this.idKitSelecionado();
      const idLocal = this.idArmazenamentoSelecionado();
      this.disponibilidadeBruta.set(null);

      if (!idKit || !idLocal) return;

      this.carregandoDisponibilidade.set(true);
      this.kitService
        .consultarDisponibilidade(idKit, idLocal)
        .pipe(finalize(() => this.carregandoDisponibilidade.set(false)))
        .subscribe({
          next: (disp) => {
            this.disponibilidadeBruta.set(disp);
          },
          error: () => {
            this.disponibilidadeBruta.set(null);
          },
        });
    });
  }

  ngOnInit(): void {
    this.kitService.listarTodos(false).subscribe({
      next: (kits) => this.kits.set(kits),
    });

    this.enderecoService.listarTodas().subscribe({
      next: (enderecos) => this.enderecos.set(enderecos),
    });
  }

  protected confirmar(): void {
    if (!this.podeConfirmar()) {
      this.form.markAllAsTouched();
      return;
    }

    const idLocal = this.idArmazenamentoSelecionado()!;
    const endereco = this.enderecos().find((e) => e.id === idLocal);
    const nomeArmazenamento =
      endereco?.nome ?? this.disponibilidadeBruta()?.nomeLocalArmazenamento ?? 'Endereço';
    const qtdKits = Number(this.form.controls.quantidadeKits.getRawValue());

    const itensDesmembrados: LinhaItemSaida[] = this.itensPrevia().map((item) => ({
      idItem: item.idItem,
      nomeItem: item.nomeItem,
      sigla: item.sigla,
      quantidade: item.quantidadePorKit * qtdKits,
      idArmazenamento: idLocal,
      nomeArmazenamento,
    }));

    this.dialogRef.close(itensDesmembrados);
  }

  protected cancelar(): void {
    this.dialogRef.close(undefined);
  }
}
