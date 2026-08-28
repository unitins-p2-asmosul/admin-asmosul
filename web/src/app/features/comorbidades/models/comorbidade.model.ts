export interface Comorbidade {
  id: number;
  nome: string;
  descricao?: string;
}

export interface ComorbidadePayload {
  nome: string;
  descricao?: string;
}
