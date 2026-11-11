import { Component, input } from '@angular/core';
import { ItemRanking } from '../../models/relatorio-comum.model';

@Component({
  selector: 'app-ranking-cards',
  standalone: true,
  templateUrl: './ranking-cards.component.html',
})
export class RankingCardsComponent {
  readonly titulo = input.required<string>();
  readonly itens = input.required<ItemRanking[]>();
  readonly sufixo = input<string>('un.');
}
