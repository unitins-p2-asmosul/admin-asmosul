import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { CategoriaResumo } from '../../models/categoria.model';
import { CategoriaTabelaComponent } from './categoria-tabela.component';

const CATEGORIAS_PESSOAS_EXEMPLO: CategoriaResumo[] = [
  {
    id: 1,
    nome: 'Associado',
    descricao: 'Membro ativo',
    pessoasPertencentes: 23,
    ativo: true,
  },
];

describe('CategoriaTabelaComponent', () => {
  let fixture: ComponentFixture<CategoriaTabelaComponent>;
  let component: CategoriaTabelaComponent;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [CategoriaTabelaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoriaTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', CATEGORIAS_PESSOAS_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 1);
    fixture.componentRef.setInput('tamanhoPagina', 20);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('inclui a coluna Pessoas Pertencentes nas colunas exibidas e exportáveis', () => {
    expect(component['colunasExibidas']).toEqual([
      'id',
      'acoes',
      'nome',
      'descricao',
      'pessoasPertencentes',
      'status',
    ]);
    expect(component.colunasExportaveis.map((c) => c.id)).toContain('pessoasPertencentes');

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Pessoas Pertencentes');
    expect(texto).toContain('23');
  });
});
