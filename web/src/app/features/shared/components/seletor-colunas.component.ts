import { Component, effect, input, model, OnInit } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

export interface OpcaoColuna {
  id: string;
  rotulo: string;
}

@Component({
  selector: 'app-seletor-colunas',
  standalone: true,
  imports: [MatMenuModule, MatButtonModule, MatCheckboxModule, MatIconModule],
  templateUrl: './seletor-colunas.component.html'
})
export class SeletorColunasComponent implements OnInit {
  storageKey = input<string>();
  colunasDisponiveis = input.required<OpcaoColuna[]>();
  colunasVisiveis = model.required<string[]>();

  constructor() {
    effect(() => {
      const chave = this.storageKey();
      const colunas = this.colunasVisiveis();

      if (chave && typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(`colunas_${chave}`, JSON.stringify(colunas));
      }
    });
  }

  ngOnInit(): void {
    const chave = this.storageKey();
    if (!chave || typeof window === 'undefined' || !window.localStorage) {
      return;
    }

    const salvo = localStorage.getItem(`colunas_${chave}`);
    if (salvo) {
      try {
        const colunasSalvas: string[] = JSON.parse(salvo);
        const colunasValidas = this.colunasDisponiveis().map(c => c.id);
        const filtradas = colunasSalvas.filter(c => colunasValidas.includes(c));

        if (filtradas.length > 0) {
          this.colunasVisiveis.set(filtradas);
        }
      } catch {
        localStorage.removeItem(`colunas_${chave}`);
      }
    }
  }

  alternarColuna(id: string): void {
    const ativas = [...this.colunasVisiveis()];
    const indice = ativas.indexOf(id);

    if (indice > -1) {
      if (ativas.length > 1) {
        ativas.splice(indice, 1);
      }
    } else {
      ativas.push(id);
    }

    this.colunasVisiveis.set(ativas);
  }
}
