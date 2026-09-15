import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import {
  AbstractControl,
  FormGroupDirective,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import {
  ESCOLARIDADE_OPCOES,
  EscolaridadeCodigo,
  ItemRelacionadoResumo,
  PessoaRequisicao,
  RENDA_FAMILIAR_OPCOES,
  RendaFamiliarCodigo,
  SEXO_OPCOES,
  SexoCodigo,
  TipoPessoaCodigo,
  UfCodigo,
} from '../../models/pessoa.model';
import { CepService } from '../../services/cep.service';
import { PessoaService } from '../../services/pessoa.service';

@Component({
  selector: 'app-pessoa-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './pessoa-cadastro-page.component.html',
})
export class PessoaCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly pessoaService = inject(PessoaService);
  private readonly cepService = inject(CepService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly formDirective = viewChild(FormGroupDirective);

  readonly id = input<string>();
  readonly visualizacao = input<boolean>(false);

  protected readonly modoVisualizacao = computed(() => this.visualizacao());
  protected readonly modoEdicao = computed(() => !!this.id() && !this.visualizacao());
  protected readonly modoCriacao = computed(() => !this.id());

  protected readonly sexoOpcoes = SEXO_OPCOES;
  protected readonly escolaridadeOpcoes = ESCOLARIDADE_OPCOES;
  protected readonly rendaFamiliarOpcoes = RENDA_FAMILIAR_OPCOES;
  protected readonly tipoPessoaOpcoes = [
    { codigo: TipoPessoaCodigo.FISICA, descricao: 'Pessoa Física' },
    { codigo: TipoPessoaCodigo.JURIDICA, descricao: 'Pessoa Jurídica' },
  ];
  protected readonly ufOpcoes = Object.values(UfCodigo);

  protected readonly comorbidades = signal<ItemRelacionadoResumo[]>([]);
  protected readonly categorias = signal<ItemRelacionadoResumo[]>([]);
  protected readonly carregando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/)]],
    cpfCnpj: ['', [Validators.required, documentoValido]],
    tipoPessoa: [TipoPessoaCodigo.FISICA as TipoPessoaCodigo | null],
    dataNascimento: ['', [dataNascimentoValida]],
    sexo: [null as SexoCodigo | null],
    telefone: ['', [Validators.required, telefoneValido]],
    email: ['', [Validators.email]],
    escolaridade: [null as EscolaridadeCodigo | null],
    profissao: [''],
    rendaFamiliar: [null as RendaFamiliarCodigo | null],
    comorbidades: [[] as number[]],
    categorias: [[] as number[]],
    descricao: [''],
    cep: [''],
    uf: [null as UfCodigo | null],
    cidade: [''],
    bairro: [''],
    logradouro: [''],
    complementoEndereco: [''],
    quantidadeCoabitantes: [0],
    ehBeneficiario: [false],
    ehDoador: [false],
  });

  constructor() {
    this.carregarOpcoes();

    effect(() => {
      const idPessoa = this.id();
      if (idPessoa) {
        this.carregarPessoa(Number(idPessoa));
      } else {
        this.form.reset();
      }
    });
  }

  protected salvar(): void {
    if (this.modoVisualizacao()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacaoService.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    const requisicao = this.montarRequisicao();
    const operacao$ = this.modoEdicao()
      ? this.pessoaService.atualizar(Number(this.id()), requisicao)
      : this.pessoaService.cadastrar(requisicao);

    operacao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(
          `Pessoa ${this.modoEdicao() ? 'atualizada' : 'cadastrada'} com sucesso!`,
        );
        this.limparFormulario();
        this.voltarParaLista();
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected cancelar(): void {
    this.limparFormulario();
    this.voltarParaLista();
  }

  private voltarParaLista(): void {
    this.router.navigate(['/pessoas'], {
      queryParams: this.route.snapshot.queryParams,
    });
  }

  /*
   * resetForm() zera tambem o estado "submitted" do formulario.
   * Usar apenas form.reset() deixaria os campos obrigatorios vazios
   * marcados em vermelho logo apos um cadastro bem-sucedido.
   */
  private limparFormulario(): void {
    this.formDirective()?.resetForm();
  }

  protected aplicarMascaraCpfCnpj(): void {
    const digitos = somenteDigitos(this.form.controls.cpfCnpj.value).slice(0, 14);
    let formatado = digitos;

    if (digitos.length > 12) {
      formatado = `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5, 8)}/${digitos.slice(8, 12)}-${digitos.slice(12)}`;
    } else if (digitos.length > 9 && digitos.length <= 11) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
    } else if (digitos.length > 6) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
    } else if (digitos.length > 3) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
    }

    this.form.controls.cpfCnpj.setValue(formatado, { emitEvent: false });
  }

  protected aplicarMascaraCep(): void {
    const digitos = somenteDigitos(this.form.controls.cep.value).slice(0, 8);
    const formatado = digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
    this.form.controls.cep.setValue(formatado, { emitEvent: false });
  }

  protected consultarCep(): void {
    const cep = somenteDigitos(this.form.controls.cep.value);
    if (cep.length !== 8 || this.modoVisualizacao()) return;

    this.carregando.set(true);
    this.cepService.buscar(cep).subscribe({
      next: (endereco) => this.form.patchValue({
        cep: endereco.cep,
        logradouro: endereco.logradouro,
        complementoEndereco: endereco.complemento,
        bairro: endereco.bairro,
        cidade: endereco.cidade,
        uf: endereco.uf,
      }),
      error: () => this.form.controls.cep.setErrors({ cepNaoEncontrado: true }),
      complete: () => this.carregando.set(false),
    });
  }

  protected aplicarMascaraTelefone(): void {
    const digitos = somenteDigitos(this.form.controls.telefone.value).slice(0, 11);
    let formatado = digitos;

    if (digitos.length > 6) {
      const corte = digitos.length > 10 ? 7 : 6;
      formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2, corte)}-${digitos.slice(corte)}`;
    } else if (digitos.length > 2) {
      formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
    } else if (digitos.length > 0) {
      formatado = `(${digitos}`;
    }

    this.form.controls.telefone.setValue(formatado, { emitEvent: false });
  }

  protected aplicarMascaraData(): void {
    const digitos = somenteDigitos(this.form.controls.dataNascimento.value).slice(0, 8);
    let formatado = digitos;

    if (digitos.length > 4) {
      formatado = `${digitos.slice(0, 2)}/${digitos.slice(2, 4)}/${digitos.slice(4)}`;
    } else if (digitos.length > 2) {
      formatado = `${digitos.slice(0, 2)}/${digitos.slice(2)}`;
    }

    this.form.controls.dataNascimento.setValue(formatado, { emitEvent: false });
  }

  private carregarOpcoes(): void {
    this.pessoaService.listarComorbidades().subscribe({
      next: (itens) => this.comorbidades.set(itens),
    });

    this.pessoaService.listarCategorias().subscribe({
      next: (itens) => this.categorias.set(itens),
    });
  }

  private carregarPessoa(id: number): void {
    this.carregando.set(true);
    this.pessoaService.buscarPorId(id).subscribe({
      next: (pessoa) => {
        this.form.patchValue({
          nome: pessoa.nome,
          cpfCnpj: pessoa.cpfCnpj ?? pessoa.cpf,
          tipoPessoa: pessoa.tipoPessoa as TipoPessoaCodigo ?? TipoPessoaCodigo.FISICA,
          dataNascimento: pessoa.dataNascimento?.replace(/-/g, '/') ?? '',
          sexo: pessoa.sexo?.codigo ?? null,
          telefone: pessoa.telefone,
          email: pessoa.email ?? '',
          escolaridade: pessoa.escolaridade?.codigo ?? null,
          profissao: pessoa.profissao ?? '',
          rendaFamiliar: pessoa.rendaFamiliar?.codigo ?? null,
          comorbidades: pessoa.comorbidades ?? [],
          categorias: pessoa.categorias ?? [],
          descricao: pessoa.descricao ?? '',
          cep: pessoa.cep ?? '',
          uf: pessoa.uf ?? null,
          cidade: pessoa.cidade ?? '',
          bairro: pessoa.bairro ?? '',
          logradouro: pessoa.logradouro ?? '',
          complementoEndereco: pessoa.complementoEndereco ?? '',
          quantidadeCoabitantes: pessoa.quantidadeCoabitantes ?? 0,
          ehBeneficiario: pessoa.ehBeneficiario ?? false,
          ehDoador: pessoa.ehDoador ?? false,
        });

        if (this.modoVisualizacao()) {
          this.form.disable();
        }
      },
      error: () => this.voltarParaLista(),
      complete: () => this.carregando.set(false),
    });
  }

  private montarRequisicao(): PessoaRequisicao {
    const valores = this.form.getRawValue();

    return {
      nome: valores.nome.trim(),
      cpfCnpj: somenteDigitos(valores.cpfCnpj),
      tipoPessoa: valores.tipoPessoa ?? undefined,
      // A tela usa dd/mm/aaaa; o contrato da API exige dd-mm-aaaa.
      dataNascimento: valores.dataNascimento.replace(/\//g, '-'),
      sexo: valores.sexo ?? undefined,
      telefone: somenteDigitos(valores.telefone),
      email: valores.email.trim() || undefined,
      escolaridade: valores.escolaridade ?? undefined,
      profissao: valores.profissao.trim() || undefined,
      rendaFamiliar: valores.rendaFamiliar ?? undefined,
      comorbidades: valores.comorbidades.length > 0 ? valores.comorbidades : undefined,
      categorias: valores.categorias.length > 0 ? valores.categorias : undefined,
      descricao: valores.descricao.trim() || undefined,
      cep: somenteDigitos(valores.cep) || undefined,
      uf: valores.uf ?? undefined,
      cidade: valores.cidade.trim() || undefined,
      bairro: valores.bairro.trim() || undefined,
      logradouro: valores.logradouro.trim() || undefined,
      complementoEndereco: valores.complementoEndereco.trim() || undefined,
      quantidadeCoabitantes: valores.quantidadeCoabitantes,
      ehBeneficiario: valores.ehBeneficiario,
      ehDoador: valores.ehDoador,
    };
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    corpo?.erros?.forEach(({ campo, mensagem }) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });

    if (erro.status === 409) {
      this.form.controls.cpfCnpj.setErrors({
        backend: corpo?.detail ?? 'Já existe uma pessoa cadastrada com este CPF.',
      });
    }

    this.form.markAllAsTouched();
  }
}

function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

function documentoValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(String(control.value ?? ''));

  if (!digitos) {
    return null;
  }

  if (digitos.length !== 11 && digitos.length !== 14) {
    return { documentoIncompleto: true };
  }

  if (/^(\d)\1+$/.test(digitos)) {
    return { documentoInvalido: true };
  }

  if (digitos.length === 14) return null;

  const digitoVerificador = (quantidade: number): number => {
    let soma = 0;

    for (let posicao = 0; posicao < quantidade; posicao++) {
      soma += Number(digitos[posicao]) * (quantidade + 1 - posicao);
    }

    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };

  const primeiroValido = digitoVerificador(9) === Number(digitos[9]);
  const segundoValido = digitoVerificador(10) === Number(digitos[10]);

  return primeiroValido && segundoValido ? null : { documentoInvalido: true };
}

function dataNascimentoValida(control: AbstractControl): ValidationErrors | null {
  const valor = String(control.value ?? '');

  if (!valor) {
    return null;
  }

  const partes = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(valor);

  if (!partes) {
    return { dataFormato: true };
  }

  const dia = Number(partes[1]);
  const mes = Number(partes[2]);
  const ano = Number(partes[3]);
  const data = new Date(ano, mes - 1, dia);

  const existeNoCalendario =
    data.getFullYear() === ano && data.getMonth() === mes - 1 && data.getDate() === dia;

  if (!existeNoCalendario || ano < 1900) {
    return { dataInvalida: true };
  }

  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);

  return data > hoje ? { dataFutura: true } : null;
}

function telefoneValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(String(control.value ?? ''));

  if (!digitos) {
    return null;
  }

  return /^\d{10,11}$/.test(digitos) ? null : { telefoneInvalido: true };
}
