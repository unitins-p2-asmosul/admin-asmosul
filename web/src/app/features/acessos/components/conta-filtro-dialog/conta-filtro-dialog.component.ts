import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ContaFiltros, PERFIS_CODIGOS, PerfilCodigo, PerfilDisponivel } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';

@Component({
  selector: 'app-conta-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './conta-filtro-dialog.component.html',
})
export class ContaFiltroDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly contaService = inject(ContaService);
  private readonly dialogRef = inject(MatDialogRef<ContaFiltroDialogComponent, ContaFiltros>);
  private readonly filtrosAtuais = inject<ContaFiltros>(MAT_DIALOG_DATA);

  protected readonly perfis = signal<PerfilDisponivel[]>([]);
  protected readonly opcoesRedefinirSenha = [
    { valor: 'true', rotulo: 'Pendente' },
    { valor: 'false', rotulo: 'Não pendente' },
  ];
  protected readonly codigosPerfil = PERFIS_CODIGOS;

  protected readonly form = this.fb.group({
    nomePessoa: [this.filtrosAtuais.nomePessoa ?? ''],
    nomeUsuario: [this.filtrosAtuais.nomeUsuario ?? ''],
    email: [this.filtrosAtuais.email ?? ''],
    perfis: [this.filtrosAtuais.perfis ?? [] as PerfilCodigo[]],
    redefinirSenha: [
      this.filtrosAtuais.redefinirSenha === undefined ? '' : String(this.filtrosAtuais.redefinirSenha),
    ],
    dataCriacao: [paraCampoData(this.filtrosAtuais.dataCriacao)],
    dataInativo: [paraCampoData(this.filtrosAtuais.dataInativo)],
  });

  constructor() {
    this.contaService.listarPerfis().subscribe({
      next: (perfis) => this.perfis.set(perfis),
      error: () => this.perfis.set(this.codigosPerfil.map((codigo) => ({ codigo, descricao: codigo }))),
    });
  }

  protected aplicar(): void {
    const valores = this.form.getRawValue();
    this.dialogRef.close({
      nomePessoa: valores.nomePessoa.trim() || undefined,
      nomeUsuario: valores.nomeUsuario.trim() || undefined,
      email: valores.email.trim() || undefined,
      perfis: valores.perfis.length ? valores.perfis : undefined,
      redefinirSenha: valores.redefinirSenha === '' ? undefined : valores.redefinirSenha === 'true',
      dataCriacao: paraApiData(valores.dataCriacao),
      dataInativo: paraApiData(valores.dataInativo),
    });
  }

  protected limpar(): void {
    this.dialogRef.close({});
  }
}

function paraCampoData(data?: string): string {
  if (!data) return '';
  const [dia, mes, ano] = data.split('-');
  return `${ano}-${mes}-${dia}`;
}

function paraApiData(data: string): string | undefined {
  if (!data) return undefined;
  const [ano, mes, dia] = data.split('-');
  return `${dia}-${mes}-${ano}`;
}