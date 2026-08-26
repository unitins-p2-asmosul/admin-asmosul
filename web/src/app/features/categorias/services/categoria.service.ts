import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_CATEGORIAS } from '../mocks/categoria.mock';
import { CategoriaDetalhe, CategoriaRequisicao } from '../models/categoria.model';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'categorias';
  private readonly categorias = [...MOCK_CATEGORIAS];

  cadastrar(requisicao: CategoriaRequisicao): Observable<CategoriaDetalhe> {
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

      const existe = this.categorias.some(
        (categoria) => categoria.nome.localeCompare(nome, undefined, { sensitivity: 'base' }) === 0,
      );

      if (existe) {
        return throwError(() => ({
          status: 409,
          error: { detail: 'Já existe uma categoria cadastrada com este nome.' },
        })).pipe(delay(300));
      }

      const categoria: CategoriaDetalhe = {
        id: Math.max(0, ...this.categorias.map((item) => item.id)) + 1,
        nome,
        descricao,
      };
      this.categorias.push(categoria);
      return of(categoria).pipe(delay(300));
    }

    return this.http.post<CategoriaDetalhe>(this.endpoint, requisicao);
  }
}
