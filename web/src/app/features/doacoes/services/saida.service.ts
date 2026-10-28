import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  SaidaAtualizacao,
  SaidaConsultaParametros,
  SaidaDetalhe,
  SaidaRequisicao,
  SaidaResumo,
} from '../models/saida.model';

@Injectable({ providedIn: 'root' })
export class SaidaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/saidas';

  listar(parametros: SaidaConsultaParametros): Observable<RespostaPaginada<SaidaResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'dataHora,desc');

    if (parametros.itensIds?.length) {
      parametros.itensIds.forEach((id) => {
        params = params.append('itensIds', id.toString());
      });
    }
    if (parametros.recebedorId) params = params.set('recebedorId', parametros.recebedorId);
    if (parametros.quantidade !== undefined && parametros.quantidade !== null) {
      params = params.set('quantidade', parametros.quantidade);
    }
    if (parametros.data) params = params.set('data', parametros.data);

    return this.http.get<RespostaPaginada<SaidaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<SaidaDetalhe> {
    return this.http.get<SaidaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: SaidaRequisicao): Observable<SaidaDetalhe> {
    return this.http.post<SaidaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: SaidaAtualizacao): Observable<SaidaDetalhe> {
    return this.http.patch<SaidaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }

  excluirItem(idSaida: number, idSaidaEstoque: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${idSaida}/estoque/${idSaidaEstoque}`);
  }
}
