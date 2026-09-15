import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  ESCOLARIDADE_OPCOES,
  RENDA_FAMILIAR_OPCOES,
  SEXO_OPCOES,
  PessoaFiltros,
} from '../../models/pessoa.model';
import { PessoaService } from '../../services/pessoa.service';

@Component({
  selector: 'app-pessoa-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatSelectModule,
  ],
  template: `
    <h2 mat-dialog-title class="pessoa-filtro__titulo">
      <span class="material-icons" aria-hidden="true">tune</span>
      <span>Filtrar Pessoas</span>
    </h2>

    <mat-dialog-content class="pessoa-filtro__content">
      <form [formGroup]="form" class="pessoa-filtro__form grid gap-4 sm:grid-cols-2">
        <h3 class="pessoa-filtro__section-title sm:col-span-2">Identificação</h3>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="nome" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>CPF/CNPJ</mat-label>
          <input matInput formControlName="cpfCnpj" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Telefone</mat-label>
          <input matInput formControlName="telefone" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email" />
        </mat-form-field>

        <h3 class="pessoa-filtro__section-title sm:col-span-2">Dados pessoais</h3>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Data de nascimento</mat-label>
          <input matInput placeholder="dd-mm-aaaa" formControlName="dataNascimento" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Tipo de pessoa</mat-label>
          <mat-select formControlName="tipoPessoa">
            <mat-option value="">Todos</mat-option>
            <mat-option value="FISICA">Física</mat-option>
            <mat-option value="JURIDICA">Jurídica</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Sexo</mat-label>
          <mat-select formControlName="sexo">
            <mat-option value="">Todos</mat-option>
            @for (opcao of sexoOpcoes; track opcao.codigo) {
              <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Escolaridade</mat-label>
          <mat-select formControlName="escolaridade">
            <mat-option value="">Todas</mat-option>
            @for (opcao of escolaridadeOpcoes; track opcao.codigo) {
              <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Renda familiar</mat-label>
          <mat-select formControlName="rendaFamiliar">
            <mat-option value="">Todas</mat-option>
            @for (opcao of rendaFamiliarOpcoes; track opcao.codigo) {
              <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Profissão</mat-label>
          <input matInput formControlName="profissao" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Bairro</mat-label>
          <input matInput formControlName="bairro" />
        </mat-form-field>

        <h3 class="pessoa-filtro__section-title sm:col-span-2">Relacionamentos e status</h3>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Comorbidade</mat-label>
          <mat-select formControlName="comorbidadeId">
            <mat-option value="">Todas</mat-option>
            @for (opcao of comorbidades(); track opcao.id) {
              <mat-option [value]="opcao.id">{{ opcao.nome }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Categoria</mat-label>
          <mat-select formControlName="categoriaId">
            <mat-option value="">Todas</mat-option>
            @for (opcao of categorias(); track opcao.id) {
              <mat-option [value]="opcao.id">{{ opcao.nome }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Quantidade de coabitantes</mat-label>
          <input matInput type="number" min="0" formControlName="quantidadeCoabitantes" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Beneficiário?</mat-label>
          <mat-select formControlName="ehBeneficiario">
            <mat-option value="">Todos</mat-option>
            <mat-option value="true">Sim</mat-option>
            <mat-option value="false">Não</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Doador?</mat-label>
          <mat-select formControlName="ehDoador">
            <mat-option value="">Todos</mat-option>
            <mat-option value="true">Sim</mat-option>
            <mat-option value="false">Não</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-checkbox class="sm:col-span-2" formControlName="apenasInativos">Apenas inativos</mat-checkbox>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="center" class="pessoa-filtro__actions">
      <button mat-flat-button type="button" class="pessoa-filtro__button" (click)="limpar()">Limpar</button>
      <button mat-flat-button color="primary" type="button" class="pessoa-filtro__button" (click)="aplicar()">
        Aplicar Filtros
      </button>
    </mat-dialog-actions>
  `,
})
export class PessoaFiltroDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly pessoaService = inject(PessoaService);
  private readonly dialogRef = inject(
    MatDialogRef<PessoaFiltroDialogComponent, PessoaFiltros>,
  );
  private readonly filtrosAtuais = inject<PessoaFiltros>(MAT_DIALOG_DATA, { optional: true });

  protected readonly sexoOpcoes = SEXO_OPCOES;
  protected readonly escolaridadeOpcoes = ESCOLARIDADE_OPCOES;
  protected readonly rendaFamiliarOpcoes = RENDA_FAMILIAR_OPCOES;
  protected readonly comorbidades = signal<{ id: number; nome: string }[]>([]);
  protected readonly categorias = signal<{ id: number; nome: string }[]>([]);

  protected readonly form = this.fb.group({
    nome: [this.filtrosAtuais?.nome ?? ''],
    cpfCnpj: [this.filtrosAtuais?.cpfCnpj ?? ''],
    tipoPessoa: [this.filtrosAtuais?.tipoPessoa ?? ''],
    dataNascimento: [this.filtrosAtuais?.dataNascimento ?? ''],
    sexo: [this.filtrosAtuais?.sexo ?? ''],
    telefone: [this.filtrosAtuais?.telefone ?? ''],
    email: [this.filtrosAtuais?.email ?? ''],
    escolaridade: [this.filtrosAtuais?.escolaridade ?? ''],
    profissao: [this.filtrosAtuais?.profissao ?? ''],
    bairro: [this.filtrosAtuais?.bairro ?? ''],
    rendaFamiliar: [this.filtrosAtuais?.rendaFamiliar ?? ''],
    comorbidadeId: [this.filtrosAtuais?.comorbidadeId?.toString() ?? ''],
    categoriaId: [this.filtrosAtuais?.categoriaId?.toString() ?? ''],
    quantidadeCoabitantes: [this.filtrosAtuais?.quantidadeCoabitantes?.toString() ?? ''],
    ehBeneficiario: [this.filtrosAtuais?.ehBeneficiario === undefined ? '' : String(this.filtrosAtuais.ehBeneficiario)],
    ehDoador: [this.filtrosAtuais?.ehDoador === undefined ? '' : String(this.filtrosAtuais.ehDoador)],
    apenasInativos: [this.filtrosAtuais?.apenasInativos ?? false],
  });

  constructor() {
    this.pessoaService.listarComorbidades().subscribe((itens) => this.comorbidades.set(itens));
    this.pessoaService.listarCategorias().subscribe((itens) => this.categorias.set(itens));
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.dialogRef.close({
      nome: valores.nome.trim() || undefined,
      cpfCnpj: valores.cpfCnpj.trim() || undefined,
      tipoPessoa: valores.tipoPessoa || undefined,
      dataNascimento: valores.dataNascimento.trim() || undefined,
      sexo: valores.sexo || undefined,
      telefone: valores.telefone.trim() || undefined,
      email: valores.email.trim() || undefined,
      escolaridade: valores.escolaridade || undefined,
      profissao: valores.profissao.trim() || undefined,
      bairro: valores.bairro.trim() || undefined,
      rendaFamiliar: valores.rendaFamiliar || undefined,
      comorbidadeId: valores.comorbidadeId ? Number(valores.comorbidadeId) : undefined,
      categoriaId: valores.categoriaId ? Number(valores.categoriaId) : undefined,
      quantidadeCoabitantes: valores.quantidadeCoabitantes ? Number(valores.quantidadeCoabitantes) : undefined,
      ehBeneficiario: valores.ehBeneficiario === '' ? undefined : valores.ehBeneficiario === 'true',
      ehDoador: valores.ehDoador === '' ? undefined : valores.ehDoador === 'true',
      apenasInativos: valores.apenasInativos || undefined,
    });
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}
