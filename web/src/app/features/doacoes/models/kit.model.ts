import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface ItemKitRequisicao {
  idItem: number;
  quantidade: number;
}

export interface ItemKitDetalhe {
  idItem: number;
  nomeItem: string;
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
  itensNomes: string[];
  quantidadeDistribuivel: number;
}

export interface KitDetalhe {
  id: number;
  nome: string;
  descricao?: string;
  quantidadeDistribuivel: number;
  itens: ItemKitDetalhe[];
}

export interface KitFiltros {
  nome?: string;
  itensIds?: number[];
  quantidadeDistribuivel?: number;
  apenasInativos?: boolean;
}

export interface KitConsultaParametros extends ParametrosConsulta, KitFiltros {}
