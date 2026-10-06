import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from "@core/auth/services/auth.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatMenuModule, MatIconModule],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuarioAtual$, {
    initialValue: this.authService.obterUsuarioAtual(),
  });

  protected readonly iniciais = computed(() =>
    (this.usuario()?.nomeUsuario ?? '?').charAt(0).toUpperCase(),
  );

  protected readonly perfis = computed(() =>
    (this.usuario()?.perfis ?? []).map((perfil) => this.formatarPerfil(perfil)),
  );

  protected sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // GERENCIADOR_PESSOAS -> "Gerenciador Pessoas"
  private formatarPerfil(codigo: string): string {
    return codigo
      .toLowerCase()
      .split('_')
      .map((parte) => parte.charAt(0).toUpperCase() + parte.slice(1))
      .join(' ');
  }
}
