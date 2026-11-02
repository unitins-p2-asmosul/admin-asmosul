import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';
import { ItemGrafico, ItemRanking } from './relatorio-comum.model';

export type CriterioAgrupamentoPessoa = 'SEXO' | 'ESCOLARIDADE' | 'FAIXA_RENDA';

export interface ResumoPessoasGeral {
  totalBeneficiarios: number;
  totalDoadores: number;
  totalCadastrosGerais: number;
  totalInativos: number;
  totalPessoasGrafico: number;
  distribuicao: ItemGrafico[];
}

export interface ResumoComorbidades {
  totalOcorrencias: number;
  top5MaisFrequentes: ItemRanking[];
  distribuicao: ItemGrafico[];
}

export interface PessoaComorbidadeItem {
  idPessoa: number;
  nome: string;
  cpf: string;
  contato: string;
  genero: string;
  comorbidadesDiagnosticadas: string[];
}

export interface PessoaComorbidadeFiltros {
  termoBusca?: string;
  comorbidadeIds?: number[];
}

export interface PessoaComorbidadeConsultaParametros
  extends ParametrosConsulta,
    PessoaComorbidadeFiltros {}

export interface ResumoCategoriasPessoa {
  totalVinculos: number;
  top5MaisFrequentes: ItemRanking[];
  distribuicao: ItemGrafico[];
}
