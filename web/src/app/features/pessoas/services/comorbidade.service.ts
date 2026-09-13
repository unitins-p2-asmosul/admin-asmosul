import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ErroCampo } from '@features/shared/models/erro-api.model';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../../enviroments/enviroment';
import { MOCK_COMORBIDADES } from '../mocks/comorbidade.mock';
import {
  ComorbidadeAtualizacao,
  ComorbidadeConsultaParametros,
  ComorbidadeDetalhe,
  ComorbidadeRequisicao,
  ComorbidadeResumo,
} from '../models/comorbidade.model';

const LATENCIA_SIMULADA_MS = 300;
const CHAVE_STORAGE = 'MOCK_COMORBIDADES_V2';
const TAMANHO_MAXIMO_NOME = 50;

@Injectable({
  providedIn: 'root',
})
export class ComorbidadeService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'comorbidades';

  /** GET /comorbidades — listagem paginada, ordenada e filtrada. */
  listar(
    parametros?: ComorbidadeConsultaParametros,
  ): Observable<RespostaPaginada<ComorbidadeResumo>> {
    if (environment.mockApi) {
      return this.listarSimulado(parametros);
    }

    let params = new HttpParams();

    if (parametros) {
      if (parametros.page !== undefined) params = params.set('page', parametros.page);
      if (parametros.size !== undefined) params = params.set('size', parametros.size);
      if (parametros.sort) params = params.set('sort', parametros.sort);
      if (parametros.incluirInativos !== undefined) {
        params = params.set('incluirInativos', parametros.incluirInativos);
      }
      if (parametros.nome) params = params.set('nome', parametros.nome.trim());
      if (parametros.apenasInativos) params = params.set('apenasInativos', true);
    }

    return this.http.get<RespostaPaginada<ComorbidadeResumo>>(this.endpoint, { params });
  }

  /** GET /comorbidades/todas — lista completa, sem paginação, para selects. */
  listarTodas(incluirInativos = false): Observable<ComorbidadeResumo[]> {
    if (environment.mockApi) {
      const lista = this.lerMock().filter((item) => incluirInativos || item.ativo);
      return of(this.ordenarPorNome(lista)).pipe(delay(LATENCIA_SIMULADA_MS));
    }

    const params = new HttpParams().set('incluirInativos', incluirInativos);
    return this.http.get<ComorbidadeResumo[]>(`${this.endpoint}/todas`, { params });
  }

  /** GET /comorbidades/{id} */
  buscarPorId(id: number): Observable<ComorbidadeDetalhe> {
    if (environment.mockApi) {
      const item = this.lerMock().find((registro) => registro.id === id);
      return item
        ? of({ ...item }).pipe(delay(LATENCIA_SIMULADA_MS))
        : this.erroSimulado(404, 'Comorbidade não encontrada.');
    }

    return this.http.get<ComorbidadeDetalhe>(`${this.endpoint}/${id}`);
  }

  /** POST /comorbidades */
  cadastrar(requisicao: ComorbidadeRequisicao): Observable<ComorbidadeDetalhe> {
    if (environment.mockApi) {
      return this.cadastrarSimulado(requisicao);
    }

    return this.http.post<ComorbidadeDetalhe>(this.endpoint, requisicao);
  }

  /** PUT /comorbidades/{id} */
  atualizar(id: number, requisicao: ComorbidadeAtualizacao): Observable<ComorbidadeDetalhe> {
    if (environment.mockApi) {
      return this.atualizarSimulado(id, requisicao);
    }

    return this.http.put<ComorbidadeDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  /** PATCH /comorbidades/{id}/desativar */
  desativar(id: number): Observable<void> {
    if (environment.mockApi) {
      return this.alterarStatusSimulado(id, false);
    }

    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  /** PATCH /comorbidades/{id}/reativar */
  reativar(id: number): Observable<void> {
    if (environment.mockApi) {
      return this.alterarStatusSimulado(id, true);
    }

    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }

  // ---------------------------------------------------------------------------
  // Simulação (environment.mockApi = true)
  // ---------------------------------------------------------------------------

  private listarSimulado(
    parametros?: ComorbidadeConsultaParametros,
  ): Observable<RespostaPaginada<ComorbidadeResumo>> {
    const page = parametros?.page ?? 0;
    const size = parametros?.size ?? 10;
    const [campo = 'nome', direcao = 'asc'] = (parametros?.sort ?? 'nome,asc').split(',');

    let lista = this.lerMock();

    if (parametros?.apenasInativos) {
      lista = lista.filter((item) => !item.ativo);
    } else if (!parametros?.incluirInativos) {
      lista = lista.filter((item) => item.ativo);
    }

    if (parametros?.nome?.trim()) {
      const termo = normalizar(parametros.nome);
      lista = lista.filter((item) => normalizar(item.nome).includes(termo));
    }

    lista = this.ordenar(lista, campo, direcao);

    const inicio = page * size;
    const resposta: RespostaPaginada<ComorbidadeResumo> = {
      dados: lista.slice(inicio, inicio + size),
      paginaAtual: page,
      tamanhoPagina: size,
      totalElementos: lista.length,
      totalPaginas: Math.ceil(lista.length / size),
    };

    return of(resposta).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private cadastrarSimulado(requisicao: ComorbidadeRequisicao): Observable<ComorbidadeDetalhe> {
    const lista = this.lerMock();
    const erros = this.validarNome(requisicao.nome);

    if (erros.length > 0) {
      return this.erroSimulado(400, 'Um ou mais campos não passaram na validação.', erros);
    }

    if (this.nomeJaExiste(requisicao.nome, lista)) {
      return this.erroSimulado(409, 'Já existe uma comorbidade cadastrada com este nome.');
    }

    const novo: ComorbidadeResumo = {
      id: Math.max(0, ...lista.map((item) => item.id)) + 1,
      nome: requisicao.nome.trim(),
      descricao: requisicao.descricao?.trim() || undefined,
      ativo: true,
    };

    this.gravarMock([...lista, novo]);
    return of({ ...novo }).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private atualizarSimulado(
    id: number,
    requisicao: ComorbidadeAtualizacao,
  ): Observable<ComorbidadeDetalhe> {
    const lista = this.lerMock();
    const indice = lista.findIndex((item) => item.id === id);

    if (indice === -1) {
      return this.erroSimulado(404, 'Comorbidade não encontrada.');
    }

    const erros = this.validarNome(requisicao.nome);

    if (erros.length > 0) {
      return this.erroSimulado(400, 'Um ou mais campos não passaram na validação.', erros);
    }

    if (this.nomeJaExiste(requisicao.nome, lista, id)) {
      return this.erroSimulado(409, 'Já existe uma comorbidade cadastrada com este nome.');
    }

    const atualizado: ComorbidadeResumo = {
      ...lista[indice],
      nome: requisicao.nome.trim(),
      descricao: requisicao.descricao?.trim() || undefined,
    };

    lista[indice] = atualizado;
    this.gravarMock(lista);
    return of({ ...atualizado }).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private alterarStatusSimulado(id: number, ativo: boolean): Observable<void> {
    const lista = this.lerMock();
    const indice = lista.findIndex((item) => item.id === id);

    if (indice === -1) {
      return this.erroSimulado(404, 'Comorbidade não encontrada.');
    }

    lista[indice] = { ...lista[indice], ativo };
    this.gravarMock(lista);
    return of(void 0).pipe(delay(LATENCIA_SIMULADA_MS));
  }

  private validarNome(nome: string): ErroCampo[] {
    const erros: ErroCampo[] = [];
    const valor = nome?.trim() ?? '';

    if (!valor) {
      erros.push({ campo: 'nome', mensagem: 'O nome é obrigatório' });
    } else if (valor.length > TAMANHO_MAXIMO_NOME) {
      erros.push({
        campo: 'nome',
        mensagem: `O nome deve ter no máximo ${TAMANHO_MAXIMO_NOME} caracteres`,
      });
    }

    return erros;
  }

  /** Unicidade por nome, ignorando acentos e maiúsculas, mesmo entre inativos (RN03). */
  private nomeJaExiste(nome: string, lista: ComorbidadeResumo[], ignorarId?: number): boolean {
    const alvo = normalizar(nome);
    return lista.some((item) => item.id !== ignorarId && normalizar(item.nome) === alvo);
  }

  private ordenar(lista: ComorbidadeResumo[], campo: string, direcao: string): ComorbidadeResumo[] {
    const fator = direcao.toLowerCase() === 'desc' ? -1 : 1;

    return [...lista].sort((a, b) => {
      if (campo === 'id') return (a.id - b.id) * fator;

      const valorA = campo === 'descricao' ? (a.descricao ?? '') : a.nome;
      const valorB = campo === 'descricao' ? (b.descricao ?? '') : b.nome;
      return valorA.localeCompare(valorB, 'pt-BR', { sensitivity: 'base' }) * fator;
    });
  }

  private ordenarPorNome(lista: ComorbidadeResumo[]): ComorbidadeResumo[] {
    return this.ordenar(lista, 'nome', 'asc');
  }

  private lerMock(): ComorbidadeResumo[] {
    try {
      const salvo = localStorage.getItem(CHAVE_STORAGE);
      if (salvo) {
        const lista = JSON.parse(salvo) as ComorbidadeResumo[];
        if (Array.isArray(lista) && lista.every((item) => typeof item.ativo === 'boolean')) {
          return lista;
        }
      }
    } catch {
      // Sem acesso ao localStorage: usa a base em memória.
    }

    return MOCK_COMORBIDADES.map((item) => ({ ...item }));
  }

  private gravarMock(lista: ComorbidadeResumo[]): void {
    try {
      localStorage.setItem(CHAVE_STORAGE, JSON.stringify(lista));
    } catch {
      // Sem acesso ao localStorage: a alteração vale só até recarregar.
    }
  }

  /** Reproduz o corpo de erro RFC 7807 devolvido pelo backend. */
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

/** Remove acentos e caixa para comparações e buscas. */
function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}
