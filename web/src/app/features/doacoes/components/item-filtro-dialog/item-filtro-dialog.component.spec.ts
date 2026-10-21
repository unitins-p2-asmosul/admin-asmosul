import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemFiltros } from '../../models/item.model';
import { CategoriaItemService } from '../../services/categoria-item.service';
import { ItemFiltroDialogComponent } from './item-filtro-dialog.component';

describe('ItemFiltroDialogComponent', () => {
  let dialogRef: { close: ReturnType<typeof vi.fn> };
  let categoriaItemServiceMock: { listar: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };
    categoriaItemServiceMock = {
      listar: vi
        .fn()
        .mockReturnValue(of({ dados: [], totalElementos: 0, paginaAtual: 0, tamanhoPagina: 100 })),
    };

    await TestBed.configureTestingModule({
      imports: [ItemFiltroDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: CategoriaItemService, useValue: categoriaItemServiceMock },
      ],
    }).compileComponents();
  });

  it('aplica filtros com dados sanitizados e conversão de valores monetários para centavos', () => {
    const fixture = TestBed.createComponent(ItemFiltroDialogComponent);
    fixture.componentInstance['form'].setValue({
      nome: '  Arroz  ',
      categoriasIds: [1, 2],
      estoque: 10,
      precoUnitarioMinimo: 5.5,
      precoUnitarioMaximo: 20,
    });

    fixture.componentInstance['aplicar']();

    expect(dialogRef.close).toHaveBeenCalledWith({
      nome: 'Arroz',
      categoriasIds: [1, 2],
      estoque: 10,
      precoUnitarioMinimo: 550,
      precoUnitarioMaximo: 2000,
    });
  });

  it('restaura os filtros recebidos no formulário', async () => {
    const filtros: ItemFiltros = {
      nome: 'Arroz',
      categoriasIds: [1],
      estoque: 50,
      precoUnitarioMinimo: 1000,
      precoUnitarioMaximo: 3000,
    };
    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: filtros });
    const fixture = TestBed.createComponent(ItemFiltroDialogComponent);
    await fixture.whenStable();

    const valores = fixture.componentInstance['form'].getRawValue();
    expect(valores.nome).toBe('Arroz');
    expect(valores.categoriasIds).toEqual([1]);
    expect(valores.estoque).toBe(50);
    expect(valores.precoUnitarioMinimo).toBe(10);
    expect(valores.precoUnitarioMaximo).toBe(30);
  });

  it('limpa os filtros', () => {
    const fixture = TestBed.createComponent(ItemFiltroDialogComponent);
    fixture.componentInstance['limpar']();

    expect(dialogRef.close).toHaveBeenCalledWith({});
  });
});
