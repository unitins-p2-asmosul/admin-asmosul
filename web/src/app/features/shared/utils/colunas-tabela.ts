export function filtrarColunasExibidas(
  colunasExibidas: readonly string[],
  idsColunasExportaveis: readonly string[],
  idsColunasVisiveis: readonly string[] | null,
): string[] {
  if (idsColunasVisiveis === null) {
    return [...colunasExibidas];
  }

  return colunasExibidas.filter(
    (id) =>
      id === 'acoes' ||
      !idsColunasExportaveis.includes(id) ||
      idsColunasVisiveis.includes(id),
  );
}
