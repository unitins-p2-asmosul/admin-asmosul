import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import {ItemRanking} from '@features/relatorios/models/relatorio-comum.model';

@Component({
  selector: 'app-ranking-cards',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './ranking-cards.component.html',
})
export class RankingCardsComponent {
  readonly titulo = input.required<string>();
  readonly itens = input.required<ItemRanking[]>();
  readonly icone = input<string>('trending_up');
  readonly sufixo = input<string>('pessoas');
}
