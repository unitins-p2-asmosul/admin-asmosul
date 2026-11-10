import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

/**
 * Models de Comorbidade, espelhando os schemas do Swagger
 * (ComorbidadeDTO.Resumo, .Detalhe, .Requisicao e .Atualizacao).
 */

/** Item da listagem paginada (GET /comorbidades). */
export interface ComorbidadeResumo {
  id: number;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

/**
 * Retorno de GET /comorbidades/{id}, POST e PUT.
 *
 * O campo `ativo` ainda não é devolvido pelo backend neste DTO
 * (só existe no Resumo). Fica opcional até o contrato ser ajustado.
 */
export interface ComorbidadeDetalhe {
  id: number;
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

/** Corpo de POST /comorbidades. */
export interface ComorbidadeRequisicao {
  nome: string;
  descricao?: string;
}

/** Corpo de PUT /comorbidades/{id}. No Swagger tem a mesma estrutura da Requisicao. */
export type ComorbidadeAtualizacao = ComorbidadeRequisicao;

/**
 * The API supports `nome`; inclusion of inactive records comes from shared query parameters.
 */
export interface ComorbidadeFiltros {
  nome?: string;
}

/** Parâmetros finais enviados à API (paginação + filtros). */
export interface ComorbidadeConsultaParametros extends ParametrosConsulta, ComorbidadeFiltros {}
