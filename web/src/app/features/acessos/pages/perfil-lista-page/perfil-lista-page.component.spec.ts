import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ContaService } from '../../services/conta.service';
import { PerfilListaPageComponent } from './perfil-lista-page.component';

describe('PerfilListaPageComponent', () => {
  let fixture: ComponentFixture<PerfilListaPageComponent>;
  let contaServiceMock: { listarPerfis: ReturnType<typeof vi.fn> };
  let locationMock: { back: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    contaServiceMock = { listarPerfis: vi.fn().mockReturnValue(of([])) };
    locationMock = { back: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [PerfilListaPageComponent],
      providers: [
        { provide: ContaService, useValue: contaServiceMock },
        { provide: Location, useValue: locationMock },
      ],
    }).compileComponents();

  });

  it('mostra os cinco perfis estáticos quando o endpoint não retorna opções', () => {
    fixture = TestBed.createComponent(PerfilListaPageComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const linhas = fixture.nativeElement.querySelectorAll('tbody tr');

    expect(contaServiceMock.listarPerfis).toHaveBeenCalledOnce();
    expect(component['perfis']()).toHaveLength(5);
    expect(linhas).toHaveLength(5);
    expect(fixture.nativeElement.textContent).toContain('Gerenciamento de Perfis');
    expect(fixture.nativeElement.textContent).toContain('Gerenciador de Pessoas');
    expect(fixture.nativeElement.textContent).toContain('Possui acesso a todo o módulo de pessoas');
  });

  it('usa os nomes do serviço mantendo descrições formais da lista local', () => {
    contaServiceMock.listarPerfis.mockReturnValue(of([
      { codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gestão de Pessoas (API)' },
      { codigo: 'GERENCIADOR_DOACOES', descricao: 'Gestão de Doações (API)' },
    ]));
    fixture = TestBed.createComponent(PerfilListaPageComponent);
    fixture.detectChanges();

    const perfis = fixture.componentInstance['perfis']();
    expect(perfis).toHaveLength(5);
    expect(perfis[0].nome).toBe('Gestão de Pessoas (API)');
    expect(perfis[0].descricao).toContain('módulo de pessoas');
    expect(perfis[2].nome).toBe('Gerenciador de Capacitações');
  });
});