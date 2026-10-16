import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  CategoriaItemAtualizacao,
  CategoriaItemConsultaParametros,
  CategoriaItemDetalhe,
  CategoriaItemRequisicao,
  CategoriaItemResumo,
} from '../models/categoria-item.model';

@Injectable({ providedIn: 'root' })
export class CategoriaItemService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/categorias';

  listar(
    parametros: CategoriaItemConsultaParametros,
  ): Observable<RespostaPaginada<CategoriaItemResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    if (parametros.descricao) params = params.set('descricao', parametros.descricao.trim());
    if (parametros.apenasInativos) params = params.set('apenasInativos', true);

    return this.http.get<RespostaPaginada<CategoriaItemResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<CategoriaItemDetalhe> {
    return this.http.get<CategoriaItemDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: CategoriaItemRequisicao): Observable<CategoriaItemDetalhe> {
    return this.http.post<CategoriaItemDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: CategoriaItemAtualizacao): Observable<CategoriaItemDetalhe> {
    return this.http.put<CategoriaItemDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}
