import { ItemDominio } from '@features/shared/models/item-dominio.model';

export enum TipoPessoa {
  FISICA = 'FISICA',
  JURIDICA = 'JURIDICA',
}

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

export enum UfCodigo {
  AC = 'AC',
  AL = 'AL',
  AP = 'AP',
  AM = 'AM',
  BA = 'BA',
  CE = 'CE',
  DF = 'DF',
  ES = 'ES',
  GO = 'GO',
  MA = 'MA',
  MT = 'MT',
  MS = 'MS',
  MG = 'MG',
  PA = 'PA',
  PB = 'PB',
  PR = 'PR',
  PE = 'PE',
  PI = 'PI',
  RJ = 'RJ',
  RN = 'RN',
  RS = 'RS',
  RO = 'RO',
  RR = 'RR',
  SC = 'SC',
  SP = 'SP',
  SE = 'SE',
  TO = 'TO',
}

export type UfItem = ItemDominio<UfCodigo>;

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

export const UF_OPCOES: readonly UfItem[] = [
  { codigo: UfCodigo.AC, descricao: 'Acre' },
  { codigo: UfCodigo.AL, descricao: 'Alagoas' },
  { codigo: UfCodigo.AP, descricao: 'Amapá' },
  { codigo: UfCodigo.AM, descricao: 'Amazonas' },
  { codigo: UfCodigo.BA, descricao: 'Bahia' },
  { codigo: UfCodigo.CE, descricao: 'Ceará' },
  { codigo: UfCodigo.DF, descricao: 'Distrito Federal' },
  { codigo: UfCodigo.ES, descricao: 'Espírito Santo' },
  { codigo: UfCodigo.GO, descricao: 'Goiás' },
  { codigo: UfCodigo.MA, descricao: 'Maranhão' },
  { codigo: UfCodigo.MT, descricao: 'Mato Grosso' },
  { codigo: UfCodigo.MS, descricao: 'Mato Grosso do Sul' },
  { codigo: UfCodigo.MG, descricao: 'Minas Gerais' },
  { codigo: UfCodigo.PA, descricao: 'Pará' },
  { codigo: UfCodigo.PB, descricao: 'Paraíba' },
  { codigo: UfCodigo.PR, descricao: 'Paraná' },
  { codigo: UfCodigo.PE, descricao: 'Pernambuco' },
  { codigo: UfCodigo.PI, descricao: 'Piauí' },
  { codigo: UfCodigo.RJ, descricao: 'Rio de Janeiro' },
  { codigo: UfCodigo.RN, descricao: 'Rio Grande do Norte' },
  { codigo: UfCodigo.RS, descricao: 'Rio Grande do Sul' },
  { codigo: UfCodigo.RO, descricao: 'Rondônia' },
  { codigo: UfCodigo.RR, descricao: 'Roraima' },
  { codigo: UfCodigo.SC, descricao: 'Santa Catarina' },
  { codigo: UfCodigo.SP, descricao: 'São Paulo' },
  { codigo: UfCodigo.SE, descricao: 'Sergipe' },
  { codigo: UfCodigo.TO, descricao: 'Tocantins' },
];

export interface ItemRelacionadoResumo {
  id: number;
  nome: string;
}

export interface CepDados {
  cep: string;
  logradouro: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  uf: UfCodigo | string;
}

export interface PessoaRequisicao {
  nome: string;
  cpfCnpj: string;
  tipoPessoa?: TipoPessoa;
  dataNascimento?: string;
  sexo?: SexoCodigo;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeCodigo;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarCodigo;
  comorbidades?: number[];
  categorias?: number[];
  descricao?: string;
  cep?: string;
  uf?: UfCodigo | string;
  cidade?: string;
  bairro?: string;
  logradouro?: string;
  complementoEndereco?: string;
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
}

export interface PessoaDetalhe {
  id: number;
  nome: string;
  cpfCnpj: string;
  tipoPessoa?: TipoPessoa;
  dataNascimento?: string;
  sexo?: SexoItem;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeItem;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarItem;
  comorbidades?: ItemRelacionadoResumo[] | number[];
  categorias?: ItemRelacionadoResumo[] | number[];
  descricao?: string;
  cep?: string;
  uf?: UfItem | UfCodigo | string;
  cidade?: string;
  bairro?: string;
  logradouro?: string;
  complementoEndereco?: string;
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
  ativo?: boolean;
}
