import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface ItemMovimentacaoRequisicao {
  idItem: number;
  idArmazenamentoAntigo: number;
  quantidade: number;
}

export interface ItemMovimentacaoDetalhe {
  id: number;
  idItem: number;
  nomeItem: string;
  idArmazenamentoAntigo: number;
  nomeArmazenamentoAntigo: string;
  quantidade: number;
  unidadeMedida?: string;
}

export interface MovimentacaoRequisicao {
  idNovoArmazenamento: number;
  descricao?: string;
  itens: ItemMovimentacaoRequisicao[];
}

export interface MovimentacaoAtualizacao {
  descricao?: string;
}

export interface MovimentacaoResumo {
  id: number;
  nomeItem?: string;
  localAntigo?: string;
  localNovo?: string;
  quantidade: string;
  dataHora: string;
}

export interface MovimentacaoDetalhe {
  id: number;
  idNovoArmazenamento?: number;
  nomeNovoArmazenamento?: string;
  dataHora: string;
  descricao?: string;
  itens: ItemMovimentacaoDetalhe[];
}

export interface MovimentacaoFiltros {
  itemId?: number;
  localAntigoId?: number;
  localNovoId?: number;
  quantidade?: number;
  dataHoraInicio?: string;
  dataHoraFim?: string;
}

export interface MovimentacaoConsultaParametros extends ParametrosConsulta, MovimentacaoFiltros {}
