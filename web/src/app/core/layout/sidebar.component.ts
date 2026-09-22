import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs/operators';

/** Item de navegação. Sem `rota` = módulo ainda não implementado. */
export interface MenuItem {
	rotulo: string;
	icone: string;
	rota?: string;
}

/**
 * Seção do menu.
 *
 * - `itens` vazio + `rota` preenchida: link direto, sem sanfona (ex.: Início).
 * - `emDefinicao`: módulo ainda sem escopo fechado, exibido desabilitado.
 */
export interface MenuSecao {
	id: string;
	titulo: string;
	icone: string;
	rota?: string;
	itens: MenuItem[];
	emDefinicao?: boolean;
}

@Component({
	selector: 'app-sidebar',
	standalone: true,
	imports: [RouterLink],
	templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
	private readonly router = inject(Router);

	protected readonly secoes = signal<MenuSecao[]>([
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
			itens: [
				{ rotulo: 'Pessoas', icone: 'person', rota: '/pessoas' },
				{ rotulo: 'Comorbidades', icone: 'medical_information', rota: '/pessoas/comorbidades' },
				{ rotulo: 'Categorias', icone: 'category', rota: '/pessoas/categorias' },
			],
		},
		{
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
			itens: [
				{ rotulo: 'Contas', icone: 'manage_accounts' },
				{ rotulo: 'Perfis', icone: 'admin_panel_settings' },
			],
		},
	]);

	private readonly expandidas = signal<ReadonlySet<string>>(new Set<string>());

	private readonly urlAtual = toSignal(
		this.router.events.pipe(
			filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
			map((evento) => this.semQueryParams(evento.urlAfterRedirects)),
		),
		{ initialValue: this.semQueryParams(this.router.url) },
	);

	/**
	 * Rota ativa pelo prefixo mais longo que casa com a URL.
	 *
	 * Resolve o caso de `/pessoas` ser prefixo de `/pessoas/categorias`:
	 * o `routerLinkActive` padrão acenderia os dois ao mesmo tempo.
	 */
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
		// Abre automaticamente a seção do módulo em que o usuário está navegando.
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
