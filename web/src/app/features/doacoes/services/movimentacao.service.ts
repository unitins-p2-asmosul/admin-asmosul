import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  MovimentacaoAtualizacao,
  MovimentacaoConsultaParametros,
  MovimentacaoDetalhe,
  MovimentacaoRequisicao,
  MovimentacaoResumo,
} from '../models/movimentacao.model';

@Injectable({ providedIn: 'root' })
export class MovimentacaoService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/movimentacoes';

  listar(
    parametros: MovimentacaoConsultaParametros,
  ): Observable<RespostaPaginada<MovimentacaoResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'dataHora,desc');

    for (const campo of [
      'itemId',
      'localAntigoId',
      'localNovoId',
      'quantidade',
      'dataHoraInicio',
      'dataHoraFim',
    ] as const) {
      const valor = parametros[campo];
      if (valor !== undefined && valor !== null && valor !== '') {
        params = params.set(campo, valor);
      }
    }

    return this.http.get<RespostaPaginada<MovimentacaoResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<MovimentacaoDetalhe> {
    return this.http.get<MovimentacaoDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: MovimentacaoRequisicao): Observable<MovimentacaoDetalhe> {
    return this.http.post<MovimentacaoDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: MovimentacaoAtualizacao): Observable<MovimentacaoDetalhe> {
    return this.http.patch<MovimentacaoDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }
}
