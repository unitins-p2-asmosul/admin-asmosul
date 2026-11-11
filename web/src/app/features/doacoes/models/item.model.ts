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
  unidade:
    | UnidadeMedidaItem
    | UnidadeMedidaCodigo
    | string
    | { codigo?: string; descricao?: string; sigla?: string; nome?: string }
    | undefined,
): string {
  if (!unidade) return '';
  if (typeof unidade === 'object') {
    const obj = unidade as Record<string, any>;
    if (obj['sigla']) {
      const siglaUpper = String(obj['sigla']).toUpperCase();
      return UNIDADE_MEDIDA_SIGLAS[siglaUpper] || String(obj['sigla']).toLowerCase();
    }
    const chave = obj['codigo'] || obj['nome'];
    if (chave) {
      const chaveUpper = String(chave).toUpperCase();
      if (UNIDADE_MEDIDA_SIGLAS[chaveUpper]) {
        return UNIDADE_MEDIDA_SIGLAS[chaveUpper];
      }
    }
    if (obj['descricao']) {
      const descUpper = String(obj['descricao'])
        .replace(/\s*\([^)]*\)/, '')
        .trim()
        .toUpperCase();
      if (UNIDADE_MEDIDA_SIGLAS[descUpper]) {
        return UNIDADE_MEDIDA_SIGLAS[descUpper];
      }
      return String(obj['descricao']);
    }
    return chave ? String(chave).toLowerCase() : '';
  }
  const chaveUpper = String(unidade).trim().toUpperCase();
  return UNIDADE_MEDIDA_SIGLAS[chaveUpper] || String(unidade).toLowerCase();
}

export const MAPA_SIGLA_PARA_UNIDADE_MEDIDA_CODIGO: Record<string, UnidadeMedidaCodigo> = {
  QUILO: UnidadeMedidaCodigo.QUILO,
  KG: UnidadeMedidaCodigo.QUILO,
  UNIDADE: UnidadeMedidaCodigo.UNIDADE,
  UN: UnidadeMedidaCodigo.UNIDADE,
  GRAMA: UnidadeMedidaCodigo.GRAMA,
  G: UnidadeMedidaCodigo.GRAMA,
  METRO: UnidadeMedidaCodigo.METRO,
  M: UnidadeMedidaCodigo.METRO,
  CENTIMETRO: UnidadeMedidaCodigo.CENTIMETRO,
  CM: UnidadeMedidaCodigo.CENTIMETRO,
  MILIMETRO: UnidadeMedidaCodigo.MILIMETRO,
  MM: UnidadeMedidaCodigo.MILIMETRO,
  MILIGRAMA: UnidadeMedidaCodigo.MILIGRAMA,
  MG: UnidadeMedidaCodigo.MILIGRAMA,
  CAIXA: UnidadeMedidaCodigo.CAIXA,
  CX: UnidadeMedidaCodigo.CAIXA,
  PACOTE: UnidadeMedidaCodigo.PACOTE,
  PCT: UnidadeMedidaCodigo.PACOTE,
  FARDO: UnidadeMedidaCodigo.FARDO,
  FD: UnidadeMedidaCodigo.FARDO,
  LATA: UnidadeMedidaCodigo.LATA,
  LT: UnidadeMedidaCodigo.LATA,
};

export function normalizarUnidadeMedidaCodigo(
  unidade:
    | UnidadeMedidaItem
    | UnidadeMedidaCodigo
    | string
    | { codigo?: string; descricao?: string; sigla?: string; nome?: string; name?: string }
    | null
    | undefined,
): UnidadeMedidaCodigo | null {
  if (!unidade) return null;
  if (typeof unidade === 'string') {
    const limpo = unidade.trim().toUpperCase();
    if (MAPA_SIGLA_PARA_UNIDADE_MEDIDA_CODIGO[limpo]) {
      return MAPA_SIGLA_PARA_UNIDADE_MEDIDA_CODIGO[limpo];
    }
    const semParenteses = limpo.replace(/\s*\([^)]*\)/, '').trim();
    if (MAPA_SIGLA_PARA_UNIDADE_MEDIDA_CODIGO[semParenteses]) {
      return MAPA_SIGLA_PARA_UNIDADE_MEDIDA_CODIGO[semParenteses];
    }
  } else if (typeof unidade === 'object') {
    const obj = unidade as Record<string, any>;
    if (obj['codigo']) {
      const cod = normalizarUnidadeMedidaCodigo(obj['codigo']);
      if (cod) return cod;
    }
    if (obj['sigla']) {
      const cod = normalizarUnidadeMedidaCodigo(obj['sigla']);
      if (cod) return cod;
    }
    if (obj['nome']) {
      const cod = normalizarUnidadeMedidaCodigo(obj['nome']);
      if (cod) return cod;
    }
    if (obj['name']) {
      const cod = normalizarUnidadeMedidaCodigo(obj['name']);
      if (cod) return cod;
    }
    if (obj['descricao']) {
      const cod = normalizarUnidadeMedidaCodigo(obj['descricao']);
      if (cod) return cod;
    }
  }
  return null;
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
