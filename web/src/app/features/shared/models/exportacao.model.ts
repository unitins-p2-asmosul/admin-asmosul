export type FormatoExportacao = 'xlsx' | 'csv';

export type ValorExportacao = string | number | boolean | null | undefined;

export interface ColunaExportacao<T> {
  id: string;
  titulo: string;
  valor: (registro: T) => ValorExportacao;
}

export interface SolicitacaoExportacao<T> {
  formato: FormatoExportacao;
  colunas: ColunaExportacao<T>[];
}
