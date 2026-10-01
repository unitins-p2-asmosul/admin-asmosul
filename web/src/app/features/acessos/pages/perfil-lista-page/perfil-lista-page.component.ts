import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Location } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { PERFIS_CODIGOS, PerfilCodigo, PerfilDisponivel } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';

interface PerfilListaItem extends PerfilDisponivel {
  nome: string;
}

const PERFIS_PADRAO: PerfilListaItem[] = [
  perfil('GERENCIADOR_PESSOAS', 'Gerenciador de Pessoas', 'pessoas'),
  perfil('GERENCIADOR_DOACOES', 'Gerenciador de Doações', 'doações'),
  perfil('GERENCIADOR_CAPACITACOES', 'Gerenciador de Capacitações', 'capacitações'),
  perfil('GERENCIADOR_ACESSO', 'Gerenciador de Acesso', 'acesso'),
  perfil('GERENCIADOR_RELATORIOS', 'Gerenciador de Relatórios', 'relatórios'),
];

@Component({
  selector: 'app-perfil-lista-page',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule],
  templateUrl: './perfil-lista-page.component.html',
})
export class PerfilListaPageComponent {
  private readonly contaService = inject(ContaService);
  private readonly location = inject(Location);
  protected readonly perfis = signal<PerfilListaItem[]>(PERFIS_PADRAO);
  protected readonly colunas = ['indice', 'nome', 'descricao'];

  constructor() {
    this.contaService.listarPerfis().subscribe({
      next: (perfis) => this.perfis.set(this.comporLista(perfis)),
    });
  }

  protected voltar(): void {
    this.location.back();
  }

  private comporLista(perfis: PerfilDisponivel[]): PerfilListaItem[] {
    return PERFIS_CODIGOS.map((codigo) => {
      const padrao = PERFIS_PADRAO.find((item) => item.codigo === codigo)!;
      const recebido = perfis.find((item) => item.codigo === codigo);
      return { ...padrao, nome: recebido?.descricao ?? padrao.nome };
    });
  }
}

function perfil(codigo: PerfilCodigo, nome: string, modulo: string): PerfilListaItem {
  return {
    codigo,
    nome,
    descricao: `Possui acesso a todo o módulo de ${modulo} e suas funções`,
  };
}