export interface ErroCampo {
  campo: string;
  mensagem: string;
}

export interface ErroApi {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  erros?: ErroCampo[];
}
