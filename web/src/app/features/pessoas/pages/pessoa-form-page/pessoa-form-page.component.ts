import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal, viewChild } from '@angular/core';
import { finalize } from 'rxjs';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { ErroApi } from '@features/shared/models/erro-api.model';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { CategoriaService } from '../../services/categoria.service';
import { ComorbidadeService } from '../../services/comorbidade.service';
import { PessoaService } from '../../services/pessoa.service';
import {
  ESCOLARIDADE_OPCOES,
  EscolaridadeCodigo,
  ItemRelacionadoResumo,
  PessoaRequisicao,
  RENDA_FAMILIAR_OPCOES,
  RendaFamiliarCodigo,
  SEXO_OPCOES,
  SexoCodigo,
  TipoPessoa,
  UF_OPCOES,
  UfCodigo,
} from '../../models/pessoa.model';

@Component({
  selector: 'app-pessoa-form-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './pessoa-form-page.component.html',
})
export class PessoaFormPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly pessoaService = inject(PessoaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly comorbidadeService = inject(ComorbidadeService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly formDirective = viewChild(FormGroupDirective);

  protected readonly sexoOpcoes = SEXO_OPCOES;
  protected readonly escolaridadeOpcoes = ESCOLARIDADE_OPCOES;
  protected readonly rendaFamiliarOpcoes = RENDA_FAMILIAR_OPCOES;
  protected readonly ufOpcoes = UF_OPCOES;

  protected readonly ehPessoaJuridica = signal<boolean>(false);
  protected readonly comorbidades = signal<ItemRelacionadoResumo[]>([]);
  protected readonly categorias = signal<ItemRelacionadoResumo[]>([]);
  protected readonly carregando = signal<boolean>(false);
  protected readonly buscandoCep = signal<boolean>(false);

  private ultimoCepBuscado = '';

  protected readonly form = this.fb.group({
    ehPessoaJuridica: [false],
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    cpfCnpj: ['', [Validators.required, cpfValido]],
    dataNascimento: ['', [Validators.required, dataNascimentoValida]],
    sexo: [null as SexoCodigo | null],
    telefone: ['', [Validators.required, telefoneValido]],
    email: ['', [Validators.email, Validators.maxLength(50)]],
    escolaridade: [null as EscolaridadeCodigo | null],
    profissao: ['', [Validators.maxLength(50)]],
    rendaFamiliar: [null as RendaFamiliarCodigo | null],
    comorbidades: [[] as number[]],
    categorias: [[] as number[]],
    descricao: [''],
    // Endereço
    cep: ['', [cepValido]],
    uf: [null as UfCodigo | string | null],
    cidade: ['', [Validators.maxLength(100)]],
    bairro: ['', [Validators.maxLength(50)]],
    logradouro: ['', [Validators.maxLength(100)]],
    complementoEndereco: ['', [Validators.maxLength(100)]],
    quantidadeCoabitantes: [null as number | null, [Validators.min(0), Validators.max(127)]],
    ehBeneficiario: [false],
    ehDoador: [false],
  });

  constructor() {
    this.carregarOpcoes();
    this.configurarAlternanciaTipoPessoa();
  }

  private configurarAlternanciaTipoPessoa(): void {
    this.form.controls.ehPessoaJuridica.valueChanges.subscribe((ehJuridica) => {
      this.ehPessoaJuridica.set(ehJuridica);
      this.atualizarValidadores(ehJuridica);
      this.aplicarMascaraDocumento();
    });
  }

  private atualizarValidadores(ehJuridica: boolean): void {
    const {
      cpfCnpj,
      dataNascimento,
      sexo,
      escolaridade,
      profissao,
      rendaFamiliar,
      comorbidades,
      quantidadeCoabitantes,
      ehBeneficiario,
    } = this.form.controls;

    if (ehJuridica) {
      cpfCnpj.setValidators([Validators.required, cnpjValido]);
      dataNascimento.clearValidators();
      dataNascimento.setValue('', { emitEvent: false });
      sexo.setValue(null, { emitEvent: false });
      escolaridade.setValue(null, { emitEvent: false });
      profissao.setValue('', { emitEvent: false });
      rendaFamiliar.setValue(null, { emitEvent: false });
      comorbidades.setValue([], { emitEvent: false });
      quantidadeCoabitantes.setValue(null, { emitEvent: false });
      ehBeneficiario.setValue(false, { emitEvent: false });
    } else {
      cpfCnpj.setValidators([Validators.required, cpfValido]);
      dataNascimento.setValidators([Validators.required, dataNascimentoValida]);
    }

    cpfCnpj.updateValueAndValidity({ emitEvent: false });
    dataNascimento.updateValueAndValidity({ emitEvent: false });
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notificacaoService.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    this.pessoaService.cadastrar(this.montarRequisicao()).subscribe({
      next: () => {
        const tipoDesc = this.ehPessoaJuridica() ? 'Pessoa Jurídica' : 'Pessoa';
        this.notificacaoService.sucesso(`${tipoDesc} cadastrada com sucesso!`);
        this.limparFormulario();
        this.router.navigate(['/pessoas']);
      },
      error: (erro: HttpErrorResponse) => this.tratarErro(erro),
      complete: () => this.carregando.set(false),
    });
  }

  protected cancelar(): void {
    this.limparFormulario();
    this.router.navigate(['/pessoas']);
  }

  private limparFormulario(): void {
    this.ultimoCepBuscado = '';
    this.formDirective()?.resetForm({
      ehPessoaJuridica: false,
      ehBeneficiario: false,
      ehDoador: false,
      comorbidades: [],
      categorias: [],
    });
    this.ehPessoaJuridica.set(false);
    this.atualizarValidadores(false);
  }

  protected aplicarMascaraDocumento(): void {
    const isPj = this.ehPessoaJuridica();
    const maxDigitos = isPj ? 14 : 11;
    const digitos = somenteDigitos(this.form.controls.cpfCnpj.value).slice(0, maxDigitos);
    let formatado = digitos;

    if (isPj) {
      // 00.000.000/0000-00
      if (digitos.length > 12) {
        formatado = `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5, 8)}/${digitos.slice(8, 12)}-${digitos.slice(12)}`;
      } else if (digitos.length > 8) {
        formatado = `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5, 8)}/${digitos.slice(8)}`;
      } else if (digitos.length > 5) {
        formatado = `${digitos.slice(0, 2)}.${digitos.slice(2, 5)}.${digitos.slice(5)}`;
      } else if (digitos.length > 2) {
        formatado = `${digitos.slice(0, 2)}.${digitos.slice(2)}`;
      }
    } else {
      // 000.000.000-00
      if (digitos.length > 9) {
        formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
      } else if (digitos.length > 6) {
        formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
      } else if (digitos.length > 3) {
        formatado = `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
      }
    }

    this.form.controls.cpfCnpj.setValue(formatado, { emitEvent: false });
  }

  protected aplicarMascaraCep(): void {
    const digitos = somenteDigitos(this.form.controls.cep.value).slice(0, 8);
    if (digitos.length === 0) {
      this.ultimoCepBuscado = '';
    }
    let formatado = digitos;

    if (digitos.length > 5) {
      formatado = `${digitos.slice(0, 5)}-${digitos.slice(5)}`;
    }

    this.form.controls.cep.setValue(formatado, { emitEvent: false });

    if (digitos.length === 8) {
      this.buscarCep(digitos);
    }
  }

  protected buscarCep(cepDigitos: string): void {
    if (cepDigitos.length !== 8 || this.ultimoCepBuscado === cepDigitos) return;
    this.ultimoCepBuscado = cepDigitos;

    this.buscandoCep.set(true);
    this.pessoaService
      .consultarCep(cepDigitos)
      .pipe(finalize(() => this.buscandoCep.set(false)))
      .subscribe({
        next: (dados) => {
          if (dados) {
            if (dados.logradouro) {
              this.form.controls.logradouro.setValue(dados.logradouro);
            }
            if (dados.bairro) {
              this.form.controls.bairro.setValue(dados.bairro);
            }
            if (dados.cidade) {
              this.form.controls.cidade.setValue(dados.cidade);
            }
            if (dados.uf) {
              const codigoUf =
                typeof dados.uf === 'object' && dados.uf !== null
                  ? ((dados.uf as { codigo?: string }).codigo ?? String(dados.uf))
                  : String(dados.uf);
              this.form.controls.uf.setValue(codigoUf.toUpperCase());
            }
            if (dados.complemento && !this.form.controls.complementoEndereco.value) {
              this.form.controls.complementoEndereco.setValue(dados.complemento);
            }
          }
        },
        error: () => {
          // Conforme especificado: não mostrar mensagem de erro caso ocorra um
        },
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
    this.comorbidadeService.listarTodas(false).subscribe({
      next: (itens) => this.comorbidades.set(itens.map((i) => ({ id: i.id, nome: i.nome }))),
      error: () => {
        this.pessoaService.listarComorbidades().subscribe({
          next: (itens) => this.comorbidades.set(itens),
        });
      },
    });

    this.categoriaService.listarTodas(false).subscribe({
      next: (itens) => this.categorias.set(itens.map((i) => ({ id: i.id, nome: i.nome }))),
      error: () => {
        this.pessoaService.listarCategorias().subscribe({
          next: (itens) => this.categorias.set(itens),
        });
      },
    });
  }

  private montarRequisicao(): PessoaRequisicao {
    const valores = this.form.getRawValue();
    const ehJuridica = Boolean(valores.ehPessoaJuridica);

    const requisicao: PessoaRequisicao = {
      nome: valores.nome.trim(),
      cpfCnpj: somenteDigitos(valores.cpfCnpj),
      tipoPessoa: ehJuridica ? TipoPessoa.JURIDICA : TipoPessoa.FISICA,
      telefone: somenteDigitos(valores.telefone),
      email: valores.email.trim() || undefined,
      categorias: valores.categorias.length > 0 ? valores.categorias : undefined,
      descricao: valores.descricao.trim() || undefined,
      cep: somenteDigitos(valores.cep) || undefined,
      uf: valores.uf || undefined,
      cidade: valores.cidade.trim() || undefined,
      bairro: valores.bairro.trim() || undefined,
      logradouro: valores.logradouro.trim() || undefined,
      complementoEndereco: valores.complementoEndereco.trim() || undefined,
      ehDoador: Boolean(valores.ehDoador),
    };

    if (ehJuridica) {
      requisicao.ehBeneficiario = false;
    } else {
      requisicao.dataNascimento = valores.dataNascimento.replace(/\//g, '-');
      requisicao.sexo = valores.sexo ?? undefined;
      requisicao.escolaridade = valores.escolaridade ?? undefined;
      requisicao.profissao = valores.profissao.trim() || undefined;
      requisicao.rendaFamiliar = valores.rendaFamiliar ?? undefined;
      requisicao.comorbidades = valores.comorbidades.length > 0 ? valores.comorbidades : undefined;
      requisicao.quantidadeCoabitantes =
        valores.quantidadeCoabitantes !== null && valores.quantidadeCoabitantes !== undefined
          ? Number(valores.quantidadeCoabitantes)
          : undefined;
      requisicao.ehBeneficiario = Boolean(valores.ehBeneficiario);
    }

    return requisicao;
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    corpo?.erros?.forEach(({ campo, mensagem }) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });

    if (erro.status === 409) {
      const detail = corpo?.detail ?? '';
      if (detail.toLowerCase().includes('e-mail') || detail.toLowerCase().includes('email')) {
        this.form.controls.email.setErrors({
          backend: detail || 'Já existe uma pessoa cadastrada com este e-mail.',
        });
      } else {
        const rotulo = this.ehPessoaJuridica() ? 'CNPJ' : 'CPF';
        this.form.controls.cpfCnpj.setErrors({
          backend: detail || `Já existe um cadastro com este ${rotulo}.`,
        });
      }
      this.notificacaoService.alerta(
        corpo?.detail ?? 'Já existe um cadastro com os dados informados.',
      );
    }

    this.form.markAllAsTouched();
  }
}

export function somenteDigitos(valor: string | null | undefined): string {
  return String(valor ?? '').replace(/\D/g, '');
}

export function cpfValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(control.value);

  if (!digitos) {
    return null;
  }

  if (digitos.length !== 11) {
    return { cpfIncompleto: true };
  }

  if (/^(\d)\1{10}$/.test(digitos)) {
    return { cpfInvalido: true };
  }

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

  return primeiroValido && segundoValido ? null : { cpfInvalido: true };
}

export function cnpjValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(control.value);

  if (!digitos) {
    return null;
  }

  if (digitos.length !== 14) {
    return { cnpjIncompleto: true };
  }

  if (/^(\d)\1{13}$/.test(digitos)) {
    return { cnpjInvalido: true };
  }

  const calcularDigito = (tamanho: number): number => {
    let soma = 0;
    let pos = tamanho - 7;
    for (let i = tamanho; i >= 1; i--) {
      soma += Number(digitos.charAt(tamanho - i)) * pos--;
      if (pos < 2) pos = 9;
    }
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };

  const primeiroValido = calcularDigito(12) === Number(digitos.charAt(12));
  const segundoValido = calcularDigito(13) === Number(digitos.charAt(13));

  return primeiroValido && segundoValido ? null : { cnpjInvalido: true };
}

export function dataNascimentoValida(control: AbstractControl): ValidationErrors | null {
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

export function telefoneValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(control.value);

  if (!digitos) {
    return null;
  }

  return /^\d{10,11}$/.test(digitos) ? null : { telefoneInvalido: true };
}

export function cepValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(control.value);

  if (!digitos) {
    return null;
  }

  return digitos.length === 8 ? null : { cepIncompleto: true };
}

// Alias para manter retrocompatibilidade
export { PessoaFormPageComponent as PessoaCadastroPageComponent };
