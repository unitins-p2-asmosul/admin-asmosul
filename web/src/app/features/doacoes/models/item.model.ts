import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';
import { UnidadeMedidaCodigo, UnidadeMedidaItem } from './unidade-medida.model';
import { CategoriaItemResumo, CategoriaItemDetalhe } from './categoria-item.model';

export interface ItemRequisicao {
  nome: string;
  idCategoria: number;
  precoUnitario: number;
  unidadeMedida: UnidadeMedidaCodigo;
  descricao?: string;
}

export interface ItemAtualizacao {
  nome: string;
  idCategoria: number;
  precoUnitario: number;
  unidadeMedida: UnidadeMedidaCodigo;
  descricao?: string;
}

export interface ItemResumo {
  id: number;
  nome: string;
  categoria?: CategoriaItemResumo;
  estoque: number;
  precoUnitario: number;
  unidadeMedida: UnidadeMedidaItem | UnidadeMedidaCodigo;
  descricao?: string;
  ativo: boolean;
}

export interface ItemDetalhe {
  id: number;
  nome: string;
  categoria?: CategoriaItemDetalhe;
  estoque: number;
  precoUnitario: number;
  unidadeMedida: UnidadeMedidaItem | UnidadeMedidaCodigo;
  descricao?: string;
  ativo?: boolean;
  dataInativo?: string;
}

export interface ItemFiltros {
  nome?: string;
  categoriasIds?: number[];
  estoque?: number;
  precoUnitarioMinimo?: number;
  precoUnitarioMaximo?: number;
  apenasInativos?: boolean;
}

export interface ItemConsultaParametros extends ParametrosConsulta, ItemFiltros {}
