import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { AuthService } from '../auth/services/auth.service'; // ajuste ao caminho real
import { Perfil } from '../auth/models/auth.model'; // ajuste ao caminho real

export interface MenuItem {
  rotulo: string;
  icone: string;
  rota?: string;
}

export interface MenuSecao {
  id: string;
  titulo: string;
  icone: string;
  rota?: string;
  itens: MenuItem[];
  emDefinicao?: boolean;
  /** Perfil exigido para ver a seção. Sem perfil = visível para qualquer usuário. */
  perfil?: Perfil;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  private readonly todasSecoes = signal<MenuSecao[]>([
    {
      id: 'inicio',
      titulo: 'Início',
      icone: 'home',
      rota: '/',
      itens: [],
    },
    {
      id: 'pessoas',
      titulo: 'Pessoas',
      icone: 'groups',
      perfil: Perfil.GERENCIADOR_PESSOAS,
      itens: [
        { rotulo: 'Pessoas', icone: 'person', rota: '/pessoas' },
        { rotulo: 'Comorbidades', icone: 'medical_information', rota: '/pessoas/comorbidades' },
        { rotulo: 'Categorias', icone: 'category', rota: '/pessoas/categorias' },
      ],
    },
    {
      // Ainda não implementado: visível para todos
      id: 'doacoes',
      titulo: 'Doações',
      icone: 'card_giftcard',
      itens: [
        { rotulo: 'Itens', icone: 'inventory_2' },
        { rotulo: 'Kits', icone: 'widgets' },
        { rotulo: 'Entradas', icone: 'input' },
        { rotulo: 'Saídas', icone: 'output' },
        { rotulo: 'Endereço', icone: 'location_on' },
      ],
    },
    {
      id: 'capacitacoes',
      titulo: 'Capacitações',
      icone: 'menu_book',
      itens: [],
      emDefinicao: true,
    },
    {
      id: 'relatorios',
      titulo: 'Relatórios',
      icone: 'edit_note',
      itens: [],
      emDefinicao: true,
    },
    {
      id: 'acesso',
      titulo: 'Acesso',
      icone: 'badge',
      perfil: Perfil.GERENCIADOR_ACESSO,
      itens: [
        { rotulo: 'Contas', icone: 'manage_accounts', rota: '/acessos' },
        { rotulo: 'Perfis', icone: 'admin_panel_settings', rota: '/acessos/perfis' },
      ],
    },
  ]);

  private readonly usuario = toSignal(this.authService.usuarioAtual$, {
    initialValue: this.authService.obterUsuarioAtual(),
  });

  /** Seções que o usuário atual pode ver. */
  protected readonly secoes = computed(() => {
    const perfis = this.usuario()?.perfis ?? [];

    return this.todasSecoes().filter((secao) => !secao.perfil || perfis.includes(secao.perfil));
  });

  private readonly expandidas = signal<ReadonlySet<string>>(new Set<string>());

  private readonly urlAtual = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => this.semQueryParams(evento.urlAfterRedirects)),
    ),
    { initialValue: this.semQueryParams(this.router.url) },
  );

  protected readonly rotaAtiva = computed<string | null>(() => {
    const url = this.urlAtual();

    return this.secoes()
      .flatMap((secao) => (secao.rota ? [secao.rota] : secao.itens.map((item) => item.rota)))
      .filter((rota): rota is string => !!rota)
      .filter((rota) => url === rota || url.startsWith(`${rota}/`))
      .sort((a, b) => b.length - a.length)[0] ?? null;
  });

  private readonly secaoAtiva = computed<string | null>(() => {
    const ativa = this.rotaAtiva();
    if (!ativa) {
      return null;
    }

    return this.secoes().find((secao) => secao.itens.some((item) => item.rota === ativa))?.id ?? null;
  });

  constructor() {
    effect(() => {
      const secao = this.secaoAtiva();
      if (!secao) {
        return;
      }

      this.expandidas.update((atuais) =>
        atuais.has(secao) ? atuais : new Set(atuais).add(secao),
      );
    });
  }

  protected estaExpandida(id: string): boolean {
    return this.expandidas().has(id);
  }

  protected alternarSecao(id: string): void {
    this.expandidas.update((atuais) => {
      const novas = new Set(atuais);
      if (!novas.delete(id)) {
        novas.add(id);
      }
      return novas;
    });
  }

  private semQueryParams(url: string): string {
    return url.split('?')[0];
  }
}
