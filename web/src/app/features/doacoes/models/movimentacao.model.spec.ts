import { describe, expect, it } from 'vitest';
import { formatarDataHoraMovimentacao } from './movimentacao.model';

describe('formatarDataHoraMovimentacao', () => {
  it('formata datas no padrão recebido pelo endpoint', () => {
    expect(formatarDataHoraMovimentacao('20-10-2026 08:30:45')).toBe('20/10/2026 08:30');
  });

  it('formata datas ISO', () => {
    expect(formatarDataHoraMovimentacao('2026-10-20T08:30:45')).toBe('20/10/2026 08:30');
  });
});
