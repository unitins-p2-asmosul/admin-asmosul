import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  computed,
  effect,
  input,
  viewChild,
} from '@angular/core';
import { Chart, DoughnutController, ArcElement, Tooltip } from 'chart.js';
import { ItemGrafico } from '../../models/relatorio-comum.model';

Chart.register(DoughnutController, ArcElement, Tooltip);

const CORES_PADRAO = [
  '#0f3854', // Azul marinho escuro
  '#0284c7', // Azul ciano
  '#10b981', // Verde esmeralda
  '#f59e0b', // Laranja
  '#8b5cf6', // Roxo
  '#ec4899', // Rosa (fallback caso haja > 5 itens)
  '#6b7280', // Cinza (fallback)
];

export interface ItemLegenda {
  chave: string;
  total: number;
  porcentagem: string;
  cor: string;
}

@Component({
  selector: 'app-grafico-rosca',
  standalone: true,
  templateUrl: './grafico-rosca.component.html',
})
export class GraficoRoscaComponent implements AfterViewInit, OnDestroy {
  readonly canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  readonly titulo = input.required<string>();
  readonly dados = input.required<ItemGrafico[]>();
  readonly cores = input<string[]>(CORES_PADRAO);

  private chart?: Chart<'doughnut'>;

  protected readonly totalGeral = computed(() =>
    this.dados().reduce((acc, curr) => acc + (curr.quantidade || 0), 0)
  );

  protected readonly itensLegenda = computed<ItemLegenda[]>(() => {
    const lista = this.dados();
    const total = this.totalGeral();
    const paleta = this.cores();

    return lista.map((item, index) => {
      const porcentagemCalculada = total > 0 ? (item.quantidade / total) * 100 : 0;
      const pctFormatada = item.percentual !== undefined
        ? item.percentual.toFixed(1)
        : porcentagemCalculada.toFixed(1);

      return {
        chave: item.rotulo,
        total: item.quantidade,
        porcentagem: `${pctFormatada}%`,
        cor: paleta[index % paleta.length],
      };
    });
  });

  constructor() {
    effect(() => {
      const legenda = this.itensLegenda();
      if (this.chart) {
        this.atualizarGrafico(legenda);
      }
    });
  }

  ngAfterViewInit(): void {
    this.criarGrafico();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private criarGrafico(): void {
    const canvas = this.canvasRef().nativeElement;
    const legenda = this.itensLegenda();

    this.chart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: legenda.map((i) => i.chave),
        datasets: [
          {
            data: legenda.map((i) => i.total),
            backgroundColor: legenda.map((i) => i.cor),
            borderWidth: 0,
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '55%',
        plugins: {
          legend: {
            display: false,
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const valor = context.raw as number;
                const total = this.totalGeral();
                const pct = total > 0 ? ((valor / total) * 100).toFixed(1) : '0';
                return ` ${context.label}: ${valor} (${pct}%)`;
              },
            },
          },
        },
      },
    });
  }

  private atualizarGrafico(legenda: ItemLegenda[]): void {
    if (!this.chart) return;
    this.chart.data.labels = legenda.map((i) => i.chave);
    this.chart.data.datasets[0].data = legenda.map((i) => i.total);
    this.chart.data.datasets[0].backgroundColor = legenda.map((i) => i.cor);
    this.chart.update();
  }
}
