import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SelectPesquisavelComponent, SelectPesquisavelOpcao } from './select-pesquisavel.component';

describe('SelectPesquisavelComponent', () => {
  let fixture: ComponentFixture<SelectPesquisavelComponent<number>>;
  let component: SelectPesquisavelComponent<number>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectPesquisavelComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectPesquisavelComponent<number>);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('emite a consulta somente após 300 ms sem digitação', async () => {
    const consultas: string[] = [];
    component.pesquisar.subscribe((consulta) => consultas.push(consulta));

    vi.useFakeTimers();
    component['textoControl'].setValue('ar');
    await vi.advanceTimersByTimeAsync(200);
    component['textoControl'].setValue('arroz');
    expect(component['textoControl'].value).toBe('arroz');
    await vi.advanceTimersByTimeAsync(299);
    expect(consultas).toEqual([]);

    await vi.advanceTimersByTimeAsync(1);
    expect(consultas).toEqual(['arroz']);
    vi.useRealTimers();
  });

  it('filtra as opções pela etiqueta e pelo texto de busca adicional', () => {
    const opcoes: SelectPesquisavelOpcao<number>[] = [
      { valor: 1, rotulo: 'Ana Silva · CPF 123', textoBusca: '123' },
      { valor: 2, rotulo: 'Bruno Souza · CPF 456', textoBusca: '456' },
    ];
    fixture.componentRef.setInput('opcoes', opcoes);
    component['textoControl'].setValue('456');
    fixture.detectChanges();

    expect(component['opcoesFiltradas']().map((opcao) => opcao.valor)).toEqual([2]);
  });

  it('propaga a opção selecionada pelo ControlValueAccessor', () => {
    const alterar = vi.fn();
    const opcao = { valor: 7, rotulo: 'Item de teste' };
    component.registerOnChange(alterar);

    component['selecionar']({ option: { value: opcao } } as never);

    expect(alterar).toHaveBeenCalledWith(7);
    expect(component['valorAtual']()).toBe(7);
  });
});
