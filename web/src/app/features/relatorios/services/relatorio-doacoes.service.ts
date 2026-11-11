import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RespostaPaginada } from '@features/shared/models/resposta-paginada.model';
import { Observable } from 'rxjs';
import {
  CategoriaItemRelatorioConsultaParametros,
  EntradasConsultaParametros,
  EntradasFiltros,
  FinanceiroConsultaParametros,
  FinanceiroFiltros,
  GiroEstoqueConsultaParametros,
  GiroEstoqueItem,
  InventarioEstoqueConsultaParametros,
  ItemEntradaDiscriminada,
  ItemFinanceiroTabela,
  ItemInventarioEstoque,
  ItemSaidaDiscriminada,
  ItemTabelaCategoriaRelatorio,
  ResumoArmazenamento,
  ResumoBalancoFinanceiro,
  ResumoCategoriasItens,
  ResumoEntradas,
  ResumoEstoqueGeral,
  ResumoItensGiro,
  ResumoSaidas,
  SaidasConsultaParametros,
  SaidasFiltros,
} from '../models/relatorio-doacoes.model';

@Injectable({ providedIn: 'root' })
export class RelatorioDoacoesService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'relatorios/doacoes';

  // --- Itens e Giro de Estoque ---
  obterResumoItens(): Observable<ResumoItensGiro> {
    return this.http.get<ResumoItensGiro>(`${this.endpoint}/itens/resumo`);
  }

  buscarGiroEstoque(
    parametros: GiroEstoqueConsultaParametros,
  ): Observable<RespostaPaginada<GiroEstoqueItem>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'itemNome,asc');

    if (parametros.nomeItem?.trim()) params = params.set('nomeItem', parametros.nomeItem.trim());
    if (parametros.categoriaId) params = params.set('categoriaId', parametros.categoriaId);
    if (parametros.unidadeMedida) params = params.set('unidadeMedida', parametros.unidadeMedida);
    if (parametros.saldoEstoqueMin !== undefined && parametros.saldoEstoqueMin !== null) {
      params = params.set('saldoMin', parametros.saldoEstoqueMin);
    }
    if (parametros.saldoEstoqueMax !== undefined && parametros.saldoEstoqueMax !== null) {
      params = params.set('saldoMax', parametros.saldoEstoqueMax);
    }
    if (parametros.totalEntradasMin !== undefined && parametros.totalEntradasMin !== null) {
      params = params.set('entradasMin', parametros.totalEntradasMin);
    }
    if (parametros.totalEntradasMax !== undefined && parametros.totalEntradasMax !== null) {
      params = params.set('entradasMax', parametros.totalEntradasMax);
    }
    if (parametros.totalSaidasMin !== undefined && parametros.totalSaidasMin !== null) {
      params = params.set('saidasMin', parametros.totalSaidasMin);
    }
    if (parametros.totalSaidasMax !== undefined && parametros.totalSaidasMax !== null) {
      params = params.set('saidasMax', parametros.totalSaidasMax);
    }
    if (parametros.apenasInativos) params = params.set('incluirInativos', true);

    return this.http.get<RespostaPaginada<GiroEstoqueItem>>(`${this.endpoint}/itens/tabela`, {
      params,
    });
  }

  // --- Locais de Armazenamento ---
  obterResumoArmazenamento(): Observable<ResumoArmazenamento> {
    return this.http.get<ResumoArmazenamento>(`${this.endpoint}/armazenamento`);
  }

  // --- Estoque Consolidado ---
  obterTotaisEstoqueGeral(): Observable<ResumoEstoqueGeral> {
    return this.http.get<ResumoEstoqueGeral>(`${this.endpoint}/estoque/totais`);
  }

  listarInventarioEstoque(
    parametros: InventarioEstoqueConsultaParametros,
  ): Observable<RespostaPaginada<ItemInventarioEstoque>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'itemNome,asc');

    if (parametros.nomeItem?.trim()) params = params.set('nomeItem', parametros.nomeItem.trim());
    if (parametros.categoriasIds?.length) {
      parametros.categoriasIds.forEach((id) => {
        params = params.append('categoriasIds', id.toString());
      });
    }
    if (parametros.locaisIds?.length) {
      parametros.locaisIds.forEach((id) => {
        params = params.append('locaisIds', id.toString());
      });
    }
    if (parametros.quantidadeMinima !== undefined && parametros.quantidadeMinima !== null) {
      params = params.set('quantidadeMinima', parametros.quantidadeMinima);
    }
    if (parametros.valorMinimo !== undefined && parametros.valorMinimo !== null) {
      params = params.set('valorMinimo', parametros.valorMinimo);
    }

    return this.http.get<RespostaPaginada<ItemInventarioEstoque>>(`${this.endpoint}/estoque/tabela`, {
      params,
    });
  }

  // --- Categorias de Itens ---
  obterResumoCategoriasItens(): Observable<ResumoCategoriasItens> {
    return this.http.get<ResumoCategoriasItens>(`${this.endpoint}/categorias/resumo`);
  }

  listarTabelaCategorias(
    parametros: CategoriaItemRelatorioConsultaParametros,
  ): Observable<RespostaPaginada<ItemTabelaCategoriaRelatorio>> {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'nome,asc');

    if (parametros.termoBusca?.trim()) {
      params = params.set('termoBusca', parametros.termoBusca.trim());
    }

    return this.http.get<RespostaPaginada<ItemTabelaCategoriaRelatorio>>(
      `${this.endpoint}/categorias/tabela`,
      { params },
    );
  }

  // --- Financeiro ---
  obterBalancoFinanceiro(filtros: FinanceiroFiltros): Observable<ResumoBalancoFinanceiro> {
    let params = new HttpParams();
    if (filtros.dataInicio) params = params.set('dataInicio', filtros.dataInicio);
    if (filtros.dataFim) params = params.set('dataFim', filtros.dataFim);

    return this.http.get<ResumoBalancoFinanceiro>(`${this.endpoint}/financeiro/balanco`, { params });
  }

  listarFinanceiroSaidas(
    parametros: FinanceiroConsultaParametros,
  ): Observable<RespostaPaginada<ItemFinanceiroTabela>> {
    const params = this.montarParamsFinanceiro(parametros);
    return this.http.get<RespostaPaginada<ItemFinanceiroTabela>>(
      `${this.endpoint}/financeiro/saidas`,
      { params },
    );
  }

  listarFinanceiroEntradas(
    parametros: FinanceiroConsultaParametros,
  ): Observable<RespostaPaginada<ItemFinanceiroTabela>> {
    const params = this.montarParamsFinanceiro(parametros);
    return this.http.get<RespostaPaginada<ItemFinanceiroTabela>>(
      `${this.endpoint}/financeiro/entradas`,
      { params },
    );
  }

  // --- Entradas ---
  obterResumoEntradas(filtros: EntradasFiltros): Observable<ResumoEntradas> {
    const params = this.montarParamsMovimentacao(filtros);
    return this.http.get<ResumoEntradas>(`${this.endpoint}/entradas/resumo`, { params });
  }

  listarEntradasDiscriminadas(
    parametros: EntradasConsultaParametros,
  ): Observable<RespostaPaginada<ItemEntradaDiscriminada>> {
    let params = this.montarParamsMovimentacao(parametros)
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'dataHora,desc');

    return this.http.get<RespostaPaginada<ItemEntradaDiscriminada>>(
      `${this.endpoint}/entradas/tabela`,
      { params },
    );
  }

  // --- Saídas ---
  obterResumoSaidas(filtros: SaidasFiltros): Observable<ResumoSaidas> {
    const params = this.montarParamsMovimentacao(filtros);
    return this.http.get<ResumoSaidas>(`${this.endpoint}/saidas/resumo`, { params });
  }

  listarSaidasDiscriminadas(
    parametros: SaidasConsultaParametros,
  ): Observable<RespostaPaginada<ItemSaidaDiscriminada>> {
    let params = this.montarParamsMovimentacao(parametros)
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'dataHora,desc');

    return this.http.get<RespostaPaginada<ItemSaidaDiscriminada>>(
      `${this.endpoint}/saidas/tabela`,
      { params },
    );
  }

  // --- Helpers de Parâmetros ---
  private montarParamsFinanceiro(parametros: FinanceiroConsultaParametros): HttpParams {
    let params = new HttpParams()
      .set('page', parametros.page)
      .set('size', parametros.size)
      .set('sort', parametros.sort ?? 'valorTotalFinanceiro,desc');

    if (parametros.dataInicio) params = params.set('dataInicio', parametros.dataInicio);
    if (parametros.dataFim) params = params.set('dataFim', parametros.dataFim);
    if (parametros.termoItem?.trim()) params = params.set('termoItem', parametros.termoItem.trim());
    if (parametros.termoCategoria?.trim()) {
      params = params.set('termoCategoria', parametros.termoCategoria.trim());
    }
    return params;
  }

  private montarParamsMovimentacao(filtros: EntradasFiltros | SaidasFiltros): HttpParams {
    let params = new HttpParams();
    if (filtros.dataInicio) params = params.set('dataInicio', filtros.dataInicio);
    if (filtros.dataFim) params = params.set('dataFim', filtros.dataFim);
    if (filtros.localArmazenamentoId) {
      params = params.set('localArmazenamentoId', filtros.localArmazenamentoId);
    }
    if (filtros.categoriaItemId) {
      params = params.set('categoriaItemId', filtros.categoriaItemId);
    }
    if (filtros.termoBusca?.trim()) {
      params = params.set('termoBusca', filtros.termoBusca.trim());
    }
    if (filtros.ano) params = params.set('ano', filtros.ano);

    if ('doadorId' in filtros && filtros.doadorId) {
      params = params.set('doadorId', filtros.doadorId);
    }
    if ('recebedorId' in filtros && filtros.recebedorId) {
      params = params.set('recebedorId', filtros.recebedorId);
    }
    return params;
  }
}
