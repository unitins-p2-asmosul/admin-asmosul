import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  ContaAtualizacao,
  ContaConsultaParametros,
  ContaDetalhe,
  ContaRedefinirSenhaAdmin,
  ContaRequisicao,
  ContaResumo,
  PerfilDisponivel,
} from '../models/conta.model';

@Injectable({ providedIn: 'root' })
export class ContaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'acessos/contas';

  listar(parametros: ContaConsultaParametros): Observable<RespostaPaginada<ContaResumo>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort);

    for (const campo of [
      'nomePessoa',
      'nomeUsuario',
      'email',
      'redefinirSenha',
      'dataCriacao',
      'dataInativo',
      'apenasInativos',
    ] as const) {
      const valor = parametros[campo];
      if (valor !== undefined && valor !== '') {
        params = params.set(campo, valor);
      }
    }

    for (const perfil of parametros.perfis ?? []) {
      params = params.append('perfis', perfil);
    }

    return this.http.get<RespostaPaginada<ContaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<ContaDetalhe> {
    return this.http.get<ContaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: ContaRequisicao): Observable<ContaDetalhe> {
    return this.http.post<ContaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: ContaAtualizacao): Observable<ContaDetalhe> {
    return this.http.put<ContaDetalhe>(`${this.endpoint}/${id}`, {
      nomeUsuario: requisicao.nomeUsuario,
    });
  }

  atualizarPerfis(id: number, perfis: ContaRequisicao['perfis']): Observable<ContaDetalhe> {
    return this.http.put<ContaDetalhe>(`${this.endpoint}/${id}/perfis`, { perfis });
  }

  redefinirSenhaAdmin(id: number, novaSenhaTemporaria: string): Observable<void> {
    const requisicao: ContaRedefinirSenhaAdmin = { novaSenhaTemporaria };
    return this.http.patch<void>(`${this.endpoint}/${id}/redefinir-senha`, requisicao);
  }

  alterarMinhaSenha(dados: { senhaAtual: string; novaSenha: string }): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/minha-senha`, dados);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

  listarPerfis(): Observable<PerfilDisponivel[]> {
    return this.http.get<PerfilDisponivel[]>(`${this.endpoint}/perfis`);
  }
}
