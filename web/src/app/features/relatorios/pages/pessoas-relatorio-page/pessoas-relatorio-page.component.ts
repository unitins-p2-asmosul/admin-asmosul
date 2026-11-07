import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { CardResumoComponent } from '../../components/card-resumo/card-resumo.component';
import { GraficoRoscaComponent } from '../../components/grafico-rosca/grafico-rosca.component';
import { CriterioAgrupamentoPessoa, ResumoPessoasGeral } from '../../models/relatorio-pessoas.model';
import { RelatorioPessoasService } from '../../services/relatorio-pessoas.service';
import {formatarDistribuicaoDemografica} from '@features/relatorios/utils/relatorio-pessoas.utils';
import {ItemGrafico} from '@features/relatorios/models/relatorio-comum.model';

interface OpcaoCriterio {
  codigo: CriterioAgrupamentoPessoa;
  rotulo: string;
}

const CRITERIO_PADRAO: CriterioAgrupamentoPessoa = 'SEXO';

const RESUMO_INICIAL: ResumoPessoasGeral = {
  totalBeneficiarios: 0,
  totalDoadores: 0,
  totalCadastrosGerais: 0,
  totalInativos: 0,
  totalPessoasGrafico: 0,
  distribuicao: [],
};

@Component({
  selector: 'app-pessoas-relatorio-page',
  standalone: true,
  imports: [
    MatIconModule,
    CardResumoComponent,
    GraficoRoscaComponent,
  ],
  templateUrl: './pessoas-relatorio-page.component.html',
})
export class PessoasRelatorioPageComponent {
  private readonly relatorioService = inject(RelatorioPessoasService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly carregando = signal(false);

  protected readonly opcoesCriterio: OpcaoCriterio[] = [
    { codigo: 'SEXO', rotulo: 'Sexo' },
    { codigo: 'ESCOLARIDADE', rotulo: 'Nível de Escolaridade' },
    { codigo: 'FAIXA_RENDA', rotulo: 'Faixa de Renda' },
  ];

  private readonly criterio$ = this.route.queryParamMap.pipe(
    map((params) => {
      const criterioParam = params.get('criterio') as CriterioAgrupamentoPessoa | null;
      if (
        criterioParam === 'SEXO' ||
        criterioParam === 'ESCOLARIDADE' ||
        criterioParam === 'FAIXA_RENDA'
      ) {
        return criterioParam;
      }
      return CRITERIO_PADRAO;
    }),
  );

  protected readonly criterioAtivo = toSignal(
    this.criterio$,
    { initialValue: CRITERIO_PADRAO }
  );

  private readonly dados$ = this.criterio$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((criterio) =>
      this.relatorioService.obterResumoGeral(criterio).pipe(
        catchError(() => {
          this.notificacao.erro('Não foi possível carregar os dados demográficos de pessoas.');
          return of(RESUMO_INICIAL);
        }),
        finalize(() => this.carregando.set(false)),
      ),
    ),
  );

  protected readonly dados = toSignal(
    this.dados$,
    { initialValue: RESUMO_INICIAL }
  );

  protected readonly tituloGrafico = computed(() => {
    switch (this.criterioAtivo()) {
      case 'ESCOLARIDADE':
        return 'Divisão das Pessoas da ASMOSUL por Nível de Escolaridade';
      case 'FAIXA_RENDA':
        return 'Divisão das Pessoas da ASMOSUL por Faixa de Renda';
      case 'SEXO':
      default:
        return 'Divisão das Pessoas da ASMOSUL por Sexo';
    }
  });

  protected alternarCriterio(criterio: CriterioAgrupamentoPessoa): void {
    if (this.criterioAtivo() === criterio) return;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { criterio: criterio === CRITERIO_PADRAO ? null : criterio },
      queryParamsHandling: 'merge',
    });
  }

  protected readonly distribuicaoFormatada = computed<ItemGrafico[]>(() => {
    const distribuicaoOriginal = this.dados().distribuicao ?? [];
    const criterio = this.criterioAtivo();
    return formatarDistribuicaoDemografica(distribuicaoOriginal, criterio);
  });
}
