import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import {
  PessoaFormPageComponent,
  cnpjValido,
  cpfValido,
  dataNascimentoValida,
  somenteDigitos,
  telefoneValido,
  cepValido,
} from './pessoa-form-page.component';
import { PessoaService } from '../../services/pessoa.service';
import { CategoriaService } from '../../services/categoria.service';
import { ComorbidadeService } from '../../services/comorbidade.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { TipoPessoa } from '../../models/pessoa.model';
import { FormControl } from '@angular/forms';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('PessoaFormPageComponent', () => {
  let component: PessoaFormPageComponent;
  let fixture: ComponentFixture<PessoaFormPageComponent>;
  let pessoaServiceMock: {
    cadastrar: ReturnType<typeof vi.fn>;
    consultarCep: ReturnType<typeof vi.fn>;
    listarComorbidades: ReturnType<typeof vi.fn>;
    listarCategorias: ReturnType<typeof vi.fn>;
  };
  let categoriaServiceMock: { listarTodas: ReturnType<typeof vi.fn> };
  let comorbidadeServiceMock: { listarTodas: ReturnType<typeof vi.fn> };
  let notificacaoServiceMock: {
    sucesso: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    pessoaServiceMock = {
      cadastrar: vi.fn().mockReturnValue(of({ id: 1 })),
      consultarCep: vi.fn().mockReturnValue(
        of({
          cep: '77001-000',
          logradouro: 'Avenida JK',
          complemento: 'Quadra 104',
          bairro: 'Centro',
          cidade: 'Palmas',
          uf: 'TO',
        }),
      ),
      listarComorbidades: vi.fn().mockReturnValue(of([{ id: 1, nome: 'Diabetes' }])),
      listarCategorias: vi.fn().mockReturnValue(of([{ id: 1, nome: 'Associado' }])),
    };

    categoriaServiceMock = {
      listarTodas: vi.fn().mockReturnValue(of([{ id: 1, nome: 'Associado', ativo: true }])),
    };

    comorbidadeServiceMock = {
      listarTodas: vi.fn().mockReturnValue(of([{ id: 1, nome: 'Diabetes', ativo: true }])),
    };

    notificacaoServiceMock = {
      sucesso: vi.fn(),
      alerta: vi.fn(),
      erro: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PessoaFormPageComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAnimationsAsync('noop'),
        provideRouter([{ path: 'pessoas', component: class DummyComponent {} }]),
        { provide: PessoaService, useValue: pessoaServiceMock },
        { provide: CategoriaService, useValue: categoriaServiceMock },
        { provide: ComorbidadeService, useValue: comorbidadeServiceMock },
        { provide: NotificacaoService, useValue: notificacaoServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PessoaFormPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve inicializar como Pessoa Física por padrão', () => {
    expect(component['ehPessoaJuridica']()).toBe(false);
    expect(component['form'].controls.ehPessoaJuridica.value).toBe(false);
  });

  it('deve carregar categorias e comorbidades ao inicializar', () => {
    expect(categoriaServiceMock.listarTodas).toHaveBeenCalledWith(false);
    expect(comorbidadeServiceMock.listarTodas).toHaveBeenCalledWith(false);
    expect(component['categorias']().length).toBe(1);
    expect(component['comorbidades']().length).toBe(1);
  });

  it('deve alternar para Pessoa Jurídica ao marcar o checkbox', () => {
    component['form'].controls.ehPessoaJuridica.setValue(true);
    expect(component['ehPessoaJuridica']()).toBe(true);

    // Data de nascimento não deve ter erros mesmo vazia para PJ
    expect(component['form'].controls.dataNascimento.validator).toBeNull();

    // CNPJ deve ser obrigatório para PJ
    component['form'].controls.cpfCnpj.setValue('');
    expect(component['form'].controls.cpfCnpj.hasError('required')).toBe(true);
  });

  it('deve voltar para Pessoa Física e restaurar validações ao desmarcar o checkbox', () => {
    component['form'].controls.ehPessoaJuridica.setValue(true);
    component['form'].controls.ehPessoaJuridica.setValue(false);

    expect(component['ehPessoaJuridica']()).toBe(false);

    // Data de nascimento volta a ser obrigatória para PF
    component['form'].controls.dataNascimento.setValue('');
    expect(component['form'].controls.dataNascimento.hasError('required')).toBe(true);
  });

  it('deve preencher dados de endereço automaticamente ao consultar CEP válido', () => {
    component['buscarCep']('77001000');

    expect(pessoaServiceMock.consultarCep).toHaveBeenCalledWith('77001000');
    expect(component['form'].controls.logradouro.value).toBe('Avenida JK');
    expect(component['form'].controls.bairro.value).toBe('Centro');
    expect(component['form'].controls.cidade.value).toBe('Palmas');
    expect(component['form'].controls.uf.value).toBe('TO');
  });

  it('deve ignorar silenciosamente falha na consulta de CEP sem disparar alerta ou erro', () => {
    pessoaServiceMock.consultarCep.mockReturnValue(
      throwError(() => new Error('CEP não encontrado')),
    );

    component['buscarCep']('99999999');

    expect(notificacaoServiceMock.erro).not.toHaveBeenCalled();
    expect(notificacaoServiceMock.alerta).not.toHaveBeenCalled();
    expect(component['buscandoCep']()).toBe(false);
  });

  it('deve submeter formulário com sucesso quando válido para PF', () => {
    component['form'].patchValue({
      nome: 'Maria da Silva',
      cpfCnpj: '52998224725',
      dataNascimento: '19/08/1995',
      telefone: '63999998888',
      email: 'maria@email.com',
      ehBeneficiario: true,
      ehDoador: false,
    });

    component['salvar']();

    expect(pessoaServiceMock.cadastrar).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Maria da Silva',
        cpfCnpj: '52998224725',
        tipoPessoa: TipoPessoa.FISICA,
        dataNascimento: '19-08-1995',
        telefone: '63999998888',
        ehBeneficiario: true,
      }),
    );
    expect(notificacaoServiceMock.sucesso).toHaveBeenCalledWith('Pessoa cadastrada com sucesso!');
  });

  it('deve submeter formulário com sucesso quando válido para PJ com ehBeneficiario false', () => {
    component['form'].controls.ehPessoaJuridica.setValue(true);
    component['form'].patchValue({
      nome: 'Empresa Teste Ltda',
      cpfCnpj: '12345678000195',
      telefone: '63988887777',
      email: 'contato@empresa.com',
      ehDoador: true,
    });

    component['salvar']();

    expect(pessoaServiceMock.cadastrar).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Empresa Teste Ltda',
        cpfCnpj: '12345678000195',
        tipoPessoa: TipoPessoa.JURIDICA,
        telefone: '63988887777',
        ehBeneficiario: false,
        ehDoador: true,
      }),
    );
    expect(notificacaoServiceMock.sucesso).toHaveBeenCalledWith(
      'Pessoa Jurídica cadastrada com sucesso!',
    );
  });

  it('deve evitar requisição duplicada para o mesmo CEP consecutivo', () => {
    component['buscarCep']('77001000');
    expect(pessoaServiceMock.consultarCep).toHaveBeenCalledTimes(1);

    component['buscarCep']('77001000');
    expect(pessoaServiceMock.consultarCep).toHaveBeenCalledTimes(1);
  });

  it('deve marcar erro de conflito no campo email se 409 for de email', () => {
    component['tratarErro'](
      new HttpErrorResponse({
        status: 409,
        error: { detail: 'Já existe uma pessoa cadastrada com este e-mail.' },
      }),
    );

    expect(component['form'].controls.email.hasError('backend')).toBe(true);
    expect(notificacaoServiceMock.alerta).toHaveBeenCalled();
  });

  it('deve marcar erro de conflito no campo cpfCnpj se 409 for de documento', () => {
    component['tratarErro'](
      new HttpErrorResponse({
        status: 409,
        error: { detail: 'Já existe uma pessoa cadastrada com este CPF.' },
      }),
    );

    expect(component['form'].controls.cpfCnpj.hasError('backend')).toBe(true);
    expect(notificacaoServiceMock.alerta).toHaveBeenCalled();
  });

  it('deve reformatar a máscara do documento ao alternar o checkbox de PJ', () => {
    component['form'].controls.cpfCnpj.setValue('12345678000195');
    component['form'].controls.ehPessoaJuridica.setValue(true);

    expect(component['form'].controls.cpfCnpj.value).toBe('12.345.678/0001-95');

    component['form'].controls.ehPessoaJuridica.setValue(false);
    expect(component['form'].controls.cpfCnpj.value).toBe('123.456.780-00');
  });

  it('deve validar limites de quantidadeCoabitantes (min 0 e max 127)', () => {
    const control = component['form'].controls.quantidadeCoabitantes;

    control.setValue(-1);
    expect(control.hasError('min')).toBe(true);

    control.setValue(128);
    expect(control.hasError('max')).toBe(true);

    control.setValue(5);
    expect(control.valid).toBe(true);
  });

  it('deve permitir nova busca de CEP se o campo for apagado e preenchido novamente', () => {
    component['form'].controls.cep.setValue('77001000');
    component['aplicarMascaraCep']();
    expect(pessoaServiceMock.consultarCep).toHaveBeenCalledTimes(1);

    // Apaga o campo
    component['form'].controls.cep.setValue('');
    component['aplicarMascaraCep']();

    // Digita o mesmo CEP novamente
    component['form'].controls.cep.setValue('77001000');
    component['aplicarMascaraCep']();
    expect(pessoaServiceMock.consultarCep).toHaveBeenCalledTimes(2);
  });

  it('deve formatar telefone com 10 dígitos (fixo) e 11 dígitos (celular)', () => {
    component['form'].controls.telefone.setValue('6332181234');
    component['aplicarMascaraTelefone']();
    expect(component['form'].controls.telefone.value).toBe('(63) 3218-1234');

    component['form'].controls.telefone.setValue('63999998888');
    component['aplicarMascaraTelefone']();
    expect(component['form'].controls.telefone.value).toBe('(63) 99999-8888');
  });

  it('não deve enviar atributos exclusivos de PF no payload de PJ', () => {
    component['form'].controls.ehPessoaJuridica.setValue(true);
    component['form'].patchValue({
      nome: 'Empresa Alpha Ltda',
      cpfCnpj: '12345678000195',
      telefone: '6332181234',
    });

    component['salvar']();

    expect(pessoaServiceMock.cadastrar).toHaveBeenCalledWith(
      expect.not.objectContaining({
        dataNascimento: expect.anything(),
        sexo: expect.anything(),
        escolaridade: expect.anything(),
        profissao: expect.anything(),
        rendaFamiliar: expect.anything(),
        comorbidades: expect.anything(),
        quantidadeCoabitantes: expect.anything(),
      }),
    );
  });
});

describe('Funções de Validação', () => {
  it('somenteDigitos deve extrair apenas números', () => {
    expect(somenteDigitos('123.456.789-00')).toBe('12345678900');
    expect(somenteDigitos('(63) 99999-8888')).toBe('63999998888');
    expect(somenteDigitos('')).toBe('');
    expect(somenteDigitos(null)).toBe('');
  });

  it('cpfValido deve validar corretamente CPF', () => {
    expect(cpfValido(new FormControl('52998224725'))).toBeNull();
    expect(cpfValido(new FormControl('11111111111'))).toEqual({ cpfInvalido: true });
    expect(cpfValido(new FormControl('12345'))).toEqual({ cpfIncompleto: true });
    expect(cpfValido(new FormControl(''))).toBeNull();
  });

  it('cnpjValido deve validar corretamente CNPJ', () => {
    expect(cnpjValido(new FormControl('12345678000195'))).toBeNull();
    expect(cnpjValido(new FormControl('11111111111111'))).toEqual({ cnpjInvalido: true });
    expect(cnpjValido(new FormControl('12345'))).toEqual({ cnpjIncompleto: true });
    expect(cnpjValido(new FormControl(''))).toBeNull();
  });

  it('dataNascimentoValida deve validar formato, existência e rejeitar datas futuras', () => {
    expect(dataNascimentoValida(new FormControl('15/05/1990'))).toBeNull();
    expect(dataNascimentoValida(new FormControl('31/02/2020'))).toEqual({ dataInvalida: true });
    expect(dataNascimentoValida(new FormControl('15-05-1990'))).toEqual({ dataFormato: true });
    expect(dataNascimentoValida(new FormControl('01/01/2099'))).toEqual({ dataFutura: true });
    expect(dataNascimentoValida(new FormControl(''))).toBeNull();
  });

  it('telefoneValido deve exigir 10 ou 11 dígitos', () => {
    expect(telefoneValido(new FormControl('63999998888'))).toBeNull();
    expect(telefoneValido(new FormControl('6332181234'))).toBeNull();
    expect(telefoneValido(new FormControl('12345'))).toEqual({ telefoneInvalido: true });
    expect(telefoneValido(new FormControl(''))).toBeNull();
  });

  it('cepValido deve exigir 8 dígitos', () => {
    expect(cepValido(new FormControl('77001000'))).toBeNull();
    expect(cepValido(new FormControl('77001-000'))).toBeNull();
    expect(cepValido(new FormControl('1234'))).toEqual({ cepIncompleto: true });
    expect(cepValido(new FormControl(''))).toBeNull();
  });
});
