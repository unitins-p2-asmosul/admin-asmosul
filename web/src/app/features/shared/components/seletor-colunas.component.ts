import { Component, input, OnInit, output, signal } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ColunaSeletor {
  id: string;
  titulo: string;
}

@Component({
  selector: 'app-seletor-colunas',
  standalone: true,
  imports: [MatButtonModule, MatCheckboxModule, MatIconModule, MatMenuModule],
  templateUrl: './seletor-colunas.component.html',
})
export class SeletorColunasComponent implements OnInit {
  readonly colunas = input.required<ColunaSeletor[]>();
  readonly chaveStorage = input.required<string>();
  readonly aoMudarSelecao = output<string[]>();

  protected readonly selecionadas = signal<string[]>([]);

  ngOnInit(): void {
    this.selecionadas.set(this.carregarSelecao());
    this.aoMudarSelecao.emit(this.selecionadas());
  }

  protected alternar(id: string, marcado: boolean): void {
    const ids = this.colunas()
      .filter((coluna) => (coluna.id === id ? marcado : this.selecionadas().includes(coluna.id)))
      .map((coluna) => coluna.id);

    this.selecionadas.set(ids);
    this.salvarSelecao(ids);
    this.aoMudarSelecao.emit(ids);
  }

  private carregarSelecao(): string[] {
    const todas = this.colunas().map((coluna) => coluna.id);

    try {
      const salvo = localStorage.getItem(this.obterChaveStorage());
      if (!salvo) return todas;

      const ids: unknown = JSON.parse(salvo);
      if (!Array.isArray(ids)) return todas;

      const selecionadas = todas.filter((id) => ids.includes(id));
      return selecionadas.length > 0 ? selecionadas : todas;
    } catch (erro) {
      console.error('Não foi possível carregar as colunas salvas.', erro);
      return todas;
    }
  }

  private salvarSelecao(ids: string[]): void {
    try {
      localStorage.setItem(this.obterChaveStorage(), JSON.stringify(ids));
    } catch (erro) {
      console.error('Não foi possível salvar as colunas selecionadas.', erro);
    }
  }

  private obterChaveStorage(): string {
    return `asmosul:colunas:${this.chaveStorage()}`;
  }
}
