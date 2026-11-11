import { Component, effect, inject, input, OnInit, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ComorbidadeResumo } from '@features/pessoas/models/comorbidade.model';
import { ComorbidadeService } from '@features/pessoas/services/comorbidade.service';
import { PessoaComorbidadeFiltros } from '../../models/relatorio-pessoas.model';

@Component({
  selector: 'app-comorbidades-filtro',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './comorbidades-filtro.component.html',
})
export class ComorbidadesFiltroComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly comorbidadeService = inject(ComorbidadeService);

  readonly filtrosIniciais = input<PessoaComorbidadeFiltros>({});
  readonly aoFiltrar = output<PessoaComorbidadeFiltros>();

  protected readonly comorbidadesOpcoes = signal<ComorbidadeResumo[]>([]);

  protected readonly form = this.fb.group({
    termoBusca: [''],
    comorbidadeIds: [[] as number[]],
  });

  constructor() {
    effect(() => {
      const iniciais = this.filtrosIniciais();
      this.form.patchValue(
        {
          termoBusca: iniciais.termoBusca ?? '',
          comorbidadeIds: iniciais.comorbidadeIds ?? [],
        },
        { emitEvent: false },
      );
    });
  }

  ngOnInit(): void {
    this.comorbidadeService.listarTodas().subscribe({
      next: (lista) => this.comorbidadesOpcoes.set(lista),
    });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.aoFiltrar.emit({
      termoBusca: valores.termoBusca.trim() || undefined,
      comorbidadeIds: valores.comorbidadeIds.length > 0 ? valores.comorbidadeIds : undefined,
    });
  }

  protected limpar(): void {
    this.form.reset({
      termoBusca: '',
      comorbidadeIds: [],
    });
    this.aplicar();
  }
}
