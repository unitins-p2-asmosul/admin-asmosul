import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';

export interface ItemEntradaRequisicao {
  idItem: number;
  idArmazenamento: number;
  quantidade: number;
}

export interface ItemEntradaResumo {
  idItem: number;
  nomeItem: string;
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
}

export interface EntradaResumo {
  id: number;
  itens: ItemEntradaResumo[];
  quantidadeTotalItens: number;
  nomeDoador?: string;
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

export function formatarDataHoraEntrada(dataHora: string | null | undefined): string {
  if (!dataHora) return '-';

  const [data, hora] = dataHora.trim().split(' ');
  const partes = data?.split('-') ?? [];
  if (partes.length !== 3) return dataHora;

  const [dia, mes, ano] = partes;
  const horaMinuto = (hora ?? '').slice(0, 5);
  return horaMinuto ? `${dia}/${mes}/${ano} às ${horaMinuto}` : `${dia}/${mes}/${ano}`;
}
