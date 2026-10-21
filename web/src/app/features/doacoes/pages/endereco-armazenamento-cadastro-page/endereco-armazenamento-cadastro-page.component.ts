import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { CepService } from '@features/pessoas/services/cep.service';
import { UF_OPCOES, UfCodigo } from '@features/pessoas/models/pessoa.model';
import { EMPTY } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  switchMap,
  tap,
} from 'rxjs/operators';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import {
  EnderecoArmazenamentoDetalhe,
  EnderecoArmazenamentoRequisicao,
} from '../../models/endereco-armazenamento.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';

@Component({
  selector: 'app-endereco-armazenamento-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './endereco-armazenamento-cadastro-page.component.html',
})
export class EnderecoArmazenamentoCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly enderecoService = inject(EnderecoArmazenamentoService);
  private readonly cepService = inject(CepService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);

  readonly id = input<string>();
  readonly visualizacao = input(false);

  protected readonly ufOpcoes = UF_OPCOES;
  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());
  protected readonly carregando = signal(false);
  protected readonly buscandoCep = signal(false);
  protected readonly registro = signal<EnderecoArmazenamentoDetalhe | null>(null);

  protected readonly form = this.fb.group({
    cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]],
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    uf: ['', Validators.required],
    cidade: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    bairro: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(50)]],
    logradouro: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    numero: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20)]],
    complemento: ['', Validators.maxLength(100)],
    informacoesAdicionais: [''],
  });

  constructor() {
    this.configurarBuscaCep();
    effect(() => {
      const idEndereco = this.id();
      if (idEndereco) this.carregar(Number(idEndereco));
    });
  }

  protected get titulo(): string {
    if (this.visualizacao()) return 'Detalhes do endereço de armazenamento';
    return this.modoEdicao()
      ? 'Editar endereço de armazenamento'
      : 'Cadastrar endereço de armazenamento';
  }

  protected get descricao(): string {
    if (this.visualizacao()) return 'Consulte os dados deste endereço de armazenamento.';
    return this.modoEdicao()
      ? 'Atualize os dados do endereço de armazenamento.'
      : 'Informe o CEP para preencher automaticamente os dados do endereço.';
  }

  protected formatarCep(): void {
    const digitos = this.form.controls.cep.value.replace(/\D/g, '').slice(0, 8);
    const formatado = digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
    if (formatado !== this.form.controls.cep.value) {
      this.form.controls.cep.setValue(formatado, { emitEvent: false });
    }
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
    const requisicao: EnderecoArmazenamentoRequisicao = {
      nome: valores.nome.trim(),
      cep: valores.cep.trim(),
      uf: valores.uf as UfCodigo,
      cidade: valores.cidade.trim(),
      bairro: valores.bairro.trim(),
      logradouro: valores.logradouro.trim(),
      numero: valores.numero.trim(),
      complemento: valores.complemento.trim() || undefined,
      informacoesAdicionais: valores.informacoesAdicionais.trim() || undefined,
    };
    const operacao = this.modoEdicao()
      ? this.enderecoService.atualizar(Number(this.id()), requisicao)
      : this.enderecoService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Endereço de armazenamento ${this.modoEdicao() ? 'atualizado' : 'cadastrado'} com sucesso.`,
        );
        this.voltar();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
    });
  }

  protected voltar(): void {
    this.location.back();
  }

  protected editar(): void {
    this.router.navigate(['/doacoes/enderecos', this.id(), 'editar'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  protected async alternarEstado(): Promise<void> {
    const endereco = this.registro();
    if (!endereco) return;

    const reativar = !endereco.ativo;
    const confirmou = await this.confirmacao.confirmar(
      `${reativar ? 'Reativar' : 'Inativar'} endereço de armazenamento`,
      `Tem certeza que deseja ${reativar ? 'reativar' : 'inativar'} o endereço "${endereco.nome}"?`,
    );
    if (!confirmou) return;

    this.carregando.set(true);
    const operacao = reativar
      ? this.enderecoService.reativar(endereco.id)
      : this.enderecoService.desativar(endereco.id);
    operacao.pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: () => {
        this.notificacao.sucesso(
          `Endereço de armazenamento ${reativar ? 'reativado' : 'inativado'} com sucesso.`,
        );
        this.carregar(endereco.id);
      },
      error: (erro: HttpErrorResponse) => {
        this.notificacao.erro(
          this.mensagemErro(
            erro,
            'Não foi possível alterar o status do endereço de armazenamento.',
          ),
        );
      },
    });
  }

  private configurarBuscaCep(): void {
    this.form.controls.cep.valueChanges
      .pipe(
        map((cep) => cep.replace(/\D/g, '')),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((cep) => {
          if (cep.length !== 8) return EMPTY;
          this.buscandoCep.set(true);
          return this.cepService.buscar(cep).pipe(
            tap((dados) => this.preencherEndereco(dados)),
            catchError((erro: HttpErrorResponse) => {
              this.notificacao.erro(
                this.mensagemErro(erro, 'Não foi possível consultar o CEP informado.'),
              );
              return EMPTY;
            }),
            finalize(() => this.buscandoCep.set(false)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe();
  }

  private preencherEndereco(dados: {
    cep: string;
    uf: string;
    cidade: string;
    bairro: string;
    logradouro: string;
    complemento: string;
  }): void {
    const cepDigitos = dados.cep.replace(/\D/g, '');
    this.form.patchValue({
      cep:
        cepDigitos.length === 8
          ? `${cepDigitos.slice(0, 5)}-${cepDigitos.slice(5)}`
          : this.form.controls.cep.value,
      uf: typeof dados.uf === 'string' ? dados.uf.toUpperCase() : String(dados.uf),
      cidade: dados.cidade ?? '',
      bairro: dados.bairro ?? '',
      logradouro: dados.logradouro ?? '',
      complemento: dados.complemento ?? '',
    });
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.enderecoService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (endereco) => {
          this.registro.set(endereco);
          this.form.patchValue(
            {
              cep: endereco.cep,
              nome: endereco.nome,
              uf: typeof endereco.uf === 'string' ? endereco.uf : endereco.uf.codigo,
              cidade: endereco.cidade,
              bairro: endereco.bairro,
              logradouro: endereco.logradouro,
              numero: endereco.numero,
              complemento: endereco.complemento ?? '',
              informacoesAdicionais: endereco.informacoesAdicionais ?? '',
            },
            { emitEvent: false },
          );
          if (this.visualizacao() || !endereco.ativo) this.form.disable();
        },
        error: (erro: HttpErrorResponse) => {
          this.notificacao.erro(
            this.mensagemErro(erro, 'Não foi possível carregar o endereço de armazenamento.'),
          );
          this.router.navigate(['/doacoes/enderecos']);
        },
      });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as {
      detail?: string;
      erros?: { campo: string; mensagem: string }[];
    } | null;
    for (const campo of corpo?.erros ?? []) {
      this.form.get(campo.campo)?.setErrors({ backend: campo.mensagem });
    }

    if (erro.status === 409) {
      const mensagem = corpo?.detail || 'Já existe um endereço de armazenamento com este nome.';
      this.form.controls.nome.setErrors({ backend: mensagem });
      this.notificacao.alerta(mensagem);
    } else if (erro.status !== 400 || !corpo?.erros?.length) {
      this.notificacao.erro(
        this.mensagemErro(erro, 'Não foi possível salvar o endereço de armazenamento.'),
      );
    }
    this.form.markAllAsTouched();
  }

  private mensagemErro(erro: HttpErrorResponse, padrao: string): string {
    const corpo = erro.error as { detail?: string } | null;
    return corpo?.detail || padrao;
  }
}
