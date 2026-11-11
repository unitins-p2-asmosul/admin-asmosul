import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  KitAtualizacao,
  KitConsultaParametros,
  KitDetalhe,
  KitDisponibilidade,
  KitRequisicao,
  KitResumo,
} from '../models/kit.model';

@Injectable({ providedIn: 'root' })
export class KitService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'doacoes/kits';

  listar(parametros: KitConsultaParametros): Observable<RespostaPaginada<KitResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    if (parametros.nome?.trim()) {
      params = params.set('nome', parametros.nome.trim());
    }
    if (parametros.descricao?.trim()) {
      params = params.set('descricao', parametros.descricao.trim());
    }
    if (parametros.itensIds?.length) {
      parametros.itensIds.forEach((id) => {
        params = params.append('itensIds', id.toString());
      });
    }
    if (
      parametros.quantidadeDistribuivel !== undefined &&
      parametros.quantidadeDistribuivel !== null
    ) {
      params = params.set('quantidadeDistribuivel', parametros.quantidadeDistribuivel);
    }
    if (parametros.apenasInativos) {
      params = params.set('apenasInativos', true);
    }

    return this.http.get<RespostaPaginada<KitResumo>>(this.endpoint, { params });
  }

  listarTodos(incluirInativos = false): Observable<KitResumo[]> {
    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<KitResumo[]>(`${this.endpoint}/todos`, { params });
  }

  buscarPorId(id: number): Observable<KitDetalhe> {
    return this.http.get<KitDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: KitRequisicao): Observable<KitDetalhe> {
    return this.http.post<KitDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: KitAtualizacao): Observable<KitDetalhe> {
    return this.http.put<KitDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

  consultarDisponibilidade(
    idKit: number,
    localArmazenamentoId: number,
  ): Observable<KitDisponibilidade> {
    const params = new HttpParams().set('localArmazenamentoId', localArmazenamentoId);
    return this.http.get<KitDisponibilidade>(`${this.endpoint}/${idKit}/disponibilidade`, {
      params,
    });
  }
}
