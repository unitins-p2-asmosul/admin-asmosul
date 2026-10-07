import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { PessoaResumo } from '@features/pessoas/models/pessoa.model';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { EMPTY } from 'rxjs';
import { concatMap, expand, map, reduce } from 'rxjs/operators';
import {
  ContaAtualizacao,
  ContaDetalhe,
  ContaRequisicao,
  PerfilCodigo,
  PerfilDisponivel,
} from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import {
  ContaRedefinirSenhaDialogComponent,
  ContaRedefinirSenhaDialogData,
} from '../../components/conta-redefinir-senha-dialog/conta-redefinir-senha-dialog.component';

@Component({
  selector: 'app-conta-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
  ],
  templateUrl: './conta-cadastro-page.component.html',
})
export class ContaCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly contaService = inject(ContaService);
  private readonly pessoaService = inject(PessoaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly dialog = inject(MatDialog);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly carregando = signal(false);
  protected readonly registro = signal<ContaDetalhe | null>(null);
  protected readonly pessoas = signal<PessoaResumo[]>([]);
  protected readonly perfis = signal<PerfilDisponivel[]>([]);

  protected readonly form = this.fb.group(
    {
      pessoaId: [0, [Validators.required, Validators.min(1)]],
      nomeUsuario: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
      senhaTemporaria: ['', Validators.required],
      confirmarSenha: ['', Validators.required],
      perfis: this.fb.control<PerfilCodigo[]>([], Validators.required),
    },
    { validators: senhasConferem },
  );

  protected readonly confirmarSenhaErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: (controle, formulario) =>
      !!controle
      && (controle.invalid || this.form.hasError('senhasDiferentes'))
      && (controle.touched || !!formulario?.submitted),
  };

  constructor() {
    this.pessoaService
      .listar({ page: 0, size: 100, sort: 'nome,asc', tipoPessoa: 'FISICA' })
      .subscribe({
        next: (resposta) => {
          const lista = resposta.dados || (resposta as any).content || [];
          this.pessoas.set(
            lista.filter((p: any) => p.tipoPessoa === 'FISICA' || p.tipoPessoa?.codigo === 'FISICA')
          );
        },
        error: (err) => console.error('Erro ao carregar pessoas para o select:', err)
      });

    this.contaService.listarPerfis().subscribe({
      next: (perfis) => this.perfis.set(perfis),
      error: () => this.perfis.set(perfisReserva()),
    });

    effect(() => {
      const idConta = this.id();
      if (idConta) this.carregar(Number(idConta));
    });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes da conta';
    return this.modoEdicao() ? 'Editar conta' : 'Criar nova conta';
  }

  protected get descricao(): string {
    if (this.visualizacao()) return 'Consulte os dados e os perfis vinculados a esta conta.';
    return this.modoEdicao()
      ? 'Altere o nome de usuário e os perfis de acesso.'
      : 'Preencha os dados abaixo para criar uma nova conta.';
  }

  protected salvar(): void {
    if (this.visualizacao()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacao.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    const valores = this.form.getRawValue();
    const requisicao = this.modoEdicao()
      ? this.contaService
          .atualizar(Number(this.id()), {
            nomeUsuario: valores.nomeUsuario.trim(),
          } satisfies ContaAtualizacao)
          .pipe(concatMap(() => this.contaService.atualizarPerfis(Number(this.id()), valores.perfis)))
      : this.contaService.cadastrar({
          pessoaId: valores.pessoaId,
          nomeUsuario: valores.nomeUsuario.trim(),
          senhaTemporaria: valores.senhaTemporaria,
          perfis: valores.perfis,
        } satisfies ContaRequisicao);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Conta ${this.modoEdicao() ? 'atualizada' : 'cadastrada'} com sucesso.`);
        this.location.back();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/acessos', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected redefinirSenha(): void {
    const conta = this.registro();
    if (!conta) return;

    const data: ContaRedefinirSenhaDialogData = {
      contaId: conta.id,
      nomePessoa: conta.nomePessoa,
      nomeUsuario: conta.nomeUsuario,
    };
    this.dialog.open<ContaRedefinirSenhaDialogComponent, ContaRedefinirSenhaDialogData, boolean>(
      ContaRedefinirSenhaDialogComponent,
      { width: '480px', maxWidth: 'calc(100vw - 32px)', data },
    ).afterClosed().subscribe((atualizada) => {
      if (atualizada) this.carregar(conta.id);
    });
  }

  protected async alternarEstado(): Promise<void> {
    const conta = this.registro();
    if (!conta) return;
    const reativar = !conta.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Desativar'} conta`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'desativar'} a conta de "${conta.nomePessoa}"?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    const operacao = reativar
      ? this.contaService.reativar(conta.id)
      : this.contaService.desativar(conta.id);
    operacao.subscribe({
      next: () => {
        this.notificacao.sucesso(`Conta ${reativar ? 'reativada' : 'desativada'} com sucesso.`);
        this.carregar(conta.id);
      },
      error: () => this.carregando.set(false),
    });
  }

  protected perfilDescricao(codigo: PerfilCodigo): string {
    return this.perfis().find((perfil) => perfil.codigo === codigo)?.descricao ?? codigo;
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.contaService.buscarPorId(id).subscribe({
      next: (conta) => {
        this.registro.set(conta);
        this.form.patchValue({
          pessoaId: conta.pessoaId,
          nomeUsuario: conta.nomeUsuario,
          perfis: conta.perfis.map((perfil) => perfil.codigo),
        });
        this.form.controls.pessoaId.disable();
        this.form.controls.senhaTemporaria.clearValidators();
        this.form.controls.confirmarSenha.clearValidators();
        this.form.controls.senhaTemporaria.updateValueAndValidity();
        this.form.controls.confirmarSenha.updateValueAndValidity();
        if (this.visualizacao()) this.form.disable();
      },
      error: () => this.location.back(),
      complete: () => this.carregando.set(false),
    });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as (ErroApi & { invalidFields?: Record<string, string> }) | undefined;
    corpo?.erros?.forEach(({ campo, mensagem }) => this.form.get(campo)?.setErrors({ backend: mensagem }));
    Object.entries(corpo?.invalidFields ?? {}).forEach(([campo, mensagem]) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });

    if (erro.status === 409) {
      const detalhe = corpo?.detail ?? '';
      if (detalhe.toLocaleLowerCase().includes('pessoa')) {
        this.form.controls.pessoaId.setErrors({
          backend: detalhe || 'Esta pessoa já possui uma conta vinculada.',
        });
      } else {
        this.form.controls.nomeUsuario.setErrors({
          backend: detalhe || 'Este nome de usuário já está sendo utilizado.',
        });
      }
    } else if (!corpo?.erros?.length && !corpo?.invalidFields) {
      this.notificacao.erro(corpo?.detail ?? 'Não foi possível salvar a conta.');
    }
    this.form.markAllAsTouched();
  }
}

function senhasConferem(controle: import('@angular/forms').AbstractControl) {
  return controle.get('senhaTemporaria')?.value === controle.get('confirmarSenha')?.value
    ? null
    : { senhasDiferentes: true };
}

function perfisReserva(): PerfilDisponivel[] {
  return [
    { codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' },
    { codigo: 'GERENCIADOR_DOACOES', descricao: 'Gerenciador de Doações' },
    { codigo: 'GERENCIADOR_CAPACITACOES', descricao: 'Gerenciador de Capacitações' },
    { codigo: 'GERENCIADOR_ACESSO', descricao: 'Gerenciador de Acesso' },
    { codigo: 'GERENCIADOR_RELATORIOS', descricao: 'Gerenciador de Relatórios' },
  ];
}
