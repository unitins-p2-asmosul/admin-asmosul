export enum Perfil {
  GERENCIADOR_PESSOAS = 'GERENCIADOR_PESSOAS',
  GERENCIADOR_DOACOES = 'GERENCIADOR_DOACOES',
  GERENCIADOR_CAPACITACOES = 'GERENCIADOR_CAPACITACOES',
  GERENCIADOR_ACESSO = 'GERENCIADOR_ACESSO',
  GERENCIADOR_RELATORIOS = 'GERENCIADOR_RELATORIOS',
}

export interface CredenciaisLogin {
  nomeUsuario: string;
  senha: string;
}

export interface PerfilResposta {
  codigo: Perfil | string;
  descricao: string;
}

export interface LoginResposta {
  token: string;
  tipo: string;
  expiracao: string;
  nomeUsuario: string;
  perfis: PerfilResposta[] | Perfil[] | string[];
  redefinirSenha?: boolean;
}

export interface UsuarioAutenticado {
  id: number;
  nomeUsuario: string;
  perfis: Perfil[];
  expiracao: number;
  redefinirSenha?: boolean;
}

export interface TokenJwtPayload {
  sub: string;
  id: number;
  perfis: Perfil[];
  exp: number;
  iss?: string;
  iat?: number;
  redefinirSenha?: boolean;
}

export interface RedefinirMinhaSenhaRequisicao {
  senhaAtual: string;
  novaSenha: string;
}
