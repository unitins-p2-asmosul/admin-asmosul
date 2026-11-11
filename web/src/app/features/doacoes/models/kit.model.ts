import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';
import { UnidadeMedidaCodigo, UnidadeMedidaItem } from './unidade-medida.model';

export interface ItemKitRequisicao {
  idItem: number;
  quantidade: number;
}

export interface ItemKitDetalhe {
  idItem: number;
  nomeItem: string;
  unidadeMedida?: UnidadeMedidaItem | UnidadeMedidaCodigo | string;
  quantidade: number;
  sobraEstoque?: number;
}

export interface KitRequisicao {
  nome: string;
  descricao?: string;
  itens: ItemKitRequisicao[];
}

export interface KitAtualizacao {
  nome: string;
  descricao?: string;
  itens: ItemKitRequisicao[];
}

export interface KitResumo {
  id: number;
  nome: string;
  descricao?: string;
  totalItens: number;
  itensNomes: string[];
  quantidadeDistribuivel: number;
  ativo: boolean;
}

export interface KitDetalhe {
  id: number;
  nome: string;
  descricao?: string;
  totalItens: number;
  quantidadeDistribuivel: number;
  itens: ItemKitDetalhe[];
  ativo: boolean;
  dataInativo?: string;
}

export interface KitItemDisponibilidade {
  idItem: number;
  nomeItem: string;
  unidadeMedida?: UnidadeMedidaItem | UnidadeMedidaCodigo | string;
  quantidadePorKit: number;
  saldoEstoque: number;
  kitsPossiveisItem: number;
}

export interface KitDisponibilidade {
  idKit: number;
  nomeKit: string;
  localArmazenamentoId: number;
  nomeLocalArmazenamento?: string;
  quantidadeDisponivel: number;
  itens: KitItemDisponibilidade[];
}

export interface KitFiltros {
  nome?: string;
  descricao?: string;
  itensIds?: number[];
  quantidadeDistribuivel?: number;
  apenasInativos?: boolean;
}

export interface KitConsultaParametros extends ParametrosConsulta, KitFiltros {}
