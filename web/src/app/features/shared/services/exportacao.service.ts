import { inject, Injectable } from '@angular/core';
import type { Borders } from 'exceljs';
import { Observable, take } from 'rxjs';
import {
  ColunaExportacao,
  FormatoExportacao,
  SolicitacaoExportacao,
} from '../models/exportacao.model';
import { NotificacaoService } from './notificacao.service';

const MIME_XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
const MIME_CSV = 'text/csv;charset=utf-8';
const COR_CABECALHO = 'FF123B55';
const CORES_LINHAS = ['FFF1F3F5', 'FFE4E8EC'];

@Injectable({ providedIn: 'root' })
export class ExportacaoService {
  private readonly notificacao = inject(NotificacaoService);

  exportarDados<T>(
    dados$: Observable<T[]>,
    solicitacao: SolicitacaoExportacao<T>,
    nomeArquivo: string,
  ): void {
    dados$.pipe(take(1)).subscribe({
      next: (dados) => {
        if (solicitacao.formato === 'csv') {
          this.exportarCSV(dados, solicitacao.colunas, nomeArquivo);
        } else {
          void this.exportarXLSX(dados, solicitacao.colunas, nomeArquivo);
        }
      },
      error: (erro) => {
        console.error('Não foi possível carregar os dados para exportação.', erro);
        this.notificacao.erro('Não foi possível carregar os dados para exportação.');
      },
    });
  }

  async exportarXLSX<T>(
    dados: T[],
    colunas: ColunaExportacao<T>[],
    nomeArquivo: string,
  ): Promise<void> {
    if (!this.validarColunas(colunas)) return;

    try {
      const exceljs = await import('exceljs');
      const planilha = new exceljs.Workbook().addWorksheet('Dados');
      planilha.addRow([`Data de exportação: ${new Date().toLocaleString('pt-BR')}`]);
      if (colunas.length > 1) planilha.mergeCells(1, 1, 1, colunas.length);
      planilha.getRow(1).font = { bold: true, color: { argb: COR_CABECALHO } };
      planilha.getRow(1).height = 24;
      planilha.getRow(1).alignment = { vertical: 'middle' };

      const cabecalho = planilha.addRow(colunas.map((coluna) => coluna.titulo));
      cabecalho.height = 32;
      cabecalho.eachCell((celula) => {
        celula.fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: COR_CABECALHO },
        };
        celula.font = { bold: true, color: { argb: 'FFFFFFFF' } };
        celula.alignment = {
          vertical: 'middle',
          horizontal: 'center',
          wrapText: true,
          indent: 1,
        };
        celula.border = this.obterBordas();
      });

      dados.forEach((registro, indice) => {
        const linha = planilha.addRow(colunas.map((coluna) => this.obterValor(coluna, registro)));
        linha.height = 24;
        linha.eachCell((celula) => {
          celula.fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: CORES_LINHAS[indice % CORES_LINHAS.length] },
          };
          celula.alignment = { vertical: 'middle', wrapText: true, indent: 1 };
          celula.border = this.obterBordas();
        });
      });

      planilha.columns = colunas.map((coluna) => ({
        width: Math.min(40, Math.max(16, coluna.titulo.length + 4)),
      }));

      const conteudo = await planilha.workbook.xlsx.writeBuffer();
      this.baixar(new Blob([new Uint8Array(conteudo)], { type: MIME_XLSX }), nomeArquivo, 'xlsx');
    } catch (erro) {
      console.error('Não foi possível gerar o arquivo XLSX.', erro);
      this.notificacao.erro('Não foi possível gerar o arquivo XLSX.');
    }
  }

  exportarCSV<T>(dados: T[], colunas: ColunaExportacao<T>[], nomeArquivo: string): void {
    if (!this.validarColunas(colunas)) return;

    try {
      const linhas = [
        colunas.map((coluna) => this.escaparCSV(coluna.titulo)).join(','),
        ...dados.map((registro) =>
          colunas.map((coluna) => this.escaparCSV(this.obterValor(coluna, registro))).join(','),
        ),
      ];
      const conteudo = `\uFEFF${linhas.join('\r\n')}`;
      this.baixar(new Blob([conteudo], { type: MIME_CSV }), nomeArquivo, 'csv');
    } catch (erro) {
      console.error('Não foi possível gerar o arquivo CSV.', erro);
      this.notificacao.erro('Não foi possível gerar o arquivo CSV.');
    }
  }

  private validarColunas<T>(colunas: ColunaExportacao<T>[]): boolean {
    if (colunas.length > 0) return true;

    this.notificacao.erro('Selecione ao menos uma coluna para exportar.');
    return false;
  }

  private obterValor<T>(coluna: ColunaExportacao<T>, registro: T): string | number {
    const valor = coluna.valor(registro);
    if (valor === null || valor === undefined) return '';
    if (typeof valor === 'boolean') return valor ? 'Sim' : 'Não';
    return valor;
  }

  private escaparCSV(valor: string | number): string {
    const texto = String(valor);
    const seguro = typeof valor === 'string' && /^\s*[=+\-@]/.test(texto) ? `'${texto}` : texto;
    return `"${seguro.replaceAll('"', '""')}"`;
  }

  private obterBordas(): Borders {
    return {
      top: { style: 'thin', color: { argb: 'FFB8C0C8' } },
      bottom: { style: 'thin', color: { argb: 'FFB8C0C8' } },
      left: { style: 'thin', color: { argb: 'FFB8C0C8' } },
      right: { style: 'thin', color: { argb: 'FFB8C0C8' } },
      diagonal: { up: false, down: false },
    };
  }

  private baixar(arquivo: Blob, nomeArquivo: string, extensao: FormatoExportacao): void {
    const url = URL.createObjectURL(arquivo);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${nomeArquivo}-${new Date().toISOString().slice(0, 10)}.${extensao}`;
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
