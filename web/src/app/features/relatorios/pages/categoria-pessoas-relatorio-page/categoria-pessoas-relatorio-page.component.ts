import { Component, inject, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { finalize } from 'rxjs';
import { GraficoRoscaComponent } from '../../components/grafico-rosca/grafico-rosca.component';
import { RankingCardsComponent } from '../../components/ranking-cards/ranking-cards.component';
import { ResumoCategoriasPessoa } from '../../models/relatorio-pessoas.model';
import { RelatorioPessoasService } from '../../services/relatorio-pessoas.service';

const RESUMO_INICIAL: ResumoCategoriasPessoa = {
  totalVinculos: 0,
  top5MaisFrequentes: [],
  distribuicao: [],
};

@Component({
  selector: 'app-categoria-pessoas-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    RankingCardsComponent,
    GraficoRoscaComponent,
  ],
  templateUrl: './categoria-pessoas-relatorio-page.component.html',
})
export class CategoriaPessoasRelatorioPageComponent implements OnInit {
  private readonly relatorioPessoasService = inject(RelatorioPessoasService);
  private readonly notificacao = inject(NotificacaoService);

  protected readonly carregando = signal(false);
  protected readonly dados = signal<ResumoCategoriasPessoa>(RESUMO_INICIAL);

  ngOnInit(): void {
    this.carregarRelatorio();
  }

  private carregarRelatorio(): void {
    this.carregando.set(true);
    this.relatorioPessoasService
      .obterResumoCategorias()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (resposta) => {
          this.dados.set(resposta);
        },
        error: () => {
          this.notificacao.erro('Não foi possível carregar os dados do relatório de categorias.');
        },
      });
  }
}
