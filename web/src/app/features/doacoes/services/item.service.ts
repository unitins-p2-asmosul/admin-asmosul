import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  ItemAtualizacao,
  ItemConsultaParametros,
  ItemDetalhe,
  ItemRequisicao,
  ItemResumo,
} from '../models/item.model';

@Injectable({ providedIn: 'root' })
export class ItemService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/itens';

  listar(parametros: ItemConsultaParametros): Observable<RespostaPaginada<ItemResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    if (parametros.nome?.trim()) params = params.set('nome', parametros.nome.trim());
    if (parametros.categoriasIds?.length) {
      parametros.categoriasIds.forEach((id) => {
        params = params.append('categoriasIds', id.toString());
      });
    }
    if (parametros.estoque !== undefined && parametros.estoque !== null) {
      params = params.set('estoque', parametros.estoque);
    }
    if (parametros.precoUnitarioMinimo !== undefined && parametros.precoUnitarioMinimo !== null) {
      params = params.set('precoUnitarioMinimo', parametros.precoUnitarioMinimo);
    }
    if (parametros.precoUnitarioMaximo !== undefined && parametros.precoUnitarioMaximo !== null) {
      params = params.set('precoUnitarioMaximo', parametros.precoUnitarioMaximo);
    }
    if (parametros.apenasInativos) {
      params = params.set('apenasInativos', true);
    }

    return this.http.get<RespostaPaginada<ItemResumo>>(this.endpoint, { params });
  }

  listarTodos(incluirInativos = false): Observable<ItemResumo[]> {
    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<ItemResumo[]>(`${this.endpoint}/todos`, { params });
  }

  buscarPorId(id: number): Observable<ItemDetalhe> {
    return this.http.get<ItemDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: ItemRequisicao): Observable<ItemDetalhe> {
    return this.http.post<ItemDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: ItemAtualizacao): Observable<ItemDetalhe> {
    return this.http.put<ItemDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}
