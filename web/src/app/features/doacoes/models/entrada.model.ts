import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface ItemEntradaRequisicao {
  idItem: number;
  idArmazenamento: number;
  quantidade: number;
}

export interface ItemEntradaDetalhe {
  id: number;
  idItem: number;
  nomeItem: string;
  idArmazenamento: number;
  nomeArmazenamento: string;
  quantidade: number;
}

export interface EntradaRequisicao {
  idDoador: number;
  descricao?: string;
  itens: ItemEntradaRequisicao[];
}

export interface EntradaAtualizacao {
  idDoador: number;
  descricao?: string;
  itens: ItemEntradaRequisicao[];
}

export interface EntradaResumo {
  id: number;
  nomeDoador?: string;
  quantidadeTotalItens: number;
  dataHora: string;
}

export interface EntradaDetalhe {
  id: number;
  idDoador?: number;
  nomeDoador?: string;
  dataHora: string;
  descricao?: string;
  itens: ItemEntradaDetalhe[];
}

export interface EntradaFiltros {
  itemId?: number;
  quantidade?: number;
  doadorId?: number;
  data?: string;
  enderecoId?: number;
}

export interface EntradaConsultaParametros extends ParametrosConsulta, EntradaFiltros {}
