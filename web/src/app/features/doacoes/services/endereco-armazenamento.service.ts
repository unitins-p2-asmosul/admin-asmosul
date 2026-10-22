import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  EnderecoArmazenamentoAtualizacao,
  EnderecoArmazenamentoConsultaParametros,
  EnderecoArmazenamentoDetalhe,
  EnderecoArmazenamentoRequisicao,
  EnderecoArmazenamentoResumo,
} from '../models/endereco-armazenamento.model';

@Injectable({ providedIn: 'root' })
export class EnderecoArmazenamentoService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/enderecos';

  listar(
    parametros: EnderecoArmazenamentoConsultaParametros,
  ): Observable<RespostaPaginada<EnderecoArmazenamentoResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    for (const campo of ['nome', 'bairro', 'logradouro'] as const) {
      const valor = parametros[campo]?.trim();
      if (valor) params = params.set(campo, valor);
    }

    if (parametros.apenasInativos) params = params.set('apenasInativos', true);

    return this.http.get<RespostaPaginada<EnderecoArmazenamentoResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<EnderecoArmazenamentoDetalhe> {
    return this.http.get<EnderecoArmazenamentoDetalhe>(`${this.endpoint}/${id}`);
  }

  listarTodas(incluirInativos = false): Observable<EnderecoArmazenamentoResumo[]> {
    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<EnderecoArmazenamentoResumo[]>(`${this.endpoint}/todas`, { params });
  }

  cadastrar(requisicao: EnderecoArmazenamentoRequisicao): Observable<EnderecoArmazenamentoDetalhe> {
    return this.http.post<EnderecoArmazenamentoDetalhe>(this.endpoint, requisicao);
  }

  atualizar(
    id: number,
    requisicao: EnderecoArmazenamentoAtualizacao,
  ): Observable<EnderecoArmazenamentoDetalhe> {
    return this.http.put<EnderecoArmazenamentoDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}
