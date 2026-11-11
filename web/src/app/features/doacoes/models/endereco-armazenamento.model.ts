import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';
import { UfCodigo, UfItem } from '@features/pessoas/models/pessoa.model';
import { UnidadeMedidaCodigo, UnidadeMedidaItem } from './unidade-medida.model';

export interface EnderecoArmazenamentoRequisicao {
  nome: string;
  cep: string;
  uf: UfCodigo | string;
  cidade: string;
  bairro: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  informacoesAdicionais?: string;
}

export type EnderecoArmazenamentoAtualizacao = EnderecoArmazenamentoRequisicao;

export interface EnderecoArmazenamentoResumo {
  id: number;
  nome: string;
  cep: string;
  uf: UfItem | UfCodigo | string;
  cidade: string;
  bairro: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  volumeFisico?: number;
  ativo: boolean;
}

export interface EnderecoEstoqueItem {
  idItem: number;
  nomeItem: string;
  categoria: string;
  unidadeMedida:
    | UnidadeMedidaItem
    | UnidadeMedidaCodigo
    | string
    | { codigo?: string; descricao?: string; sigla?: string; nome?: string };
  quantidade: number;
}

export interface EnderecoArmazenamentoDetalhe {
  id: number;
  nome: string;
  cep: string;
  uf: UfItem | UfCodigo | string;
  cidade: string;
  bairro: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  informacoesAdicionais?: string;
  itens?: EnderecoEstoqueItem[];
  ativo?: boolean;
  dataInativo?: string;
}

export interface EnderecoArmazenamentoFiltros {
  nome?: string;
  bairro?: string;
  logradouro?: string;
  apenasInativos?: boolean;
}

export interface EnderecoArmazenamentoConsultaParametros
  extends ParametrosConsulta, EnderecoArmazenamentoFiltros {}
