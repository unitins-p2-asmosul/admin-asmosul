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

export function formatarDataHoraMovimentacao(dataHora: string | null | undefined): string {
  if (!dataHora) return '-';
  const limpo = dataHora.trim();
  const partes = limpo.split(/[T ]/);
  const dataParte = partes[0];
  const horaParte = partes[1] || '';
  const horaMinuto = horaParte.slice(0, 5);

  if (dataParte.includes('-')) {
    const segmentos = dataParte.split('-');
    if (segmentos.length === 3) {
      if (segmentos[0].length === 4) {
        // YYYY-MM-DD
        const [ano, mes, dia] = segmentos;
        return horaMinuto ? `${dia}/${mes}/${ano} ${horaMinuto}` : `${dia}/${mes}/${ano}`;
      } else {
        // DD-MM-YYYY
        const [dia, mes, ano] = segmentos;
        return horaMinuto ? `${dia}/${mes}/${ano} ${horaMinuto}` : `${dia}/${mes}/${ano}`;
      }
    }
  } else if (dataParte.includes('/')) {
    const segmentos = dataParte.split('/');
    if (segmentos.length === 3) {
      const [dia, mes, ano] = segmentos;
      return horaMinuto ? `${dia}/${mes}/${ano} ${horaMinuto}` : `${dia}/${mes}/${ano}`;
    }
  }
  return limpo;
}
