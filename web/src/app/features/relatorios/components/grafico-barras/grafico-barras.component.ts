import {

  Component,
  effect,
  ElementRef,
  inject,
  input,
  OnDestroy,
  output,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ExportacaoService } from '@features/shared/services/exportacao.service';
import { Chart, registerables } from 'chart.js';
import { ItemGrafico } from '../../models/relatorio-comum.model';

Chart.register(...registerables);

@Component({
  selector: 'app-grafico-barras',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './grafico-barras.component.html',
})
export class GraficoBarrasComponent implements  OnDestroy {
  private readonly exportacaoService = inject(ExportacaoService);

  readonly titulo = input.required<string>();
  readonly dados = input.required<ItemGrafico[]>();
  readonly exibirSeletorAno = input<boolean>(false);
  readonly anoSelecionado = input<number>(new Date().getFullYear());
  readonly anosDisponiveis = input<number[]>([
    new Date().getFullYear(),
    new Date().getFullYear() - 1,
    new Date().getFullYear() - 2,
  ]);

  readonly aoMudarAno = output<number>();

  private readonly canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chartInstance: Chart<'bar'> | null = null;
  constructor() {
    effect(() => {
      const canvasEl = this.canvasRef()?.nativeElement;
      const lista = this.dados();

      if (!canvasEl) return;

      if (!this.chartInstance) {
        this.criarGrafico(canvasEl, lista);
      } else {
        this.atualizarGrafico(lista);
      }
    });
  }



  ngOnDestroy(): void {
    this.destruirGrafico();
  }

  protected selecionarAno(ano: string | number): void {
    this.aoMudarAno.emit(Number(ano));
  }

  protected exportarImagem(): void {
    const canvas = this.canvasRef()?.nativeElement;
    if (!canvas) return;
    this.exportacaoService.exportarGraficoBarras(canvas, this.titulo());
  }

  private criarGrafico(canvas: HTMLCanvasElement, itens: ItemGrafico[]): void {

    this.chartInstance = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: itens.map((i) => i.rotulo),
        datasets: [
          {
            data: itens.map((i) => i.quantidade),
            backgroundColor: '#0b3c5d',
            borderRadius: 6,
            maxBarThickness: 42,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => ` ${ctx.raw} un.`,
            },
          },
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#64748b', font: { weight: 'bold', size: 11 } },
          },
          y: {
            beginAtZero: true,
            grid: { color: '#f1f5f9' },
            ticks: { precision: 0, color: '#8298a9', font: { size: 11 } },
          },
        },
      },
    });
  }

  private atualizarGrafico(itens: ItemGrafico[]): void {
    if (!this.chartInstance) return;
    this.chartInstance.data.labels = itens.map((i) => i.rotulo);
    this.chartInstance.data.datasets[0].data = itens.map((i) => i.quantidade);
    this.chartInstance.update();
  }

  private destruirGrafico(): void {
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }
}
