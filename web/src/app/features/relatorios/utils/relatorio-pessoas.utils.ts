import {
  ESCOLARIDADE_OPCOES,
  RENDA_FAMILIAR_OPCOES,
  SEXO_OPCOES,
} from '@features/pessoas/models/pessoa.model';
import { ItemGrafico } from '../models/relatorio-comum.model';
import { CriterioAgrupamentoPessoa } from '../models/relatorio-pessoas.model';

export function traduzirRotuloDemografico(
  rotulo: string,
  criterio: CriterioAgrupamentoPessoa,
): string {
  if (!rotulo || rotulo === 'NAO_INFORMADO') {
    return 'Não informado';
  }

  switch (criterio) {
    case 'SEXO':
      return SEXO_OPCOES.find((op) => op.codigo === rotulo)?.descricao ?? rotulo;
    case 'ESCOLARIDADE':
      return ESCOLARIDADE_OPCOES.find((op) => op.codigo === rotulo)?.descricao ?? rotulo;
    case 'FAIXA_RENDA':
      return RENDA_FAMILIAR_OPCOES.find((op) => op.codigo === rotulo)?.descricao ?? rotulo;
    default:
      return rotulo;
  }
}

export function formatarDistribuicaoDemografica(
  itens: ItemGrafico[],
  criterio: CriterioAgrupamentoPessoa,
): ItemGrafico[] {
  return itens.map((item) => ({
    ...item,
    rotulo: traduzirRotuloDemografico(item.rotulo, criterio),
  }));
}
