import { Component, computed, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

export type VarianteCardModulo = 'pessoas' | 'doacoes' | 'relatorios' | 'acesso';

@Component({
  selector: 'app-card-modulo',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './card-modulo.component.html',
})
export class CardModuloComponent {
  readonly titulo = input.required<string>();
  readonly descricao = input.required<string>();
  readonly icone = input.required<string>();
  readonly rotuloAcao = input.required<string>();
  readonly variante = input.required<VarianteCardModulo>();
  readonly rota = input<string>();
  readonly motivoBloqueio = input<string>();

  protected readonly bloqueado = computed(() => !this.rota() || !!this.motivoBloqueio());

  protected readonly classesCard = computed(() => {
    const base = `card-asmosul card-modulo card-modulo--${this.variante()}`;
    return this.bloqueado() ? `${base} card-asmosul--bloqueado` : base;
  });

  protected readonly classesAcao = computed(
    () => `card-modulo__acao card-modulo__acao--${this.variante()}`,
  );
}
