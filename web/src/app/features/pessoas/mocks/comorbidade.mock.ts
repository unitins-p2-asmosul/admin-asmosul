import { ComorbidadeResumo } from '../models/comorbidade.model';

/** Base simulada. Os cinco primeiros reproduzem os dados do protótipo do Figma. */
export const MOCK_COMORBIDADES: ComorbidadeResumo[] = [
  {
    id: 1,
    nome: 'Hipertensão Arterial',
    descricao: 'Condição caracterizada pela elevação persistente da pressão arterial.',
    ativo: true,
  },
  {
    id: 2,
    nome: 'Diabetes',
    descricao:
      'Condição que afeta a forma como o organismo controla os níveis de glicose no sangue.',
    ativo: true,
  },
  {
    id: 3,
    nome: 'Asma',
    descricao: 'Doença respiratória que pode causar dificuldade para respirar, chiado e tosse.',
    ativo: true,
  },
  {
    id: 4,
    nome: 'Obesidade',
    descricao: 'Condição caracterizada pelo excesso de gordura corporal que pode afetar a saúde.',
    ativo: true,
  },
  {
    id: 5,
    nome: 'Doença Cardíaca',
    descricao: 'Grupo de condições que afetam o funcionamento e a saúde do coração.',
    ativo: true,
  },
  {
    id: 6,
    nome: 'Alzheimer',
    descricao: 'Doença neurodegenerativa progressiva que afeta memória e cognição.',
    ativo: true,
  },
  {
    id: 7,
    nome: 'Depressão',
    descricao: 'Transtorno de humor com tristeza persistente e perda de interesse.',
    ativo: false,
  },
  {
    id: 8,
    nome: 'Artrite Reumatoide',
    descricao: 'Doença autoimune que causa inflamação crônica das articulações.',
    ativo: false,
  },
];
