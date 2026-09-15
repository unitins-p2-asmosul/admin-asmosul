import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_CATEGORIAS } from '@features/pessoas/mocks/categoria.mock';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import {
  CategoriaConsultaParametros,
  CategoriaDetalhe,
  CategoriaRequisicao,
  CategoriaResumo,
} from '@features/pessoas/models/categoria.model';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'categorias';
  private readonly categorias = [...MOCK_CATEGORIAS];

  listar(parametros: CategoriaConsultaParametros): Observable<RespostaPaginada<CategoriaResumo>> {
    if (environment.mockApi) {
      const nome = parametros.nome?.trim().toLocaleLowerCase();
      const filtradas = this.categorias.filter((categoria) => {
        const correspondeNome = !nome || categoria.nome.toLocaleLowerCase().includes(nome);
        const correspondeStatus = parametros.apenasDesativados
          ? !categoria.ativo
          : parametros.incluirInativos || categoria.ativo;
        return correspondeNome && correspondeStatus;
      });
      const inicio = parametros.page * parametros.size;
      return of({
        dados: filtradas.slice(inicio, inicio + parametros.size),
        paginaAtual: parametros.page,
        tamanhoPagina: parametros.size,
        totalElementos: filtradas.length,
        totalPaginas: Math.ceil(filtradas.length / parametros.size),
      }).pipe(delay(200));
    }

    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('incluirInativos', parametros.incluirInativos ?? false)
      .set('apenasDesativados', parametros.apenasDesativados ?? false);
    if (parametros.sort) params = params.set('sort', parametros.sort);
    if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    return this.http.get<RespostaPaginada<CategoriaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<CategoriaDetalhe> {
    if (environment.mockApi) {
      const categoria = this.categorias.find((item) => item.id === id);
      return categoria
        ? of(categoria).pipe(delay(200))
        : throwError(() => ({ status: 404 })).pipe(delay(200));
    }
    return this.http.get<CategoriaDetalhe>(`${this.endpoint}/${id}`);
  }

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

      const categoria: CategoriaResumo = {
        id: Math.max(0, ...this.categorias.map((item) => item.id)) + 1,
        nome,
        descricao,
        ativo: true,
      };
      this.categorias.push(categoria);
      return of(categoria).pipe(delay(300));
    }

    return this.http.post<CategoriaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: CategoriaRequisicao): Observable<CategoriaDetalhe> {
    if (environment.mockApi) {
      const categoria = this.categorias.find((item) => item.id === id);
      if (!categoria) return throwError(() => ({ status: 404 })).pipe(delay(300));
      categoria.nome = requisicao.nome.trim();
      categoria.descricao = requisicao.descricao?.trim() || undefined;
      return of(categoria).pipe(delay(300));
    }
    return this.http.put<CategoriaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    if (environment.mockApi) {
      const categoria = this.categorias.find((item) => item.id === id);
      if (categoria) categoria.ativo = false;
      return of(void 0).pipe(delay(200));
    }
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    if (environment.mockApi) {
      const categoria = this.categorias.find((item) => item.id === id);
      if (categoria) categoria.ativo = true;
      return of(void 0).pipe(delay(200));
    }
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

  excluir(id: number): Observable<void> {
    if (environment.mockApi) {
      const indice = this.categorias.findIndex((item) => item.id === id);
      if (indice < 0) return throwError(() => ({ status: 404 })).pipe(delay(200));
      this.categorias.splice(indice, 1);
      return of(void 0).pipe(delay(200));
    }
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }
}
