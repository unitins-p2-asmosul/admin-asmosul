import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { formatarPrecoUnitario, ItemResumo } from '../../models/item.model';
import { UnidadeMedidaCodigo } from '../../models/unidade-medida.model';
import { ItemTabelaComponent } from './item-tabela.component';

const ITENS_EXEMPLO: ItemResumo[] = [
  {
    id: 1,
    nome: 'Arroz 5kg',
    categoria: { id: 10, nome: 'Alimentos', ativo: true },
    estoque: 150,
    precoUnitario: 2500,
    unidadeMedida: UnidadeMedidaCodigo.PACOTE,
    descricao: 'Arroz agulhinha',
    ativo: true,
  },
  {
    id: 2,
    nome: 'Feijão 1kg',
    categoria: { id: 10, nome: 'Alimentos', ativo: true },
    estoque: 0,
    precoUnitario: 800,
    unidadeMedida: UnidadeMedidaCodigo.UNIDADE,
    descricao: 'Feijão preto',
    ativo: false,
  },
];

describe('ItemTabelaComponent', () => {
  let fixture: ComponentFixture<ItemTabelaComponent>;
  let component: ItemTabelaComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ItemTabelaComponent] }).compileComponents();
    fixture = TestBed.createComponent(ItemTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', ITENS_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 2);
    fixture.componentRef.setInput('tamanhoPagina', 10);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('exibe as colunas requeridas com formatação correta de moeda e unidade', () => {
    expect(component['colunasExibidas']).toEqual([
      'indice',
      'acoes',
      'nome',
      'categoria',
      'estoque',
      'precoUnitario',
      'unidadeMedida',
      'descricao',
      'status',
    ]);

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Arroz 5kg');
    expect(texto).toContain('Alimentos');
    expect(texto).toContain('150 pct');
    expect(texto).toContain(formatarPrecoUnitario(2500));
    expect(texto).toContain(formatarPrecoUnitario(800));
    expect(texto).toContain('Ativo');
    expect(texto).toContain('Inativo');
  });

  it('emite evento ao clicar nos botões de ação', () => {
    const spyVisualizar = vi.spyOn(component.aoVisualizar, 'emit');
    const spyEditar = vi.spyOn(component.aoEditar, 'emit');
    const spyAlternarEstado = vi.spyOn(component.aoAlternarEstado, 'emit');

    const botoes = fixture.nativeElement.querySelectorAll('td button');
    botoes[0].click();
    expect(spyVisualizar).toHaveBeenCalledWith(1);

    botoes[1].click();
    expect(spyEditar).toHaveBeenCalledWith(1);

    botoes[2].click();
    expect(spyAlternarEstado).toHaveBeenCalledWith({ id: 1, ativo: false });
  });

  it('emite evento ao mudar de página', () => {
    fixture.componentRef.setInput('totalElementos', 30);
    fixture.detectChanges();

    const spyMudarPagina = vi.spyOn(component.aoMudarPagina, 'emit');
    component['mudarPagina'](1);
    expect(spyMudarPagina).toHaveBeenCalledWith(
      expect.objectContaining({ pageIndex: 1, pageSize: 10 }),
    );
  });
});
