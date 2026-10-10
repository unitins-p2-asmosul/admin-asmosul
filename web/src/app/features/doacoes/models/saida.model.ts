import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface ItemSaidaRequisicao {
  idItem: number;
  idArmazenamento: number;
  quantidade: number;
}

export interface KitSaidaRequisicao {
  idKit: number;
  quantidade: number;
  idArmazenamento: number;
}

export interface ItemSaidaDetalhe {
  id: number;
  idItem: number;
  nomeItem: string;
  idArmazenamento: number;
  nomeArmazenamento: string;
  quantidade: number;
}

export interface SaidaRequisicao {
  idRecebedor: number;
  descricao?: string;
  itens?: ItemSaidaRequisicao[];
  kits?: KitSaidaRequisicao[];
}

export interface SaidaAtualizacao {
  idRecebedor: number;
  descricao?: string;
  itens: ItemSaidaRequisicao[];
}

export interface SaidaResumo {
  id: number;
  nomeRecebedor?: string;
  itensOuKitsDescricao: string[];
  quantidadeTotal: number;
  dataHora: string;
}

export interface SaidaDetalhe {
  id: number;
  idRecebedor?: number;
  nomeRecebedor?: string;
  dataHora: string;
  descricao?: string;
  itens: ItemSaidaDetalhe[];
}

export interface SaidaFiltros {
  itensIds?: number[];
  kitsIds?: number[];
  quantidade?: number;
  recebedorId?: number;
  data?: string;
}

export interface SaidaConsultaParametros extends ParametrosConsulta, SaidaFiltros {}
