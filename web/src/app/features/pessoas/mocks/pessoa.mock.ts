import {
  EscolaridadeCodigo,
  ItemRelacionadoResumo,
  PessoaDetalhe,
  RendaFamiliarCodigo,
  SexoCodigo,
  TipoPessoa,
  UfCodigo,
} from '../models/pessoa.model';

/** Base simulada de pessoas. Usada para validar o retorno 201 e o conflito de CPF (409). */
export const MOCK_PESSOAS: PessoaDetalhe[] = [
  {
    id: 1,
    nome: 'Maria Silva',
    cpfCnpj: '52998224725',
    tipoPessoa: TipoPessoa.FISICA,
    dataNascimento: '19-08-1995',
    sexo: { codigo: SexoCodigo.FEMININO, descricao: 'Feminino' },
    telefone: '63999998888',
    email: 'maria.silva@email.com',
    escolaridade: {
      codigo: EscolaridadeCodigo.SUPERIOR_COMPLETO,
      descricao: 'Superior completo',
    },
    profissao: 'Assistente administrativa',
    rendaFamiliar: {
      codigo: RendaFamiliarCodigo.ENTRE_DOIS_MIL_E_TRES_MIL,
      descricao: 'Entre R$ 2.000 e R$ 3.000',
    },
    comorbidades: [1, 2],
    categorias: [1],
    descricao: 'Pessoa cadastrada para acompanhamento da ASMOSUL.',
    cep: '77001000',
    uf: UfCodigo.TO,
    cidade: 'Palmas',
    bairro: 'Plano Diretor Sul',
    logradouro: 'Avenida JK',
    complementoEndereco: 'Quadra 104 Sul',
    quantidadeCoabitantes: 2,
    ehBeneficiario: true,
    ehDoador: false,
    ativo: true,
  },
];

/**
 * Listas temporárias para os selects múltiplos do formulário.
 */
export const MOCK_COMORBIDADES: ItemRelacionadoResumo[] = [
  { id: 1, nome: 'Hipertensão' },
  { id: 2, nome: 'Diabetes' },
  { id: 3, nome: 'Asma' },
  { id: 4, nome: 'Cardiopatia' },
  { id: 5, nome: 'Obesidade' },
];

export const MOCK_CATEGORIAS: ItemRelacionadoResumo[] = [
  { id: 1, nome: 'Associado' },
  { id: 2, nome: 'Dependente' },
  { id: 3, nome: 'Voluntário' },
  { id: 4, nome: 'Colaborador' },
];
