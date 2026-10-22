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

export interface ItemEstoqueLocal {
  idEndereco: number;
  nomeEndereco?: string;
  nomeLocalArmazenamento?: string;
  quantidade: number;
}

export interface ItemDetalhe {
  id: number;
  nome: string;
  categoria?: CategoriaItemDetalhe;
  estoque: number;
  locais?: ItemEstoqueLocal[];
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

export const UNIDADE_MEDIDA_SIGLAS: Record<string, string> = {
  QUILO: 'kg',
  KG: 'kg',
  UNIDADE: 'un',
  UN: 'un',
  GRAMA: 'g',
  G: 'g',
  METRO: 'm',
  M: 'm',
  CENTIMETRO: 'cm',
  CM: 'cm',
  MILIMETRO: 'mm',
  MM: 'mm',
  MILIGRAMA: 'mg',
  MG: 'mg',
  CAIXA: 'cx',
  CX: 'cx',
  PACOTE: 'pct',
  PCT: 'pct',
  FARDO: 'fd',
  FD: 'fd',
  LATA: 'lt',
  LT: 'lt',
};

export function obterSiglaUnidade(
  unidade: UnidadeMedidaItem | UnidadeMedidaCodigo | string | undefined,
): string {
  if (!unidade) return '';
  const chave = typeof unidade === 'string' ? unidade : unidade.codigo;
  return UNIDADE_MEDIDA_SIGLAS[chave?.toUpperCase()] || chave?.toLowerCase() || '';
}

export function formatarEstoqueComUnidade(
  estoque: number,
  unidade: UnidadeMedidaItem | UnidadeMedidaCodigo | string | undefined,
): string {
  const sigla = obterSiglaUnidade(unidade);
  return sigla ? `${estoque} ${sigla}` : `${estoque}`;
}

export function formatarPrecoUnitario(precoEmCentavos: number | null | undefined): string {
  if (precoEmCentavos === null || precoEmCentavos === undefined) return 'R$ 0,00';
  return (precoEmCentavos / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
