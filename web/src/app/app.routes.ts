import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		pathMatch: 'full',
		redirectTo: 'categorias/novo',
	},
	{
		path: 'categorias',
		loadChildren: () =>
			import('./features/categorias/categorias.routes').then((modulo) => modulo.CATEGORIAS_ROUTES),
	},
	{
		path: 'pessoas',
		loadChildren: () =>
			import('./features/pessoas/pessoas.routes').then((modulo) => modulo.PESSOAS_ROUTES),
	},
	{
		path: '**',
		redirectTo: 'categorias/novo',
	},
];
