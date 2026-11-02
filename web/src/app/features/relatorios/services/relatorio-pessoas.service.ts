import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  CriterioAgrupamentoPessoa,
  PessoaComorbidadeConsultaParametros,
  PessoaComorbidadeItem,
  ResumoCategoriasPessoa,
  ResumoComorbidades,
  ResumoPessoasGeral,
} from '../models/relatorio-pessoas.model';

@Injectable({ providedIn: 'root' })
export class RelatorioPessoasService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'relatorios/pessoas';

  obterResumoGeral(criterio: CriterioAgrupamentoPessoa = 'SEXO'): Observable<ResumoPessoasGeral> {
    const params = new HttpParams().set('criterio', criterio);
    return this.http.get<ResumoPessoasGeral>(`${this.endpoint}/geral`, { params });
  }

  obterResumoComorbidades(): Observable<ResumoComorbidades> {
    return this.http.get<ResumoComorbidades>(`${this.endpoint}/comorbidades/resumo`);
  }

  listarPessoasComorbidades(
    parametros: PessoaComorbidadeConsultaParametros,
  ): Observable<RespostaPaginada<PessoaComorbidadeItem>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    if (parametros.termoBusca?.trim()) {
      params = params.set('termoBusca', parametros.termoBusca.trim());
    }

    if (parametros.comorbidadeIds?.length) {
      parametros.comorbidadeIds.forEach((id) => {
        params = params.append('comorbidadeIds', id.toString());
      });
    }

    return this.http.get<RespostaPaginada<PessoaComorbidadeItem>>(
      `${this.endpoint}/comorbidades/tabela`,
      { params },
    );
  }

  obterResumoCategorias(): Observable<ResumoCategoriasPessoa> {
    return this.http.get<ResumoCategoriasPessoa>(`${this.endpoint}/categorias`);
  }
}
