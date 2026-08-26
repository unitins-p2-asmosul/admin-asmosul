import { ItemDominio } from '@features/shared/models/item-dominio.model';

export enum SexoCodigo {
  FEMININO = 'FEMININO',
  MASCULINO = 'MASCULINO',
  PREFIRO_NAO_INFORMAR = 'PREFIRO_NAO_INFORMAR',
}

export type SexoItem = ItemDominio<SexoCodigo>;

export enum EscolaridadeCodigo {
  FUNDAMENTAL_INCOMPLETO = 'FUNDAMENTAL_INCOMPLETO',
  FUNDAMENTAL_COMPLETO = 'FUNDAMENTAL_COMPLETO',
  ENSINO_MEDIO_INCOMPLETO = 'ENSINO_MEDIO_INCOMPLETO',
  ENSINO_MEDIO_COMPLETO = 'ENSINO_MEDIO_COMPLETO',
  SUPERIOR_INCOMPLETO = 'SUPERIOR_INCOMPLETO',
  SUPERIOR_COMPLETO = 'SUPERIOR_COMPLETO',
}

export type EscolaridadeItem = ItemDominio<EscolaridadeCodigo>;

export enum RendaFamiliarCodigo {
  MENOS_DE_MIL = 'MENOS_DE_MIL',
  ENTRE_MIL_E_DOIS_MIL = 'ENTRE_MIL_E_DOIS_MIL',
  ENTRE_DOIS_MIL_E_TRES_MIL = 'ENTRE_DOIS_MIL_E_TRES_MIL',
  MAIS_DE_TRES_MIL = 'MAIS_DE_TRES_MIL',
}

export type RendaFamiliarItem = ItemDominio<RendaFamiliarCodigo>;

export const SEXO_OPCOES: readonly SexoItem[] = [
  { codigo: SexoCodigo.FEMININO, descricao: 'Feminino' },
  { codigo: SexoCodigo.MASCULINO, descricao: 'Masculino' },
  { codigo: SexoCodigo.PREFIRO_NAO_INFORMAR, descricao: 'Prefiro não informar' },
];

export const ESCOLARIDADE_OPCOES: readonly EscolaridadeItem[] = [
  { codigo: EscolaridadeCodigo.FUNDAMENTAL_INCOMPLETO, descricao: 'Fundamental incompleto' },
  { codigo: EscolaridadeCodigo.FUNDAMENTAL_COMPLETO, descricao: 'Fundamental completo' },
  { codigo: EscolaridadeCodigo.ENSINO_MEDIO_INCOMPLETO, descricao: 'Ensino médio incompleto' },
  { codigo: EscolaridadeCodigo.ENSINO_MEDIO_COMPLETO, descricao: 'Ensino médio completo' },
  { codigo: EscolaridadeCodigo.SUPERIOR_INCOMPLETO, descricao: 'Superior incompleto' },
  { codigo: EscolaridadeCodigo.SUPERIOR_COMPLETO, descricao: 'Superior completo' },
];

export const RENDA_FAMILIAR_OPCOES: readonly RendaFamiliarItem[] = [
  { codigo: RendaFamiliarCodigo.MENOS_DE_MIL, descricao: 'Menos de R$ 1.000' },
  { codigo: RendaFamiliarCodigo.ENTRE_MIL_E_DOIS_MIL, descricao: 'Entre R$ 1.000 e R$ 2.000' },
  { codigo: RendaFamiliarCodigo.ENTRE_DOIS_MIL_E_TRES_MIL, descricao: 'Entre R$ 2.000 e R$ 3.000' },
  { codigo: RendaFamiliarCodigo.MAIS_DE_TRES_MIL, descricao: 'Mais de R$ 3.000' },
];

export interface ItemRelacionadoResumo {
  id: number;
  nome: string;
}

export interface PessoaRequisicao {
  nome: string;
  cpf: string;
  dataNascimento: string;
  sexo?: SexoCodigo;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeCodigo;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarCodigo;
  comorbidades?: number[];
  categorias?: number[];
  descricao?: string;
}

export interface PessoaDetalhe {
  id: number;
  nome: string;
  cpf: string;
  dataNascimento: string;
  sexo?: SexoItem;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeItem;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarItem;
  comorbidades?: number[];
  categorias?: number[];
  descricao?: string;
}
