import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  EntradaAtualizacao,
  EntradaConsultaParametros,
  EntradaDetalhe,
  EntradaRequisicao,
  EntradaResumo,
} from '../models/entrada.model';

@Injectable({ providedIn: 'root' })
export class EntradaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/entradas';

  listar(parametros: EntradaConsultaParametros): Observable<RespostaPaginada<EntradaResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'dataHora,desc');

    if (parametros.itemId) params = params.set('itemId', parametros.itemId);
    if (parametros.doadorId) params = params.set('doadorId', parametros.doadorId);
    if (parametros.enderecoId) params = params.set('enderecoId', parametros.enderecoId);
    if (parametros.quantidade !== undefined && parametros.quantidade !== null) {
      params = params.set('quantidade', parametros.quantidade);
    }
    if (parametros.data) params = params.set('data', parametros.data);

    return this.http.get<RespostaPaginada<EntradaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<EntradaDetalhe> {
    return this.http.get<EntradaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: EntradaRequisicao): Observable<EntradaDetalhe> {
    return this.http.post<EntradaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: EntradaAtualizacao): Observable<EntradaDetalhe> {
    return this.http.patch<EntradaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }

  excluirItem(idEntrada: number, idEntradaEstoque: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${idEntrada}/estoque/${idEntradaEstoque}`);
  }
}
