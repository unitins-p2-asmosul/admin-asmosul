import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./home.component').then((modulo) => modulo.HomeComponent),
	},
	{
		path: 'pessoas',
		loadChildren: () =>
			import('@features/pessoas/pessoas.routes').then((modulo) => modulo.PESSOAS_ROUTES),
	},
];
