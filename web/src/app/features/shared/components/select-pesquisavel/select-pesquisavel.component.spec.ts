import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OpcaoSelectPesquisavel, SelectPesquisavelComponent } from './select-pesquisavel.component';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, SelectPesquisavelComponent],
  template: `
    <form [formGroup]="form">
      <app-select-pesquisavel
        formControlName="item"
        [opcoes]="opcoes"
        [carregando]="carregando"
        [permitirLimpar]="permitirLimpar"
        [placeholder]="placeholder"
        [mensagemVazio]="mensagemVazio"
        [desabilitado]="desabilitado"
        [mensagemErro]="mensagemErro()"
      />
    </form>
  `,
})
class HostTestComponent {
  form = new FormGroup({
    item: new FormControl<number | null>(null, [Validators.required]),
  });

  opcoes: OpcaoSelectPesquisavel<number>[] = [
    { valor: 1, rotulo: 'Arroz 5kg', subtexto: 'Alimentos' },
    { valor: 2, rotulo: 'Feijão Carioca 1kg', subtexto: 'Alimentos' },
    { valor: 3, rotulo: 'Maria Silva', subtexto: '123.456.789-00' },
  ];

  carregando = false;
  permitirLimpar = true;
  placeholder = 'Selecione...';
  mensagemVazio = 'Nenhum resultado encontrado';
  desabilitado = false;
  readonly mensagemErro = signal<string | null>(null);
}

describe('SelectPesquisavelComponent', () => {
  let fixture: ComponentFixture<HostTestComponent>;
  let host: HostTestComponent;
  let selectComponent: SelectPesquisavelComponent<number>;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    vi.useFakeTimers();

    await TestBed.configureTestingModule({
      imports: [HostTestComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HostTestComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();

    selectComponent = fixture.debugElement.children[0].children[0]
      .componentInstance as SelectPesquisavelComponent<number>;
  });

  afterEach(() => {
    vi.useRealTimers();
    TestBed.resetTestingModule();
  });

  it('deve criar o componente', () => {
    expect(selectComponent).toBeTruthy();
  });

  it('deve sincronizar valor recebido via formControl writeValue', () => {
    host.form.controls.item.setValue(2);
    fixture.detectChanges();

    expect(selectComponent['valorSelecionado']()).toBe(2);
    expect(selectComponent['buscaControl'].value).toBe('Feijão Carioca 1kg');
  });

  it('deve filtrar opções com debounce de 300ms ao digitar', () => {
    let eventoPesquisa = '';
    selectComponent.aoPesquisar.subscribe((termo) => (eventoPesquisa = termo));

    selectComponent['buscaControl'].setValue('arroz');
    expect(selectComponent['termoBusca']()).toBe('');

    vi.advanceTimersByTime(150);
    expect(selectComponent['termoBusca']()).toBe('');

    vi.advanceTimersByTime(150);
    expect(selectComponent['termoBusca']()).toBe('arroz');
    expect(eventoPesquisa).toBe('arroz');
    expect(selectComponent['opcoesFiltradas']().length).toBe(1);
    expect(selectComponent['opcoesFiltradas']()[0].valor).toBe(1);
  });

  it('deve encontrar opção por CPF sem pontuação', () => {
    selectComponent['buscaControl'].setValue('123456');
    vi.advanceTimersByTime(300);

    expect(selectComponent['opcoesFiltradas']().length).toBe(1);
    expect(selectComponent['opcoesFiltradas']()[0].rotulo).toBe('Maria Silva');
  });

  it('deve selecionar opção e atualizar o formulário', () => {
    let selecionado: OpcaoSelectPesquisavel<number> | null = null;
    selectComponent.aoSelecionar.subscribe(
      (op) => (selecionado = op as OpcaoSelectPesquisavel<number>),
    );

    selectComponent['onOptionSelected']({
      option: { value: 1 },
    } as unknown as MatAutocompleteSelectedEvent);
    fixture.detectChanges();

    expect(host.form.controls.item.value).toBe(1);
    expect((selecionado as OpcaoSelectPesquisavel<number> | null)?.rotulo).toBe('Arroz 5kg');
    expect(selectComponent['buscaControl'].value).toBe('Arroz 5kg');
  });

  it('deve limpar seleção ao chamar limpar()', () => {
    host.form.controls.item.setValue(1);
    fixture.detectChanges();

    let limpou = false;
    selectComponent.aoLimpar.subscribe(() => (limpou = true));

    selectComponent['limpar']();
    fixture.detectChanges();

    expect(host.form.controls.item.value).toBeNull();
    expect(selectComponent['valorSelecionado']()).toBeNull();
    expect(selectComponent['buscaControl'].value).toBe('');
    expect(limpou).toBe(true);
  });

  it('deve exibir lista vazia quando termo não corresponder', () => {
    selectComponent['buscaControl'].setValue('palavra-inexistente');
    vi.advanceTimersByTime(300);
    fixture.detectChanges();

    expect(selectComponent['opcoesFiltradas']().length).toBe(0);
    expect(selectComponent['opcoesFiltradas']()).toEqual([]);
  });

  it('deve desabilitar buscaControl quando formControl for desabilitado', () => {
    host.form.controls.item.disable();
    fixture.detectChanges();

    expect(selectComponent['desabilitadoEfetivo']()).toBe(true);
    expect(selectComponent['buscaControl'].disabled).toBe(true);

    host.form.controls.item.enable();
    fixture.detectChanges();

    expect(selectComponent['desabilitadoEfetivo']()).toBe(false);
    expect(selectComponent['buscaControl'].enabled).toBe(true);
  });

  it('deve restaurar rótulo da opção selecionada no blur se texto digitado for inválido', () => {
    host.form.controls.item.setValue(1);
    fixture.detectChanges();

    selectComponent['emFoco'].set(true);
    selectComponent['buscaControl'].setValue('texto qualquer');
    selectComponent['onBlur']();

    expect(selectComponent['buscaControl'].value).toBe('Arroz 5kg');
    expect(host.form.controls.item.value).toBe(1);
  });

  it('deve exibir mensagem de erro customizada quando informada', () => {
    host.mensagemErro.set('Erro customizado');
    fixture.detectChanges();

    expect(selectComponent['erroExibido']).toBe('Erro customizado');
  });

  it('deve exibir erro quando campo obrigatório for tocado e inválido', () => {
    host.form.controls.item.markAsTouched();
    fixture.detectChanges();

    expect(selectComponent['erroExibido']).toBe('Campo obrigatório.');
  });
});
