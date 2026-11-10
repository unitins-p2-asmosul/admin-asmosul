import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Perfil } from '@core/auth/models/auth.model';
import { AuthService } from '@core/auth/services/auth.service';
import {
  CardModuloComponent,
  VarianteCardModulo,
} from '@features/shared/components/card-modulo/card-modulo.component';

interface ModuloHome {
  id: string;
  titulo: string;
  descricao: string;
  icone: string;
  rotuloAcao: string;
  variante: VarianteCardModulo;
  perfil: Perfil;
  rota?: string;
}

const SEM_PERMISSAO = 'Você não tem permissão para acessar este módulo.';
const EM_DESENVOLVIMENTO = 'Módulo em desenvolvimento.';

const MODULOS: ModuloHome[] = [
  {
    id: 'pessoas',
    titulo: 'Pessoas',
    descricao:
      'Cadastro e assistência a famílias vulneráveis, beneficiários atendidos, doadores parceiros, mapeamento de comorbidades e categorias sociais.',
    icone: 'groups',
    rotuloAcao: 'Acessar Pessoas',
    variante: 'pessoas',
    perfil: Perfil.GERENCIADOR_PESSOAS,
    rota: '/pessoas',
  },
  {
    id: 'doacoes',
    titulo: 'Doações',
    descricao:
      'Entradas de mantimentos, saídas aos beneficiários, controle de cestas e kits, depósitos e movimentações de transferência física.',
    icone: 'card_giftcard',
    rotuloAcao: 'Acessar Doações',
    variante: 'doacoes',
    perfil: Perfil.GERENCIADOR_DOACOES,
    rota: '/doacoes/itens',
  },
  {
    id: 'relatorios',
    titulo: 'Relatórios',
    descricao:
      'Painéis analíticos completos divididos em Pessoas, Doações e Contas, com gráficos interativos e exportação em Excel, CSV e PDF.',
    icone: 'edit_note',
    rotuloAcao: 'Acessar Relatórios',
    variante: 'relatorios',
    perfil: Perfil.GERENCIADOR_RELATORIOS,
    rota: 'relatorios/pessoas'
  },
  {
    id: 'acesso',
    titulo: 'Acesso',
    descricao:
      'Administração de usuários e auditoria de perfis operacionais: Gestor de Pessoas, Gestor de Doações e Gestor de Acesso.',
    icone: 'badge',
    rotuloAcao: 'Acessar Contas',
    variante: 'acesso',
    perfil: Perfil.GERENCIADOR_ACESSO,
    rota: '/acessos',
  },
];

const URL_GUIA_USUARIO =
  'https://docs.google.com/document/d/1kObZc2Z3okHlf-dmAjZHXLhnDtbNFXtby-3lEhsCpQc/edit?usp=sharing';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CardModuloComponent, MatButtonModule, MatIconModule],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  private readonly authService = inject(AuthService);

  protected readonly urlGuiaUsuario = URL_GUIA_USUARIO;

  private readonly usuario = toSignal(this.authService.usuarioAtual$, {
    initialValue: this.authService.obterUsuarioAtual(),
  });

  protected readonly modulos = computed(() => {
    const perfis = this.usuario()?.perfis ?? [];

    return MODULOS.map((modulo) => {
      const temPermissao = perfis.includes(modulo.perfil);

      return {
        ...modulo,
        rota: temPermissao ? modulo.rota : undefined,
        motivoBloqueio: !temPermissao
          ? SEM_PERMISSAO
          : modulo.rota
            ? undefined
            : EM_DESENVOLVIMENTO,
      };
    });
  });
}
