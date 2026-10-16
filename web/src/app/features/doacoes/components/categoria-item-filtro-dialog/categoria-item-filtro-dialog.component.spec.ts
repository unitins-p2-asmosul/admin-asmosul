import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CategoriaItemFiltros } from '../../models/categoria-item.model';
import { CategoriaItemFiltroDialogComponent } from './categoria-item-filtro-dialog.component';

describe('CategoriaItemFiltroDialogComponent', () => {
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [CategoriaItemFiltroDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();
  });

  it('aplica filtros com espaços removidos', () => {
    const fixture = TestBed.createComponent(CategoriaItemFiltroDialogComponent);
    fixture.componentInstance['form'].setValue({
      nome: '  Alimentos  ',
      descricao: '  Cestas básicas  ',
    });

    fixture.componentInstance['aplicar']();

    expect(dialogRef.close).toHaveBeenCalledWith({
      nome: 'Alimentos',
      descricao: 'Cestas básicas',
    });
  });

  it('restaura os filtros atuais no formulário', async () => {
    const filtros: CategoriaItemFiltros = {
      nome: 'Alimentos',
      descricao: 'Cestas básicas',
    };
    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: filtros });
    const fixture = TestBed.createComponent(CategoriaItemFiltroDialogComponent);
    await fixture.whenStable();

    expect(fixture.componentInstance['form'].getRawValue()).toEqual(filtros);
  });

  it('limpa os filtros', () => {
    const fixture = TestBed.createComponent(CategoriaItemFiltroDialogComponent);
    fixture.componentInstance['limpar']();

    expect(dialogRef.close).toHaveBeenCalledWith({});
  });
});
