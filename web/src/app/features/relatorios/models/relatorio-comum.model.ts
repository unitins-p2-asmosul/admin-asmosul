export interface ItemGrafico {
  rotulo: string;
  quantidade: number;
  percentual?: number;
}

export interface ItemRanking {
  posicao: number;
  id?: number | null;
  nome: string;
  quantidade: number;
  subtitulo?: string;
}
