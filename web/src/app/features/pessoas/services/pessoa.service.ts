import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ItemDominio } from '@features/shared/models/item-dominio.model';
import { ErroCampo } from '@features/shared/models/erro-api.model';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_CATEGORIAS, MOCK_COMORBIDADES, MOCK_PESSOAS } from '../mocks/pessoa.mock';
import {
  ESCOLARIDADE_OPCOES,
  ItemRelacionadoResumo,
  PessoaConsultaParametros,
  PessoaDetalhe,
  PessoaRequisicao,
  PessoaResumo,
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

  listar(parametros: PessoaConsultaParametros): Observable<RespostaPaginada<PessoaResumo>> {
    if (environment.mockApi) {
      const nome = parametros.nome?.trim().toLocaleLowerCase();
      const cpfCnpj = parametros.cpfCnpj?.trim();
      const telefone = parametros.telefone?.trim();
      const email = parametros.email?.trim().toLocaleLowerCase();
      const profissao = parametros.profissao?.trim().toLocaleLowerCase();
      const bairro = parametros.bairro?.trim().toLocaleLowerCase();

      const filtradas = this.pessoas.filter((pessoa) => {
        const correspondeNome = !nome || pessoa.nome.toLocaleLowerCase().includes(nome);
        const correspondeCpf = !cpfCnpj || (pessoa.cpfCnpj ?? pessoa.cpf).includes(cpfCnpj);
        const correspondeTipo = !parametros.tipoPessoa || pessoa.tipoPessoa === parametros.tipoPessoa;
        const correspondeData = !parametros.dataNascimento || pessoa.dataNascimento === parametros.dataNascimento;
        const correspondeTelefone = !telefone || pessoa.telefone.includes(telefone);
        const correspondeEmail = !email || pessoa.email?.toLocaleLowerCase().includes(email);
        const correspondeProfissao = !profissao || pessoa.profissao?.toLocaleLowerCase().includes(profissao);
        const correspondeBairro = !bairro || pessoa.bairro?.toLocaleLowerCase().includes(bairro);
        const correspondeSexo = !parametros.sexo || pessoa.sexo?.codigo === parametros.sexo;
        const correspondeEscolaridade = !parametros.escolaridade || pessoa.escolaridade?.codigo === parametros.escolaridade;
        const correspondeRenda = !parametros.rendaFamiliar || pessoa.rendaFamiliar?.codigo === parametros.rendaFamiliar;
        const correspondeComorbidade = !parametros.comorbidadeId || pessoa.comorbidades?.includes(parametros.comorbidadeId);
        const correspondeCategoria = !parametros.categoriaId || pessoa.categorias?.includes(parametros.categoriaId);
        const correspondeCoabitantes = parametros.quantidadeCoabitantes === undefined
          || pessoa.quantidadeCoabitantes === parametros.quantidadeCoabitantes;
        const correspondeBeneficiario = parametros.ehBeneficiario === undefined
          || pessoa.ehBeneficiario === parametros.ehBeneficiario;
        const correspondeDoador = parametros.ehDoador === undefined || pessoa.ehDoador === parametros.ehDoador;
        const correspondeStatus = parametros.apenasInativos
          ? !pessoa.ativo
          : parametros.incluirInativos || pessoa.ativo;

        return correspondeNome && correspondeCpf && correspondeTipo && correspondeData
          && correspondeTelefone && correspondeEmail && correspondeProfissao && correspondeBairro
          && correspondeSexo && correspondeEscolaridade && correspondeRenda && correspondeComorbidade
          && correspondeCategoria && correspondeCoabitantes && correspondeBeneficiario && correspondeDoador
          && correspondeStatus;
      });

      const dados: PessoaResumo[] = filtradas.map((pessoa) => ({
        id: pessoa.id,
        nome: pessoa.nome,
        cpf: pessoa.cpf,
        cpfCnpj: pessoa.cpfCnpj ?? pessoa.cpf,
        tipoPessoa: pessoa.tipoPessoa ?? (pessoa.cpf.length === 14 ? 'JURIDICA' : 'FISICA'),
        dataNascimento: pessoa.dataNascimento,
        telefone: pessoa.telefone,
        email: pessoa.email,
        sexo: pessoa.sexo,
        escolaridade: pessoa.escolaridade,
        profissao: pessoa.profissao,
        bairro: pessoa.bairro,
        rendaFamiliar: pessoa.rendaFamiliar,
        comorbidades: pessoa.comorbidades,
        categorias: pessoa.categorias,
        quantidadeCoabitantes: pessoa.quantidadeCoabitantes,
        ehBeneficiario: pessoa.ehBeneficiario,
        ehDoador: pessoa.ehDoador,
        ativo: !!pessoa.ativo,
      }));

      const inicio = parametros.page * parametros.size;
      return of({
        dados: dados.slice(inicio, inicio + parametros.size),
        paginaAtual: parametros.page,
        tamanhoPagina: parametros.size,
        totalElementos: dados.length,
        totalPaginas: Math.max(1, Math.ceil(dados.length / parametros.size || 1)),
      }).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    let params = new HttpParams().set('page', parametros.page).set('size', parametros.size);
    if (parametros.sort) params = params.set('sort', parametros.sort);
    if (parametros.incluirInativos !== undefined) params = params.set('incluirInativos', parametros.incluirInativos);
    if (parametros.apenasInativos !== undefined) params = params.set('apenasInativos', parametros.apenasInativos);
    if (parametros.nome) params = params.set('nome', parametros.nome.trim());
    if (parametros.cpfCnpj) params = params.set('cpfCnpj', parametros.cpfCnpj.trim());
    if (parametros.tipoPessoa) params = params.set('tipoPessoa', parametros.tipoPessoa);
    if (parametros.dataNascimento) params = params.set('dataNascimento', parametros.dataNascimento);
    if (parametros.sexo) params = params.set('sexo', parametros.sexo);
    if (parametros.telefone) params = params.set('telefone', parametros.telefone.trim());
    if (parametros.email) params = params.set('email', parametros.email.trim());
    if (parametros.escolaridade) params = params.set('escolaridade', parametros.escolaridade);
    if (parametros.profissao) params = params.set('profissao', parametros.profissao.trim());
    if (parametros.bairro) params = params.set('bairro', parametros.bairro.trim());
    if (parametros.rendaFamiliar) params = params.set('rendaFamiliar', parametros.rendaFamiliar);
    if (parametros.comorbidadeId !== undefined) params = params.set('comorbidadeId', parametros.comorbidadeId);
    if (parametros.categoriaId !== undefined) params = params.set('categoriaId', parametros.categoriaId);
    if (parametros.quantidadeCoabitantes !== undefined) params = params.set('quantidadeCoabitantes', parametros.quantidadeCoabitantes);
    if (parametros.ehBeneficiario !== undefined) params = params.set('ehBeneficiario', parametros.ehBeneficiario);
    if (parametros.ehDoador !== undefined) params = params.set('ehDoador', parametros.ehDoador);

    return this.http.get<RespostaPaginada<PessoaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<PessoaDetalhe> {
    if (environment.mockApi) {
      const registro = this.pessoas.find((item) => item.id === id);
      return registro ? of(registro).pipe(delay(LATENCIA_SIMULADA_MS)) : throwError(() => ({ status: 404 })).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    return this.http.get<PessoaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    if (environment.mockApi) {
      return this.cadastrarSimulado(requisicao);
    }

    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
  }

  atualizar(id: number, requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    if (environment.mockApi) {
      const pessoa = this.pessoas.find((item) => item.id === id);
      if (!pessoa) {
        return throwError(() => ({ status: 404, error: { detail: 'Pessoa não encontrada.' } })).pipe(delay(LATENCIA_SIMULADA_MS));
      }

      const atualizada: PessoaDetalhe = {
        ...pessoa,
        nome: requisicao.nome,
        cpf: requisicao.cpfCnpj,
        cpfCnpj: requisicao.cpfCnpj,
        tipoPessoa: requisicao.tipoPessoa,
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
        cep: requisicao.cep,
        uf: requisicao.uf,
        cidade: requisicao.cidade,
        bairro: requisicao.bairro,
        logradouro: requisicao.logradouro,
        complementoEndereco: requisicao.complementoEndereco,
        quantidadeCoabitantes: requisicao.quantidadeCoabitantes,
        ehBeneficiario: requisicao.ehBeneficiario,
        ehDoador: requisicao.ehDoador,
      };

      Object.assign(pessoa, atualizada);
      return of(atualizada).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    return this.http.put<PessoaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    if (environment.mockApi) {
      const pessoa = this.pessoas.find((item) => item.id === id);
      if (pessoa) pessoa.ativo = false;
      return of(void 0).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    if (environment.mockApi) {
      const pessoa = this.pessoas.find((item) => item.id === id);
      if (pessoa) pessoa.ativo = true;
      return of(void 0).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

  listarComorbidades(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_COMORBIDADES]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  listarCategorias(): Observable<ItemRelacionadoResumo[]> {
    return of([...MOCK_CATEGORIAS]).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private cadastrarSimulado(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    const erros = this.validarRequisicao(requisicao);

    if (erros.length > 0) {
      return this.erroSimulado(400, 'Um ou mais campos não passaram na validação.', erros);
    }

    const cpfJaCadastrado = this.pessoas.some((pessoa) => (pessoa.cpfCnpj ?? pessoa.cpf) === requisicao.cpfCnpj);

    if (cpfJaCadastrado) {
      return this.erroSimulado(409, 'Já existe uma pessoa cadastrada com este CPF');
    }

    const pessoa: PessoaDetalhe = {
      id: Math.max(0, ...this.pessoas.map((item) => item.id)) + 1,
      nome: requisicao.nome,
      cpf: requisicao.cpfCnpj,
      cpfCnpj: requisicao.cpfCnpj,
      tipoPessoa: requisicao.tipoPessoa,
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
      cep: requisicao.cep,
      uf: requisicao.uf,
      cidade: requisicao.cidade,
      bairro: requisicao.bairro,
      logradouro: requisicao.logradouro,
      complementoEndereco: requisicao.complementoEndereco,
      quantidadeCoabitantes: requisicao.quantidadeCoabitantes,
      ehBeneficiario: requisicao.ehBeneficiario,
      ehDoador: requisicao.ehDoador,
      ativo: true,
    };

    this.pessoas.push(pessoa);
    return of(pessoa).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private validarRequisicao(requisicao: PessoaRequisicao): ErroCampo[] {
    const erros: ErroCampo[] = [];

    if (!requisicao.nome.trim()) {
      erros.push({ campo: 'nome', mensagem: 'O nome é obrigatório' });
    }

    if (!/^\d{11}$|^\d{14}$/.test(requisicao.cpfCnpj)) {
      erros.push({ campo: 'cpfCnpj', mensagem: 'O CPF/CNPJ deve conter 11 ou 14 números' });
    }

    if (requisicao.dataNascimento && !/^\d{2}-\d{2}-\d{4}$/.test(requisicao.dataNascimento)) {
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

  private descreverCodigo<T extends string>(
    codigo: T | undefined,
    opcoes: readonly ItemDominio<T>[],
  ): ItemDominio<T> | undefined {
    if (!codigo) {
      return undefined;
    }

    return opcoes.find((opcao) => opcao.codigo === codigo);
  }

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
