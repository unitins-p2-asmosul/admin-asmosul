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
