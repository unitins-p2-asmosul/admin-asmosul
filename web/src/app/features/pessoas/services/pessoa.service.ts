import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ItemDominio } from '@features/shared/models/item-dominio.model';
import { ErroCampo } from '@features/shared/models/erro-api.model';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_CATEGORIAS, MOCK_COMORBIDADES, MOCK_PESSOAS } from '../mocks/pessoa.mock';
import {
  ESCOLARIDADE_OPCOES,
  ItemRelacionadoResumo,
  PessoaDetalhe,
  PessoaRequisicao,
  RENDA_FAMILIAR_OPCOES,
  SEXO_OPCOES,
} from '../models/pessoa.model';

const LATENCIA_SIMULADA_MS = 300;

@Injectable({
  providedIn: 'root',
})
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';
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
   * Opções do select de comorbidades.
   *
   * Temporário: quando a feature de comorbidades existir, mover para
   * ComorbidadeService.listar() e apenas trocar a chamada na page.
   */
  listarComorbidades(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_COMORBIDADES]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  /**
   * Opções do select de categorias.
   *
   * Temporário: quando CategoriaService expuser listar(), trocar por ele.
   */
  listarCategorias(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_CATEGORIAS]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private cadastrarSimulado(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    const erros = this.validarRequisicao(requisicao);

    if (erros.length > 0) {
      return this.erroSimulado(400, 'Um ou mais campos não passaram na validação.', erros);
    }

    const cpfJaCadastrado = this.pessoas.some((pessoa) => pessoa.cpf === requisicao.cpf);

    if (cpfJaCadastrado) {
      return this.erroSimulado(409, 'Já existe uma pessoa cadastrada com este CPF');
    }

    const pessoa: PessoaDetalhe = {
      id: Math.max(0, ...this.pessoas.map((item) => item.id)) + 1,
      nome: requisicao.nome,
      cpf: requisicao.cpf,
      dataNascimento: requisicao.dataNascimento,
      sexo: this.descreverCodigo(requisicao.sexo, SEXO_OPCOES),
      telefone: requisicao.telefone,
      email: requisicao.email,
      escolaridade: this.descreverCodigo(requisicao.escolaridade, ESCOLARIDADE_OPCOES),
      profissao: requisicao.profissao,
      rendaFamiliar: this.descreverCodigo(requisicao.rendaFamiliar, RENDA_FAMILIAR_OPCOES),
      comorbidades: requisicao.comorbidades,
      categorias: requisicao.categorias,
      descricao: requisicao.descricao,
    };

    this.pessoas.push(pessoa);
    return of(pessoa).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  /** Espelha as validações obrigatórias descritas em docs/contratos/pessoas.md. */
  private validarRequisicao(requisicao: PessoaRequisicao): ErroCampo[] {
    const erros: ErroCampo[] = [];

    if (!requisicao.nome.trim()) {
      erros.push({ campo: 'nome', mensagem: 'O nome é obrigatório' });
    }

    if (!/^\d{11}$/.test(requisicao.cpf)) {
      erros.push({ campo: 'cpf', mensagem: 'O CPF deve conter 11 números' });
    }

    if (!/^\d{2}-\d{2}-\d{4}$/.test(requisicao.dataNascimento)) {
      erros.push({
        campo: 'dataNascimento',
        mensagem: 'A data de nascimento deve estar no formato dd-mm-aaaa',
      });
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
