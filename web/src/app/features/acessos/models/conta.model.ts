export const PERFIS_CODIGOS = [
  'GERENCIADOR_PESSOAS',
  'GERENCIADOR_DOACOES',
  'GERENCIADOR_CAPACITACOES',
  'GERENCIADOR_ACESSO',
  'GERENCIADOR_RELATORIOS',
] as const;

export type PerfilCodigo = (typeof PERFIS_CODIGOS)[number];

export interface PerfilDisponivel {
  codigo: PerfilCodigo;
  descricao: string;
}

export interface ContaResumo {
  id: number;
  nomePessoa: string;
  email?: string;
  nomeUsuario: string;
  perfis: PerfilDisponivel[];
  redefinirSenha: boolean;
  ativo: boolean;
  dataCriacao: string;
}

export interface ContaDetalhe extends ContaResumo {
  pessoaId: number;
  dataInativo?: string;
}

export interface ContaRequisicao {
  pessoaId: number;
  nomeUsuario: string;
  senhaTemporaria: string;
  perfis: PerfilCodigo[];
}

export interface ContaAtualizacao {
  nomeUsuario: string;
}

export interface ContaRedefinirSenhaAdmin {
  novaSenhaTemporaria: string;
}

export interface ContaFiltros {
  nomePessoa?: string;
  nomeUsuario?: string;
  email?: string;
  perfis?: PerfilCodigo[];
  redefinirSenha?: boolean;
  dataCriacao?: string;
  dataInativo?: string;
  apenasInativos?: boolean;
}

export interface ContaConsultaParametros extends ContaFiltros {
  page: number;
  size: number;
  sort: string;
}