import { Component, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { CardResumoComponent } from '../../components/card-resumo/card-resumo.component';
import { GraficoRoscaComponent } from '../../components/grafico-rosca/grafico-rosca.component';
import { ResumoArmazenamento } from '../../models/relatorio-doacoes.model';
import { RelatorioDoacoesService } from '../../services/relatorio-doacoes.service';

const RESUMO_INICIAL: ResumoArmazenamento = {
  totalLocais: 0,
  volumeFisicoTotal: 0,
  locaisEmUso: 0,
  distribuicaoVolumePorLocal: [],
};

@Component({
  selector: 'app-armazenamentos-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    CardResumoComponent,
    GraficoRoscaComponent,
  ],
  templateUrl: './armazenamentos-relatorios-page.component.html',
})
export class ArmazenamentosRelatorioPageComponent implements OnInit {
  private readonly relatorioService = inject(RelatorioDoacoesService);
  private readonly notificacao = inject(NotificacaoService);

  protected readonly carregando = signal(false);
  protected readonly dados = signal<ResumoArmazenamento>(RESUMO_INICIAL);

  ngOnInit(): void {
    this.carregarDados();
  }

  private carregarDados(): void {
    this.carregando.set(true);
    this.relatorioService
      .obterResumoArmazenamento()
      .pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar os dados de armazenamento.');
          return of(RESUMO_INICIAL);
        }),
        finalize(() => this.carregando.set(false)),
      )
      .subscribe((resposta) => this.dados.set(resposta));
  }
}
