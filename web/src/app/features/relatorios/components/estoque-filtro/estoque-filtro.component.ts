import { Component, inject, input, OnInit, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { CategoriaItemService } from '@features/doacoes/services/categoria-item.service';
import { EnderecoArmazenamentoService } from '@features/doacoes/services/endereco-armazenamento.service';
import { InventarioEstoqueFiltros } from '../../models/relatorio-doacoes.model';

@Component({
  selector: 'app-estoque-filtro',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './estoque-filtro.component.html',
})
export class EstoqueFiltroComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaItemService);
  private readonly localService = inject(EnderecoArmazenamentoService);

  readonly filtrosIniciais = input<InventarioEstoqueFiltros>();
  readonly aoFiltrar = output<InventarioEstoqueFiltros>();

  protected readonly categorias = toSignal(
    this.categoriaService.listar({
      page: 0, size: 99999
    }).pipe(map((res) => res.dados || [])),
    { initialValue: [] },
  );

  protected readonly locais = toSignal(
    this.localService.listar({
      page: 0, size: 99999,
    }).pipe(map((res) => res.dados || [])),
    { initialValue: [] },
  );

  protected readonly form = this.fb.group({
    nomeItem: [''],
    categoriasIds: [[] as number[]],
    locaisIds: [[] as number[]],
    quantidadeMinima: [null as number | null],
    valorMinimo: [null as number | null],
  });

  ngOnInit(): void {
    const iniciais = this.filtrosIniciais();
    if (iniciais) {
      this.form.patchValue({
        nomeItem: iniciais.nomeItem ?? '',
        categoriasIds: iniciais.categoriasIds ?? [],
        locaisIds: iniciais.locaisIds ?? [],
        quantidadeMinima: iniciais.quantidadeMinima ?? null,
        valorMinimo: iniciais.valorMinimo ?? null,
      });
    }
  }

  protected submeter(): void {
    const valores = this.form.value;
    this.aoFiltrar.emit({
      nomeItem: valores.nomeItem?.trim() || undefined,
      categoriasIds: valores.categoriasIds?.length ? valores.categoriasIds : undefined,
      locaisIds: valores.locaisIds?.length ? valores.locaisIds : undefined,
      quantidadeMinima: valores.quantidadeMinima ?? undefined,
      valorMinimo: valores.valorMinimo ?? undefined,
    });
  }

  protected limpar(): void {
    this.form.reset({
      nomeItem: '',
      categoriasIds: [],
      locaisIds: [],
      quantidadeMinima: null,
      valorMinimo: null,
    });
    this.submeter();
  }
}
