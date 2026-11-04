import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { CategoriaItemResumo } from '../../models/categoria-item.model';
import { CategoriaItemTabelaComponent } from './categoria-item-tabela.component';

const CATEGORIAS_EXEMPLO: CategoriaItemResumo[] = [
  {
    id: 1,
    nome: 'Alimentos',
    descricao: 'Gêneros alimentícios',
    itensPertencentes: 14,
    ativo: true,
  },
];

describe('CategoriaItemTabelaComponent', () => {
  let fixture: ComponentFixture<CategoriaItemTabelaComponent>;
  let component: CategoriaItemTabelaComponent;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [CategoriaItemTabelaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoriaItemTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', CATEGORIAS_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 1);
    fixture.componentRef.setInput('tamanhoPagina', 10);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('inclui a coluna Itens Pertencentes nas colunas exibidas e exportáveis', () => {
    expect(component['colunasExibidas']).toEqual([
      'indice',
      'acoes',
      'nome',
      'descricao',
      'itensPertencentes',
      'status',
    ]);
    expect(component.colunasExportaveis.map((c) => c.id)).toContain('itensPertencentes');

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Itens Pertencentes');
    expect(texto).toContain('14');
  });
});
