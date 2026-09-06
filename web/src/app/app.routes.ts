import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: 'pessoas',
		loadChildren: () =>
			import('@features/pessoas/pessoas.routes').then((modulo) => modulo.PESSOAS_ROUTES),
	},
];
