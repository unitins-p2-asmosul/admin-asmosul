import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
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
  private readonly endpoint = 'pessoas/categorias';

  listar(parametros: CategoriaConsultaParametros): Observable<RespostaPaginada<CategoriaResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('apenasInativos', parametros.apenasInativos ?? false);
    if (parametros.sort) params = params.set('sort', parametros.sort);
    if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    return this.http.get<RespostaPaginada<CategoriaResumo>>(this.endpoint, { params });
  }

  /** GET /categorias/todas — lista completa, sem paginação, para selects. */
  listarTodas(incluirInativos = false): Observable<CategoriaResumo[]> {
    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<CategoriaResumo[]>(`${this.endpoint}/todas`, { params });
  }

  buscarPorId(id: number): Observable<CategoriaDetalhe> {
    return this.http.get<CategoriaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: CategoriaRequisicao): Observable<CategoriaDetalhe> {
    return this.http.post<CategoriaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: CategoriaRequisicao): Observable<CategoriaDetalhe> {
    return this.http.put<CategoriaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

}
