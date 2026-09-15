import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
	selector: 'app-sidebar',
	standalone: true,
	imports: [RouterLink, RouterLinkActive],
	templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
	private readonly router = inject(Router);

	protected pessoasSelecionado(): boolean {
		const url = this.router.url.split('?')[0];
		return url === '/pessoas' || (url.startsWith('/pessoas/') && !url.startsWith('/pessoas/categorias'));
	}
}
