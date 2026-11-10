import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {RespostaPaginada} from '@features/shared/models/resposta-paginada.model';
import {Observable} from 'rxjs';
import {
  CepDados,
  PessoaConsultaParametros,
  PessoaDetalhe,
  PessoaRequisicao,
  PessoaResumo,
} from '../models/pessoa.model';

@Injectable({
  providedIn: 'root',
})
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';
  private readonly cepEndpoint = 'cep';

  listar(parametros: PessoaConsultaParametros): Observable<RespostaPaginada<PessoaResumo>> {

    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size);

    if (parametros.sort) params = params.set('sort', parametros.sort);
    if (parametros.incluirInativos !== undefined) {
      params = params.set('incluirInativos', parametros.incluirInativos);
    }
    if (parametros.apenasInativos !== undefined) {
      params = params.set('apenasInativos', parametros.apenasInativos);
    }
    if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    if (parametros.cpfCnpj) params = params.set('cpfCnpj', parametros.cpfCnpj.trim());
    if (parametros.tipoPessoa) params = params.set('tipoPessoa', parametros.tipoPessoa);
    if (parametros.dataNascimento) {
      params = params.set('dataNascimento', parametros.dataNascimento);
    }
    if (parametros.dataNascimentoInicio) {
      params = params.set(
        'dataNascimentoInicio',
        converterDataParaApi(parametros.dataNascimentoInicio),
      );
    }
    if (parametros.dataNascimentoFim) {
      params = params.set(
        'dataNascimentoFim',
        converterDataParaApi(parametros.dataNascimentoFim),
      );
    }
    if (parametros.sexo) params = params.set('sexo', parametros.sexo);
    if (parametros.telefone) params = params.set('telefone', parametros.telefone.trim());
    if (parametros.email) params = params.set('email', parametros.email.trim());
    if (parametros.escolaridade) params = params.set('escolaridade', parametros.escolaridade);
    if (parametros.profissao) params = params.set('profissao', parametros.profissao.trim());
    if (parametros.bairro) params = params.set('bairro', parametros.bairro.trim());
    if (parametros.rendaFamiliar) {
      params = params.set('rendaFamiliar', parametros.rendaFamiliar);
    }
    if (parametros.comorbidadeId !== undefined) {
      params = params.set('comorbidadeId', parametros.comorbidadeId);
    }
    if (parametros.categoriaId !== undefined) {
      params = params.set('categoriaId', parametros.categoriaId);
    }
    if (parametros.quantidadeCoabitantes !== undefined) {
      params = params.set('quantidadeCoabitantes', parametros.quantidadeCoabitantes);
    }
    if (parametros.ehBeneficiario !== undefined) {
      params = params.set('ehBeneficiario', parametros.ehBeneficiario);
    }
    if (parametros.ehDoador !== undefined) {
      params = params.set('ehDoador', parametros.ehDoador);
    }

    return this.http.get<RespostaPaginada<PessoaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<PessoaDetalhe> {

    return this.http.get<PessoaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
  }

  /**
   * GET /cep/{cep}
   *
   * Consulta os dados de endereço a partir do CEP informado via integração com ViaCEP no backend.
   */
  consultarCep(cep: string): Observable<CepDados> {
    const cepLimpo = cep.replace(/\D/g, '');

    return this.http.get<CepDados>(`${this.cepEndpoint}/${cepLimpo}`);
  }

  /**
   * Opções do select de comorbidades (fallback mockado).
   */
  atualizar(id: number, requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {

    return this.http.put<PessoaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {

    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {

    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }


}

function converterDataParaApi(valor: string): string {
  // O input type="date" fornece yyyy-MM-dd.
  const formatoIso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor);

  if (formatoIso) {
    return `${formatoIso[3]}-${formatoIso[2]}-${formatoIso[1]}`;
  }

  // Também aceita dd/MM/yyyy.
  return valor.replace(/\//g, '-');
}
