import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_COMORBIDADES } from '../mocks/comorbidade.mock';
import { Comorbidade, ComorbidadePayload } from '../models/comorbidade.model';

@Injectable({
  providedIn: 'root',
})
export class ComorbidadeService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'comorbidades';
  private getComorbidades(): Comorbidade[] {
    try {
      const salvo = localStorage.getItem('MOCK_COMORBIDADES');
      if (salvo) {
        return JSON.parse(salvo);
      }
    } catch {
      // Ignora erro de acesso ao localStorage
    }
    return [...MOCK_COMORBIDADES];
  }

  private salvarNoStorage(lista: Comorbidade[]): void {
    try {
      localStorage.setItem('MOCK_COMORBIDADES', JSON.stringify(lista));
    } catch {
      // Ignora erro
    }
  }

  cadastrar(requisicao: ComorbidadePayload): Observable<Comorbidade> {
    if (environment.mockApi) {
      const nome = requisicao.nome.trim();
      const descricao = requisicao.descricao?.trim() || undefined;

      if (!nome) {
        return throwError(() => ({
          status: 400,
          error: {
            detail: 'Um ou mais campos não passaram na validação.',
            erros: [{ campo: 'nome', mensagem: 'O nome é obrigatório' }],
          },
        })).pipe(delay(300));
      }

      const lista = this.getComorbidades();
      const existe = lista.some(
        (comorbidade) => comorbidade.nome.localeCompare(nome, undefined, { sensitivity: 'base' }) === 0,
      );

      if (existe) {
        return throwError(() => ({
          status: 409,
          error: { detail: 'Já existe uma comorbidade cadastrada com este nome.' },
        })).pipe(delay(300));
      }

      const comorbidade: Comorbidade = {
        id: Math.max(0, ...lista.map((item) => item.id)) + 1,
        nome,
        descricao,
      };
      lista.push(comorbidade);
      this.salvarNoStorage(lista);
      return of(comorbidade).pipe(delay(300));
    }

    return this.http.post<Comorbidade>(this.endpoint, requisicao);
  }
}
