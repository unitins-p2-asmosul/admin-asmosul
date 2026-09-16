import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ItemDominio } from '@features/shared/models/item-dominio.model';
import { ErroCampo } from '@features/shared/models/erro-api.model';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_CATEGORIAS, MOCK_COMORBIDADES, MOCK_PESSOAS } from '../mocks/pessoa.mock';
import {
  CepDados,
  ESCOLARIDADE_OPCOES,
  ItemRelacionadoResumo,
  PessoaDetalhe,
  PessoaRequisicao,
  RENDA_FAMILIAR_OPCOES,
  SEXO_OPCOES,
  TipoPessoa,
  UF_OPCOES,
  UfCodigo,
} from '../models/pessoa.model';

const LATENCIA_SIMULADA_MS = 300;

@Injectable({
  providedIn: 'root',
})
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';
  private readonly cepEndpoint = 'cep';
  private readonly pessoas = [...MOCK_PESSOAS];

  /**
   * POST /pessoas
   *
   * Enquanto environment.mockApi for true, o cadastro é simulado localmente.
   * A assinatura já é a definitiva: para integrar com o Back-end basta trocar
   * a flag em src/enviroments/enviroment.ts — nenhuma alteração na page.
   */
  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    if (environment.mockApi) {
      return this.cadastrarSimulado(requisicao);
    }

    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
  }

  /**
   * GET /cep/{cep}
   *
   * Consulta os dados de endereço a partir do CEP informado via integração com ViaCEP no backend.
   */
  consultarCep(cep: string): Observable<CepDados> {
    const cepLimpo = cep.replace(/\D/g, '');

    if (environment.mockApi) {
      if (cepLimpo.length !== 8) {
        return throwError(() => ({ status: 400, error: { detail: 'CEP inválido' } }));
      }

      if (cepLimpo === '00000000' || cepLimpo === '99999999') {
        return throwError(() => ({
          status: 404,
          error: { detail: 'CEP não encontrado' },
        })).pipe(delay(LATENCIA_SIMULADA_MS));
      }

      if (cepLimpo.startsWith('01')) {
        return of({
          cep: `${cepLimpo.slice(0, 5)}-${cepLimpo.slice(5)}`,
          logradouro: 'Praça da Sé',
          complemento: 'lado ímpar',
          bairro: 'Sé',
          cidade: 'São Paulo',
          uf: UfCodigo.SP,
        }).pipe(delay(LATENCIA_SIMULADA_MS));
      }

      return of({
        cep: `${cepLimpo.slice(0, 5)}-${cepLimpo.slice(5)}`,
        logradouro: 'Avenida Joaquim Teotônio Segurado',
        complemento: '',
        bairro: 'Plano Diretor Sul',
        cidade: 'Palmas',
        uf: UfCodigo.TO,
      }).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    return this.http.get<CepDados>(`${this.cepEndpoint}/${cepLimpo}`);
  }

  /**
   * Opções do select de comorbidades (fallback mockado).
   */
  listarComorbidades(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_COMORBIDADES]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  /**
   * Opções do select de categorias (fallback mockado).
   */
  listarCategorias(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_CATEGORIAS]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private cadastrarSimulado(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    const erros = this.validarRequisicao(requisicao);

    if (erros.length > 0) {
      return this.erroSimulado(400, 'Um ou mais campos não passaram na validação.', erros);
    }

    const documentoJaCadastrado = this.pessoas.some(
      (pessoa) => pessoa.cpfCnpj === requisicao.cpfCnpj,
    );

    if (documentoJaCadastrado) {
      const rotulo = requisicao.tipoPessoa === TipoPessoa.JURIDICA ? 'CNPJ' : 'CPF';
      return this.erroSimulado(409, `Já existe uma pessoa cadastrada com este ${rotulo}`);
    }

    if (requisicao.email) {
      const emailJaCadastrado = this.pessoas.some(
        (pessoa) => pessoa.email?.toLowerCase() === requisicao.email?.toLowerCase(),
      );
      if (emailJaCadastrado) {
        return this.erroSimulado(409, 'Já existe uma pessoa cadastrada com este e-mail');
      }
    }

    const ehJuridica = requisicao.tipoPessoa === TipoPessoa.JURIDICA;

    const pessoa: PessoaDetalhe = {
      id: Math.max(0, ...this.pessoas.map((item) => item.id)) + 1,
      nome: requisicao.nome,
      cpfCnpj: requisicao.cpfCnpj,
      tipoPessoa: requisicao.tipoPessoa ?? TipoPessoa.FISICA,
      dataNascimento: ehJuridica ? undefined : requisicao.dataNascimento,
      sexo: ehJuridica ? undefined : this.descreverCodigo(requisicao.sexo, SEXO_OPCOES),
      telefone: requisicao.telefone,
      email: requisicao.email,
      escolaridade: ehJuridica
        ? undefined
        : this.descreverCodigo(requisicao.escolaridade, ESCOLARIDADE_OPCOES),
      profissao: ehJuridica ? undefined : requisicao.profissao,
      rendaFamiliar: ehJuridica
        ? undefined
        : this.descreverCodigo(requisicao.rendaFamiliar, RENDA_FAMILIAR_OPCOES),
      comorbidades: ehJuridica ? undefined : requisicao.comorbidades,
      categorias: requisicao.categorias,
      descricao: requisicao.descricao,
      cep: requisicao.cep,
      uf: requisicao.uf ? this.descreverCodigo(requisicao.uf as UfCodigo, UF_OPCOES) : undefined,
      cidade: requisicao.cidade,
      bairro: requisicao.bairro,
      logradouro: requisicao.logradouro,
      complementoEndereco: requisicao.complementoEndereco,
      quantidadeCoabitantes: ehJuridica ? 0 : (requisicao.quantidadeCoabitantes ?? 0),
      ehBeneficiario: ehJuridica ? false : Boolean(requisicao.ehBeneficiario),
      ehDoador: Boolean(requisicao.ehDoador),
      ativo: true,
    };

    this.pessoas.push(pessoa);
    return of(pessoa).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  /** Espelha as validações obrigatórias de PF e PJ. */
  private validarRequisicao(requisicao: PessoaRequisicao): ErroCampo[] {
    const erros: ErroCampo[] = [];
    const ehJuridica = requisicao.tipoPessoa === TipoPessoa.JURIDICA;

    if (!requisicao.nome?.trim()) {
      erros.push({ campo: 'nome', mensagem: 'O nome é obrigatório' });
    }

    if (ehJuridica) {
      if (!/^\d{14}$/.test(requisicao.cpfCnpj)) {
        erros.push({ campo: 'cpfCnpj', mensagem: 'O CNPJ deve conter 14 números' });
      }

      if (requisicao.ehBeneficiario) {
        erros.push({
          campo: 'ehBeneficiario',
          mensagem: 'Pessoa Jurídica não pode ser cadastrada como beneficiária',
        });
      }
    } else {
      if (!/^\d{11}$/.test(requisicao.cpfCnpj)) {
        erros.push({ campo: 'cpfCnpj', mensagem: 'O CPF deve conter 11 números' });
      }

      if (!requisicao.dataNascimento) {
        erros.push({
          campo: 'dataNascimento',
          mensagem: 'A data de nascimento é obrigatória para Pessoa Física',
        });
      } else if (!/^\d{2}-\d{2}-\d{4}$/.test(requisicao.dataNascimento)) {
        erros.push({
          campo: 'dataNascimento',
          mensagem: 'A data de nascimento deve estar no formato dd-mm-aaaa',
        });
      }
    }

    if (!/^\d{10,11}$/.test(requisicao.telefone)) {
      erros.push({ campo: 'telefone', mensagem: 'O telefone deve conter 10 ou 11 números' });
    }

    if (requisicao.email && !requisicao.email.includes('@')) {
      erros.push({ campo: 'email', mensagem: 'O e-mail informado é inválido' });
    }

    return erros;
  }

  /** Converte o código do enum no objeto { codigo, descricao } devolvido pela API. */
  private descreverCodigo<T extends string>(
    codigo: T | undefined,
    opcoes: readonly ItemDominio<T>[],
  ): ItemDominio<T> | undefined {
    if (!codigo) {
      return undefined;
    }

    return opcoes.find((opcao) => opcao.codigo === codigo);
  }

  /** Reproduz o corpo de erro RFC 7807 que o Back-end devolverá. */
  private erroSimulado(status: number, detail: string, erros?: ErroCampo[]): Observable<never> {
    return throwError(() => ({
      status,
      error: {
        status,
        detail,
        instance: `/${this.endpoint}`,
        timestamp: new Date().toISOString(),
        erros,
      },
    })).pipe(delay(LATENCIA_SIMULADA_MS));
  }
}
