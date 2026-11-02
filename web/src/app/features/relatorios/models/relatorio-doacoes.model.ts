import { ParametrosConsulta } from '@features/shared/models/parametros-consulta.model';
import { UnidadeMedidaCodigo, UnidadeMedidaItem } from '@features/doacoes/models/unidade-medida.model';
import { ItemGrafico, ItemRanking } from './relatorio-comum.model';

// --- Itens e Giro de Estoque ---
export interface ResumoItensGiro {
  itensMaisSaem: ItemRanking[];
  itensMenosSaem: ItemRanking[];
  itensMaisEntram: ItemRanking[];
  itensMenosEntram: ItemRanking[];
}

export interface GiroEstoqueItem {
  idItem: number;
  nomeItem: string;
  nomeCategoria: string;
  unidadeMedida: UnidadeMedidaItem | UnidadeMedidaCodigo | string;
  saldoEstoque: number;
  totalEntradas: number;
  totalSaidas: number;
}

export interface GiroEstoqueFiltros {
  nomeItem?: string;
  categoriaId?: number;
  unidadeMedida?: string;
  saldoEstoqueMin?: number;
  saldoEstoqueMax?: number;
  totalEntradasMin?: number;
  totalEntradasMax?: number;
  totalSaidasMin?: number;
  totalSaidasMax?: number;
}

export interface GiroEstoqueConsultaParametros extends ParametrosConsulta, GiroEstoqueFiltros {}

// --- Locais de Armazenamento ---
export interface ResumoArmazenamento {
  totalLocais: number;
  volumeFisicoTotal: number;
  locaisEmUso: number;
  distribuicaoVolumePorLocal: ItemGrafico[];
}

// --- Estoque Consolidado ---
export interface ResumoEstoqueGeral {
  saldoFisicoGeral: number;
  valorTotalEstimado: number;
}

export interface ItemInventarioEstoque {
  idEstoque: number;
  idItem: number;
  nomeItem: string;
  nomeCategoria: string;
  localArmazenamento: string;
  quantidade: number;
  unidadeMedida: string;
  valorUnitarioEstimado: number;
  valorTotalEstimado: number;
}

export interface InventarioEstoqueFiltros {
  nomeItem?: string;
  categoriasIds?: number[];
  locaisIds?: number[];
  quantidadeMinima?: number;
  valorMinimo?: number;
}

export interface InventarioEstoqueConsultaParametros
  extends ParametrosConsulta,
    InventarioEstoqueFiltros {}

// --- Categorias de Itens ---
export interface ResumoCategoriasItens {
  totalItensCatalogo: number;
  top5MaisVariedade: ItemRanking[];
  distribuicaoParticipacao: ItemGrafico[];
}

export interface ItemTabelaCategoriaRelatorio {
  idCategoria: number;
  nomeCategoria: string;
  descricao?: string;
  itensDistintos: number;
  saldoFisico: number;
}

export interface CategoriaItemRelatorioConsultaParametros extends ParametrosConsulta {
  termoBusca?: string;
}

// --- Financeiro ---
export interface ResumoBalancoFinanceiro {
  somatorioSaidas: number;
  somatorioEntradas: number;
  balancoPeriodo: number;
  statusBalanco: string;
}

export interface ItemFinanceiroTabela {
  itemId: number;
  itemNome: string;
  categoriaNome: string;
  quantidade: number;
  unidadeMedida: string;
  precoUnitarioMedio: number;
  valorTotalFinanceiro: number;
}

export interface FinanceiroFiltros {
  dataInicio?: string;
  dataFim?: string;
  termoItem?: string;
  termoCategoria?: string;
}

export interface FinanceiroConsultaParametros extends ParametrosConsulta, FinanceiroFiltros {}

// --- Entradas ---
export interface ItemRankingDoador {
  posicao: number;
  idDoador: number;
  nomeDoador: string;
  totalDoacoesRealizadas: number;
  totalItensDoados: number;
}

export interface ResumoEntradas {
  totalItensRecebidos: number;
  valorEstimadoRecebido: number;
  entradasPorMes: ItemGrafico[];
  entradasPorAno: ItemGrafico[];
  topDoadores: ItemRankingDoador[];
}

export interface ItemEntradaDiscriminada {
  itemNome: string;
  categoriaNome: string;
  quantidade: number;
  precoUnitario: number;
  valorEstimado: number;
  localArmazenamento: string;
  doadorNome: string;
  cpfCnpj: string;
  dataHora: string;
  observacao?: string;
}

export interface EntradasFiltros {
  dataInicio?: string;
  dataFim?: string;
  doadorId?: number;
  localArmazenamentoId?: number;
  categoriaItemId?: number;
  termoBusca?: string;
  ano?: number;
}

export interface EntradasConsultaParametros extends ParametrosConsulta, EntradasFiltros {}

// --- Saídas ---
export interface ItemRankingRecebedor {
  posicao: number;
  idRecebedor: number;
  nomeRecebedor: string;
  membrosFamilia: number;
  totalEntregasRecebidas: number;
  totalItensRecebidos: number;
}

export interface ResumoSaidas {
  totalItensEntregues: number;
  valorEstimadoEntregue: number;
  saidasPorMes: ItemGrafico[];
  saidasPorAno: ItemGrafico[];
  topMaisRecebem: ItemRankingRecebedor[];
  topMenosRecebem: ItemRankingRecebedor[];
}

export interface ItemSaidaDiscriminada {
  itemNome: string;
  categoriaNome: string;
  quantidade: number;
  precoUnitario: number;
  valorEstimado: number;
  localArmazenamento: string;
  recebedorNome: string;
  cpfCnpj: string;
  dataHora: string;
  observacao?: string;
}

export interface SaidasFiltros {
  dataInicio?: string;
  dataFim?: string;
  recebedorId?: number;
  localArmazenamentoId?: number;
  categoriaItemId?: number;
  termoBusca?: string;
  ano?: number;
}

export interface SaidasConsultaParametros extends ParametrosConsulta, SaidasFiltros {}
