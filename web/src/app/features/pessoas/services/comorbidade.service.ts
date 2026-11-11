import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable} from 'rxjs';
import {
  ComorbidadeAtualizacao,
  ComorbidadeConsultaParametros,
  ComorbidadeDetalhe,
  ComorbidadeRequisicao,
  ComorbidadeResumo,
} from '../models/comorbidade.model';


@Injectable({
  providedIn: 'root',
})
export class ComorbidadeService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas/comorbidades';

  /** GET /comorbidades — listagem paginada, ordenada e filtrada. */
  listar(
    parametros?: ComorbidadeConsultaParametros,
  ): Observable<RespostaPaginada<ComorbidadeResumo>> {

    let params = new HttpParams();

    if (parametros) {
      if (parametros.page !== undefined) params = params.set('page', parametros.page);
      if (parametros.size !== undefined) params = params.set('size', parametros.size);
      if (parametros.sort) params = params.set('sort', parametros.sort);
      if (parametros.incluirInativos !== undefined) {
        params = params.set('incluirInativos', parametros.incluirInativos);
      }
      if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    }

    return this.http.get<RespostaPaginada<ComorbidadeResumo>>(this.endpoint, {params});
  }

  /** GET /comorbidades/todas — lista completa, sem paginação, para selects. */
  listarTodas(incluirInativos = false): Observable<ComorbidadeResumo[]> {

    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<ComorbidadeResumo[]>(`${this.endpoint}/todas`, {params});
  }

  /** GET /comorbidades/{id} */
  buscarPorId(id: number): Observable<ComorbidadeDetalhe> {

    return this.http.get<ComorbidadeDetalhe>(`${this.endpoint}/${id}`);
  }

  /** POST /comorbidades */
  cadastrar(requisicao: ComorbidadeRequisicao): Observable<ComorbidadeDetalhe> {

    return this.http.post<ComorbidadeDetalhe>(this.endpoint, requisicao);
  }

  /** PUT /comorbidades/{id} */
  atualizar(id: number, requisicao: ComorbidadeAtualizacao): Observable<ComorbidadeDetalhe> {

    return this.http.put<ComorbidadeDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  /** PATCH /comorbidades/{id}/desativar */
  desativar(id: number): Observable<void> {

    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  /** PATCH /comorbidades/{id}/reativar */
  reativar(id: number): Observable<void> {

    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}
