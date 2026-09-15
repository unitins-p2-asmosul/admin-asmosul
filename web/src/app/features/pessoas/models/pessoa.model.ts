import { ItemDominio } from '@features/shared/models/item-dominio.model';

export enum SexoCodigo {
  FEMININO = 'FEMININO',
  MASCULINO = 'MASCULINO',
  PREFIRO_NAO_INFORMAR = 'PREFIRO_NAO_INFORMAR',
}

export enum TipoPessoaCodigo {
  FISICA = 'FISICA',
  JURIDICA = 'JURIDICA',
}

export enum UfCodigo {
  AC = 'AC', AL = 'AL', AP = 'AP', AM = 'AM', BA = 'BA', CE = 'CE', DF = 'DF', ES = 'ES',
  GO = 'GO', MA = 'MA', MT = 'MT', MS = 'MS', MG = 'MG', PA = 'PA', PB = 'PB', PR = 'PR',
  PE = 'PE', PI = 'PI', RJ = 'RJ', RN = 'RN', RS = 'RS', RO = 'RO', RR = 'RR', SC = 'SC',
  SP = 'SP', SE = 'SE', TO = 'TO',
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

export interface PessoaResumo {
  id: number;
  nome: string;
  cpf: string;
  cpfCnpj?: string;
  tipoPessoa?: string;
  dataNascimento?: string;
  telefone: string;
  email?: string;
  sexo?: SexoItem;
  escolaridade?: EscolaridadeItem;
  profissao?: string;
  bairro?: string;
  rendaFamiliar?: RendaFamiliarItem;
  comorbidades?: number[];
  categorias?: number[];
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
  ativo: boolean;
}

export interface PessoaRequisicao {
  nome: string;
  cpfCnpj: string;
  tipoPessoa?: TipoPessoaCodigo;
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
  uf?: UfCodigo;
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
  cpf: string;
  cpfCnpj?: string;
  tipoPessoa?: string;
  dataNascimento?: string;
  sexo?: SexoItem;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeItem;
  profissao?: string;
  bairro?: string;
  cep?: string;
  uf?: UfCodigo;
  cidade?: string;
  logradouro?: string;
  complementoEndereco?: string;
  rendaFamiliar?: RendaFamiliarItem;
  comorbidades?: number[];
  categorias?: number[];
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
  descricao?: string;
  ativo?: boolean;
}

export interface PessoaFiltros {
  nome?: string;
  cpfCnpj?: string;
  tipoPessoa?: string;
  dataNascimento?: string;
  sexo?: SexoCodigo;
  telefone?: string;
  email?: string;
  escolaridade?: EscolaridadeCodigo;
  profissao?: string;
  bairro?: string;
  rendaFamiliar?: RendaFamiliarCodigo;
  comorbidadeId?: number;
  categoriaId?: number;
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
  apenasInativos?: boolean;
}

export interface PessoaConsultaParametros {
  page: number;
  size: number;
  sort?: string;
  incluirInativos?: boolean;
  apenasInativos?: boolean;
  nome?: string;
  cpfCnpj?: string;
  tipoPessoa?: string;
  dataNascimento?: string;
  sexo?: SexoCodigo;
  telefone?: string;
  email?: string;
  escolaridade?: EscolaridadeCodigo;
  profissao?: string;
  bairro?: string;
  rendaFamiliar?: RendaFamiliarCodigo;
  comorbidadeId?: number;
  categoriaId?: number;
  quantidadeCoabitantes?: number;
  ehBeneficiario?: boolean;
  ehDoador?: boolean;
}
