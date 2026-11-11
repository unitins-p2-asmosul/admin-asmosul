import { Component, inject, input, OnInit, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CategoriaItemService } from '@features/doacoes/services/categoria-item.service';
import { UNIDADE_MEDIDA_OPCOES } from '@features/doacoes/models/unidade-medida.model';
import { toSignal } from '@angular/core/rxjs-interop';
import { GiroEstoqueFiltros } from '../../models/relatorio-doacoes.model';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-itens-filtro',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './itens-filtro.component.html',
})
export class ItensFiltroComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaItemService);

  readonly filtrosIniciais = input<GiroEstoqueFiltros>();
  readonly aoFiltrar = output<GiroEstoqueFiltros>();

  protected readonly unidadesMedida = UNIDADE_MEDIDA_OPCOES;

  protected readonly categorias = toSignal(
    this.categoriaService.listar({
      page: 0, size: 99999
    }).pipe(
      map((lista) => lista.dados || [])
    ),
    { initialValue: [] }
  );

  protected readonly form = this.fb.group({
    nomeItem: [''],
    categoriaId: [null as number | null],
    unidadeMedida: [''],
    saldoEstoqueMin: [null as number | null],
    saldoEstoqueMax: [null as number | null],
    totalEntradasMin: [null as number | null],
    totalEntradasMax: [null as number | null],
    totalSaidasMin: [null as number | null],
    totalSaidasMax: [null as number | null],
  });

  ngOnInit(): void {
    const iniciais = this.filtrosIniciais();
    if (iniciais) {
      this.form.patchValue({
        nomeItem: iniciais.nomeItem ?? '',
        categoriaId: iniciais.categoriaId ?? null,
        unidadeMedida: iniciais.unidadeMedida ?? '',
        saldoEstoqueMin: iniciais.saldoEstoqueMin ?? null,
        saldoEstoqueMax: iniciais.saldoEstoqueMax ?? null,
        totalEntradasMin: iniciais.totalEntradasMin ?? null,
        totalEntradasMax: iniciais.totalEntradasMax ?? null,
        totalSaidasMin: iniciais.totalSaidasMin ?? null,
        totalSaidasMax: iniciais.totalSaidasMax ?? null,
      });
    }
  }

  protected submeter(): void {
    const valores = this.form.value;
    this.aoFiltrar.emit({
      nomeItem: valores.nomeItem?.trim() || undefined,
      categoriaId: valores.categoriaId ?? undefined,
      unidadeMedida: valores.unidadeMedida || undefined,
      saldoEstoqueMin: valores.saldoEstoqueMin ?? undefined,
      saldoEstoqueMax: valores.saldoEstoqueMax ?? undefined,
      totalEntradasMin: valores.totalEntradasMin ?? undefined,
      totalEntradasMax: valores.totalEntradasMax ?? undefined,
      totalSaidasMin: valores.totalSaidasMin ?? undefined,
      totalSaidasMax: valores.totalSaidasMax ?? undefined,
    });
  }

  protected limpar(): void {
    this.form.reset({
      nomeItem: '',
      categoriaId: null,
      unidadeMedida: '',
      saldoEstoqueMin: null,
      saldoEstoqueMax: null,
      totalEntradasMin: null,
      totalEntradasMax: null,
      totalSaidasMin: null,
      totalSaidasMax: null,
    });
    this.submeter();
  }
}

