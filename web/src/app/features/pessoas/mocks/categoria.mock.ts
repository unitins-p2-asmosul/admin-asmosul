import { CategoriaResumo } from '@features/pessoas/models/categoria.model';

export const MOCK_CATEGORIAS: CategoriaResumo[] = [
  {
    id: 1,
    nome: 'Associado',
    descricao: 'Pessoa associada à ASMOSUL.',
    pessoasPertencentes: 0,
    ativo: true,
  },
  {
    id: 2,
    nome: 'Educação',
    descricao: 'Abrange necessidades relacionadas ao acesso à educação.',
    pessoasPertencentes: 0,
    ativo: true,
  },
];
