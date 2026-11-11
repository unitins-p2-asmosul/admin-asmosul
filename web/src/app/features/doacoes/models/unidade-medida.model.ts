import { ItemDominio } from '@features/shared/models/item-dominio.model';

export enum UnidadeMedidaCodigo {
  QUILO = 'QUILO',
  UNIDADE = 'UNIDADE',
  GRAMA = 'GRAMA',
  METRO = 'METRO',
  CENTIMETRO = 'CENTIMETRO',
  MILIMETRO = 'MILIMETRO',
  MILIGRAMA = 'MILIGRAMA',
  CAIXA = 'CAIXA',
  PACOTE = 'PACOTE',
  FARDO = 'FARDO',
  LATA = 'LATA',
}

export type UnidadeMedidaItem = ItemDominio<UnidadeMedidaCodigo>;
export type UnidadeMedidaApi = {
  codigo: string;
  descricao?: string;
  sigla?: string;
};
export type UnidadeMedidaValor =
  UnidadeMedidaItem | UnidadeMedidaApi | UnidadeMedidaCodigo | string;

export const UNIDADE_MEDIDA_OPCOES: readonly UnidadeMedidaItem[] = [
  { codigo: UnidadeMedidaCodigo.QUILO, descricao: 'Quilo (kg)' },
  { codigo: UnidadeMedidaCodigo.UNIDADE, descricao: 'Unidade (un)' },
  { codigo: UnidadeMedidaCodigo.GRAMA, descricao: 'Grama (g)' },
  { codigo: UnidadeMedidaCodigo.METRO, descricao: 'Metro (m)' },
  { codigo: UnidadeMedidaCodigo.CENTIMETRO, descricao: 'Centímetro (cm)' },
  { codigo: UnidadeMedidaCodigo.MILIMETRO, descricao: 'Milímetro (mm)' },
  { codigo: UnidadeMedidaCodigo.MILIGRAMA, descricao: 'Miligrama (mg)' },
  { codigo: UnidadeMedidaCodigo.CAIXA, descricao: 'Caixa (cx)' },
  { codigo: UnidadeMedidaCodigo.PACOTE, descricao: 'Pacote (pct)' },
  { codigo: UnidadeMedidaCodigo.FARDO, descricao: 'Fardo (fd)' },
  { codigo: UnidadeMedidaCodigo.LATA, descricao: 'Lata (lt)' },
];

const CODIGOS_POR_SIGLA: Record<string, UnidadeMedidaCodigo> = {
  KG: UnidadeMedidaCodigo.QUILO,
  UN: UnidadeMedidaCodigo.UNIDADE,
  G: UnidadeMedidaCodigo.GRAMA,
  M: UnidadeMedidaCodigo.METRO,
  CM: UnidadeMedidaCodigo.CENTIMETRO,
  MM: UnidadeMedidaCodigo.MILIMETRO,
  MG: UnidadeMedidaCodigo.MILIGRAMA,
  CX: UnidadeMedidaCodigo.CAIXA,
  PCT: UnidadeMedidaCodigo.PACOTE,
  FD: UnidadeMedidaCodigo.FARDO,
  LT: UnidadeMedidaCodigo.LATA,
};

export function obterCodigoUnidadeMedida(
  unidade: UnidadeMedidaValor | null | undefined,
): UnidadeMedidaCodigo | null {
  if (!unidade) return null;

  const valor =
    typeof unidade === 'string'
      ? unidade
      : unidade.codigo || ('sigla' in unidade ? unidade.sigla : '') || unidade.descricao || '';
  const normalizado = valor.trim().toLocaleUpperCase();
  const opcao = UNIDADE_MEDIDA_OPCOES.find(
    ({ codigo, descricao }) =>
      codigo === normalizado || descricao.toLocaleUpperCase().startsWith(normalizado),
  );

  return opcao?.codigo ?? CODIGOS_POR_SIGLA[normalizado] ?? null;
}
