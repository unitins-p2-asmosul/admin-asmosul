export interface CategoriaRequisicao {
  nome: string;
  descricao?: string;
}

export interface CategoriaResumo {
  id: number;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface CategoriaDetalhe {
  id: number;
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

export interface CategoriaConsultaParametros {
  page: number;
  size: number;
  sort?: string;
  incluirInativos?: boolean;
  apenasDesativados?: boolean;
  nome?: string;
}
