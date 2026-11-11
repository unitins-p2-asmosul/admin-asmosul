import { inject, Injectable } from '@angular/core';
import type { Borders } from 'exceljs';
import { Observable, take } from 'rxjs';
import {
  ColunaExportacao,
  FormatoExportacao,
  SolicitacaoExportacao,
} from '../models/exportacao.model';
import { NotificacaoService } from './notificacao.service';
import {ItemLegenda} from '@features/relatorios/components/grafico-rosca/grafico-rosca.component';

export interface SolicitacaoExportacaoGrafico {
  canvas: HTMLCanvasElement;
  titulo: string;
  totalGeral: number;
  legendas: ItemLegenda[];
}

const MIME_XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
const MIME_CSV = 'text/csv;charset=utf-8';
const COR_CABECALHO = 'FF123B55';
const CORES_LINHAS = ['FFF1F3F5', 'FFE4E8EC'];

@Injectable({ providedIn: 'root' })
export class ExportacaoService {
  private readonly notificacao = inject(NotificacaoService);

  exportarGraficoRosca(solicitacao: SolicitacaoExportacaoGrafico): void {
    const { canvas, titulo, totalGeral, legendas } = solicitacao;

    if (!canvas || legendas.length === 0) {
      this.notificacao.erro('Não há dados disponíveis para exportar imagem.');
      return;
    }

    try {
      const escala = 2;
      const largura = 900;
      const alturaCabecalho = 70;
      const alturaCorpo = Math.max(320, 60 + legendas.length * 36);
      const alturaTotal = alturaCabecalho + alturaCorpo;

      const canvasExportacao = document.createElement('canvas');
      canvasExportacao.width = largura * escala;
      canvasExportacao.height = alturaTotal * escala;

      const ctx = canvasExportacao.getContext('2d');
      if (!ctx) return;

      ctx.scale(escala, escala);

      // Fundo branco
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, largura, alturaTotal);

      // Faixa do cabeçalho azul escuro (#0b3c5d)
      ctx.fillStyle = '#0b3c5d';
      ctx.fillRect(0, 0, largura, alturaCabecalho);

      // Título
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 20px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
      ctx.textBaseline = 'middle';
      ctx.fillText(titulo, 32, alturaCabecalho / 2);

      // Desenho da Rosca
      const graficoTamanho = 220;
      const graficoX = 70;
      const graficoY = alturaCabecalho + (alturaCorpo - graficoTamanho) / 2;
      ctx.drawImage(canvas, graficoX, graficoY, graficoTamanho, graficoTamanho);

      // Rótulo central "TOTAL" e valor
      const centroX = graficoX + graficoTamanho / 2;
      const centroY = graficoY + graficoTamanho / 2;

      ctx.textAlign = 'center';
      ctx.fillStyle = '#8298a9';
      ctx.font = 'bold 11px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
      ctx.fillText('TOTAL', centroX, centroY - 8);

      ctx.fillStyle = '#0b3c5d';
      ctx.font = '800 24px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
      ctx.fillText(totalGeral.toString(), centroX, centroY + 16);

      // Lista de Legendas
      const legendaX = 360;
      let legendaY = alturaCabecalho + 60;
      const espacamentoLinha = 34;

      ctx.textAlign = 'left';

      legendas.forEach((item) => {
        ctx.beginPath();
        ctx.arc(legendaX + 6, legendaY, 6, 0, Math.PI * 2);
        ctx.fillStyle = item.cor;
        ctx.fill();

        ctx.fillStyle = '#344054';
        ctx.font = '600 14px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
        ctx.fillText(item.chave, legendaX + 22, legendaY + 4);

        ctx.textAlign = 'right';
        ctx.fillStyle = '#1f2937';
        ctx.font = 'bold 14px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
        ctx.fillText(item.total.toString(), largura - 110, legendaY + 4);

        ctx.fillStyle = '#8298a9';
        ctx.font = '500 14px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
        ctx.fillText(item.porcentagem, largura - 40, legendaY + 4);

        ctx.textAlign = 'left';
        legendaY += espacamentoLinha;
      });

      const nomeNormalizado = titulo
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '');

      canvasExportacao.toBlob((blob) => {
        if (blob) {
          this.baixar(blob, nomeNormalizado || 'grafico-relatorio', 'png' as FormatoExportacao);
        }
      }, 'image/png');
    } catch (erro) {
      console.error('Não foi possível gerar a imagem do gráfico.', erro);
      this.notificacao.erro('Não foi possível gerar a imagem do gráfico.');
    }
  }

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

  exportarGraficoBarras(canvas: HTMLCanvasElement, titulo: string): void {
    if (!canvas) {
      this.notificacao.erro('Não há gráfico disponível para exportar.');
      return;
    }

    try {
      const escala = 2;
      const largura = 900;
      const alturaCabecalho = 70;
      const alturaCorpo = 380;
      const alturaTotal = alturaCabecalho + alturaCorpo;

      const canvasExportacao = document.createElement('canvas');
      canvasExportacao.width = largura * escala;
      canvasExportacao.height = alturaTotal * escala;

      const ctx = canvasExportacao.getContext('2d');
      if (!ctx) return;

      ctx.scale(escala, escala);

      // Fundo branco
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, largura, alturaTotal);

      // Faixa superior Azul Marinho (#0b3c5d)
      ctx.fillStyle = '#0b3c5d';
      ctx.fillRect(0, 0, largura, alturaCabecalho);

      // Título em branco
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 20px "Segoe UI", Roboto, Helvetica, Arial, sans-serif';
      ctx.textBaseline = 'middle';
      ctx.fillText(titulo, 32, alturaCabecalho / 2);

      // Desenha o gráfico centralizado no corpo
      const margemX = 40;
      const margemY = 30;
      ctx.drawImage(
        canvas,
        margemX,
        alturaCabecalho + margemY,
        largura - margemX * 2,
        alturaCorpo - margemY * 2,
      );

      const nomeNormalizado = titulo
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '');

      canvasExportacao.toBlob((blob) => {
        if (blob) {
          this.baixar(blob, nomeNormalizado || 'grafico-barras', 'png');
        }
      }, 'image/png');
    } catch (erro) {
      console.error('Não foi possível gerar a imagem do gráfico.', erro);
      this.notificacao.erro('Não foi possível gerar a imagem do gráfico.');
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

  private baixar(arquivo: Blob, nomeArquivo: string, extensao: FormatoExportacao | 'png'): void {
    const url = URL.createObjectURL(arquivo);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${nomeArquivo}-${new Date().toISOString().slice(0, 10)}.${extensao}`;
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 0);
  }
}
