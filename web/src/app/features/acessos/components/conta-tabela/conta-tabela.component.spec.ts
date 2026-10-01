import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ContaResumo } from '../../models/conta.model';
import { ContaTabelaComponent } from './conta-tabela.component';

const CONTAS: ContaResumo[] = [
  {
    id: 4,
    nomePessoa: 'Ana Silva',
    email: 'ana@example.com',
    nomeUsuario: 'ana.silva',
    perfis: [{ codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' }],
    redefinirSenha: true,
    ativo: true,
    dataCriacao: '14-05-2026',
  },
  {
    id: 5,
    nomePessoa: 'Bruno Souza',
    email: 'bruno@example.com',
    nomeUsuario: 'bruno.souza',
    perfis: [{ codigo: 'GERENCIADOR_ACESSO', descricao: 'Gerenciador de Acesso' }],
    redefinirSenha: false,
    ativo: false,
    dataCriacao: '15-05-2026',
  },
];

describe('ContaTabelaComponent', () => {
  let fixture: ComponentFixture<ContaTabelaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ContaTabelaComponent] }).compileComponents();
    fixture = TestBed.createComponent(ContaTabelaComponent);
    fixture.componentRef.setInput('dados', CONTAS);
    fixture.componentRef.setInput('totalElementos', 2);
    fixture.componentRef.setInput('tamanhoPagina', 1);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('exibe as colunas na ordem definida e formata as datas e flags', () => {
    const component = fixture.componentInstance;
    const textosCabecalho = Array.from(
      fixture.nativeElement.querySelectorAll('th'),
      (cabecalho: HTMLElement) => cabecalho.textContent?.trim().replace(/\s+/g, ' '),
    );

    expect(component['colunasExibidas']).toEqual([
      'indice',
      'acoes',
      'nomePessoa',
      'nomeUsuario',
      'email',
      'perfis',
      'dataCriacao',
      'dataInativo',
      'status',
    ]);
    expect(textosCabecalho).toEqual([
      '#',
      'Ações',
      'Nome da PessoaAssociada',
      'Nome deUsuário',
      'E-mail da PessoaAssociada',
      'Perfis',
      'DataCriação',
      'DataInativo',
      'Status',
    ]);
    expect(fixture.nativeElement.textContent).toContain('14/05/2026');
    expect(fixture.nativeElement.textContent).toContain('Senha pendente');
    expect(fixture.nativeElement.textContent).toContain('Sem pendência');
  });

  it('emite as ações de visualizar, editar, redefinir senha e desativar', () => {
    const component = fixture.componentInstance;
    const visualizar = vi.fn();
    const editar = vi.fn();
    const redefinirSenha = vi.fn();
    const alternarEstado = vi.fn();
    component.aoVisualizar.subscribe(visualizar);
    component.aoEditar.subscribe(editar);
    component.aoRedefinirSenha.subscribe(redefinirSenha);
    component.aoAlternarEstado.subscribe(alternarEstado);

    const linhaAtiva = fixture.nativeElement.querySelector('tr.mat-mdc-row');
    const botoes = linhaAtiva.querySelectorAll('td.mat-column-acoes button');
    botoes[0].click();
    botoes[1].click();
    botoes[2].click();
    botoes[3].click();

    expect(visualizar).toHaveBeenCalledWith(4);
    expect(editar).toHaveBeenCalledWith(4);
    expect(redefinirSenha).toHaveBeenCalledWith(CONTAS[0]);
    expect(alternarEstado).toHaveBeenCalledWith({ id: 4, ativo: false, nomePessoa: 'Ana Silva' });
  });

  it('oferece reativação para contas inativas e emite mudança de página', () => {
    const component = fixture.componentInstance;
    const alternarEstado = vi.fn();
    const mudarPagina = vi.fn();
    component.aoAlternarEstado.subscribe(alternarEstado);
    component.aoMudarPagina.subscribe(mudarPagina);

    fixture.componentRef.setInput('paginaAtual', 1);
    fixture.detectChanges();
    const linhaInativa = fixture.nativeElement.querySelectorAll('tr.mat-mdc-row')[1];
    linhaInativa.querySelector('button[mattooltip="Reativar"]').click();
    fixture.nativeElement.querySelector('button[aria-label="Página anterior"]').click();

    expect(alternarEstado).toHaveBeenCalledWith({ id: 5, ativo: true, nomePessoa: 'Bruno Souza' });
    expect(mudarPagina).toHaveBeenCalledWith(expect.objectContaining({
      pageIndex: 0,
      pageSize: 1,
      previousPageIndex: 1,
    }));
  });
});