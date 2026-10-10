import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface CategoriaItemRequisicao {
  nome: string;
  descricao?: string;
}

export type CategoriaItemAtualizacao = CategoriaItemRequisicao;

export interface CategoriaItemResumo {
  id: number;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface CategoriaItemDetalhe {
  id: number;
  nome: string;
  descricao?: string;
  ativo?: boolean;
  dataInativo?: string;
}

export interface CategoriaItemFiltros {
  nome?: string;
  descricao?: string;
  apenasInativos?: boolean;
}

export interface CategoriaItemConsultaParametros extends ParametrosConsulta, CategoriaItemFiltros {}
