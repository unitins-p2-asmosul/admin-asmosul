import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ContaFiltros, PerfilDisponivel } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { ContaFiltroDialogComponent } from './conta-filtro-dialog.component';

describe('ContaFiltroDialogComponent', () => {
  let contaServiceMock: { listarPerfis: ReturnType<typeof vi.fn> };
  let dialogRefMock: { close: ReturnType<typeof vi.fn> };
  let perfis: PerfilDisponivel[];

  beforeEach(async () => {
    perfis = [{ codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' }];
    contaServiceMock = { listarPerfis: vi.fn().mockReturnValue(of(perfis)) };
    dialogRefMock = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ContaFiltroDialogComponent],
      providers: [
        { provide: ContaService, useValue: contaServiceMock },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: dialogRefMock },
      ],
    }).compileComponents();
  });

  it('carrega opções de perfis do serviço', () => {
    const fixture = TestBed.createComponent(ContaFiltroDialogComponent);
    fixture.detectChanges();

    expect(contaServiceMock.listarPerfis).toHaveBeenCalledOnce();
    expect(fixture.componentInstance['perfis']()).toEqual(perfis);
  });

  it('aplica filtros com textos aparados e datas no formato da API', () => {
    const fixture = TestBed.createComponent(ContaFiltroDialogComponent);
    const component = fixture.componentInstance;
    component['form'].setValue({
      nomePessoa: '  Ana Silva  ',
      nomeUsuario: ' ana.silva ',
      email: ' ana@example.com ',
      perfis: ['GERENCIADOR_PESSOAS'],
      redefinirSenha: 'true',
      dataCriacao: '2026-05-14',
      dataInativo: '2026-06-03',
    });

    component['aplicar']();

    expect(dialogRefMock.close).toHaveBeenCalledWith({
      nomePessoa: 'Ana Silva',
      nomeUsuario: 'ana.silva',
      email: 'ana@example.com',
      perfis: ['GERENCIADOR_PESSOAS'],
      redefinirSenha: true,
      dataCriacao: '14-05-2026',
      dataInativo: '03-06-2026',
    });
  });

  it('limpa todos os filtros ao fechar com objeto vazio', () => {
    const fixture = TestBed.createComponent(ContaFiltroDialogComponent);
    fixture.componentInstance['limpar']();

    expect(dialogRefMock.close).toHaveBeenCalledWith({});
  });

  it('converte os filtros atuais para os controles do diálogo', async () => {
    const filtros: ContaFiltros = {
      nomePessoa: 'Ana',
      perfis: ['GERENCIADOR_PESSOAS'],
      redefinirSenha: false,
      dataCriacao: '14-05-2026',
    };
    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: filtros });
    const fixture = TestBed.createComponent(ContaFiltroDialogComponent);
    await fixture.whenStable();

    expect(fixture.componentInstance['form'].getRawValue()).toMatchObject({
      nomePessoa: 'Ana',
      perfis: ['GERENCIADOR_PESSOAS'],
      redefinirSenha: 'false',
      dataCriacao: '2026-05-14',
    });
  });
});