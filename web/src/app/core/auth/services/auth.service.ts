import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, catchError, map, Observable, of, switchMap, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import {
  CredenciaisLogin,
  LoginResposta,
  Perfil,
  RedefinirMinhaSenhaRequisicao,
  TokenJwtPayload,
  UsuarioAutenticado,
} from '../models/auth.model';
import { ContaDetalhe } from "@features/acessos/models/conta.model";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'auth/login';
  private readonly CHAVE_TOKEN = '@asmosul:token';

  private readonly usuarioAtualSubject = new BehaviorSubject<UsuarioAutenticado | null>(null);
  readonly usuarioAtual$ = this.usuarioAtualSubject.asObservable();

  constructor() {
    const token = this.obterToken();
    if (!token) return;

    const usuario = this.extrairUsuarioDoToken(token);
    if (usuario && !this.tokenExpirado(usuario.expiracao)) {
      this.usuarioAtualSubject.next(usuario);
    } else {
      this.removerToken();
    }
  }

  login(credenciais: CredenciaisLogin): Observable<LoginResposta> {
    return this.http.post<LoginResposta>(this.endpoint, credenciais).pipe(
      tap((resposta) => {
        this.salvarToken(resposta.token);
        this.usuarioAtualSubject.next(this.extrairUsuarioDoToken(resposta.token));
      }),
      switchMap((resposta) =>
        this.obterMinhaConta().pipe(map(() => resposta)),
      ),
    );
  }

  logout(): void {
    this.removerToken();
    this.usuarioAtualSubject.next(null);
  }

  obterUsuarioAtual(): UsuarioAutenticado | null {
    return this.usuarioAtualSubject.getValue();
  }

  obterToken(): string | null {
    try {
      return localStorage.getItem(this.CHAVE_TOKEN);
    } catch {
      return null;
    }
  }

  estaAutenticado(): boolean {
    const token = this.obterToken();
    if (!token) {
      return false;
    }

    const usuario = this.obterUsuarioAtual();
    if (usuario) {
      return !this.tokenExpirado(usuario.expiracao);
    }

    const extraido = this.extrairUsuarioDoToken(token);
    if (extraido && !this.tokenExpirado(extraido.expiracao)) {
      this.usuarioAtualSubject.next(extraido);
      return true;
    }

    return false;
  }

  temPerfil(perfil: Perfil): boolean {
    const usuario = this.obterUsuarioAtual();
    if (!usuario || !Array.isArray(usuario.perfis)) {
      return false;
    }
    return usuario.perfis.includes(perfil);
  }

  consultarRedefinirSenha(): Observable<boolean> {
    const usuario = this.obterUsuarioAtual();
    if (usuario?.redefinirSenha !== undefined) {
      return of(usuario.redefinirSenha);
    }

    return this.http.get<{ redefinirSenha?: boolean }>('acessos/contas/eu').pipe(
      map((conta) => {
        const flag = !!conta.redefinirSenha;
        this.atualizarStatusRedefinirSenha(flag);
        return flag;
      }),
      catchError(() => of(false)),
    );
  }

  alterarMinhaSenha(dados: RedefinirMinhaSenhaRequisicao): Observable<void> {
    return this.http.patch<void>('acessos/contas/minha-senha', dados).pipe(
      tap(() => {
        this.atualizarStatusRedefinirSenha(false);
      }),
    );
  }

  obterMinhaConta(): Observable<ContaDetalhe> {
    return this.http.get<ContaDetalhe>('acessos/contas/eu').pipe(
      tap((conta) => {
        const usuarioAtual = this.obterUsuarioAtual();
        if (usuarioAtual) {
          this.usuarioAtualSubject.next({
            ...usuarioAtual,
            id: conta.id,
            redefinirSenha: conta.redefinirSenha,
            perfis: conta.perfis?.map(p => (typeof p === 'string' ? p : p.codigo) as Perfil) ?? usuarioAtual.perfis,
          });
        }
      })
    );
  }

  atualizarStatusRedefinirSenha(redefinirSenha: boolean): void {
    const usuario = this.obterUsuarioAtual();
    if (usuario) {
      this.usuarioAtualSubject.next({
        ...usuario,
        redefinirSenha,
      });
    }
  }

  restaurarSessao(): void {
    if (!this.obterUsuarioAtual()) return;

    this.obterMinhaConta().subscribe({
      error: (erro: HttpErrorResponse) => {
        if (erro.status === 401) {
          this.logout();
        }
      },
    });
  }

  private salvarToken(token: string): void {
    try {
      localStorage.setItem(this.CHAVE_TOKEN, token);
    } catch {
      // Falha silenciosa em ambientes sem acesso a storage
    }
  }

  private removerToken(): void {
    try {
      localStorage.removeItem(this.CHAVE_TOKEN);
    } catch {
      // Falha silenciosa
    }
  }

  private extrairUsuarioDoToken(token: string): UsuarioAutenticado | null {
    try {
      const payload = jwtDecode<TokenJwtPayload>(token);
      if (!payload || !payload.sub) {
        return null;
      }

      return {
        id: payload.id,
        nomeUsuario: payload.sub,
        perfis: payload.perfis ?? [],
        expiracao: payload.exp,
        redefinirSenha: payload.redefinirSenha,
      };
    } catch {
      return null;
    }
  }

  private tokenExpirado(expiracaoSegundos: number): boolean {
    if (!expiracaoSegundos) {
      return true;
    }
    const agoraEmSegundos = Math.floor(Date.now() / 1000);
    return expiracaoSegundos <= agoraEmSegundos;
  }
}
