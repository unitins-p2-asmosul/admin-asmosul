import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal, viewChild } from '@angular/core';
import {
  AbstractControl,
  FormGroupDirective,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
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
} from '../../models/pessoa.model';
import { PessoaService } from '../../services/pessoa.service';

@Component({
  selector: 'app-pessoa-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './pessoa-cadastro-page.component.html',
  styles: `
    /*
     * Ajustes pontuais para aproximar do protótipo do Figma.
     * Usa apenas os tokens públicos do Angular Material 21 (sem ::ng-deep),
     * escopados no :host para não vazar para outras telas.
     */
    :host {
      --asmosul-navy: #14395c;

      --mat-form-field-container-height: 48px;
      --mat-form-field-container-vertical-padding: 12px;
      --mat-form-field-outlined-container-shape: 8px;
      --mat-form-field-outlined-outline-color: #d9dee5;
      --mat-form-field-outlined-hover-outline-color: var(--asmosul-navy);
      --mat-form-field-outlined-focus-outline-color: var(--asmosul-navy);

      --mat-button-filled-container-shape: 8px;
      --mat-button-filled-container-height: 44px;
    }

    h1,
    h2 {
      color: var(--asmosul-navy);
    }

    button[type='submit'] {
      --mat-button-filled-container-color: var(--asmosul-navy);
      --mat-button-filled-label-text-color: #ffffff;
    }

    button[type='button'] {
      --mat-button-filled-container-color: #e9eef4;
      --mat-button-filled-label-text-color: var(--asmosul-navy);
    }

    /* O min-width do Material vence as classes do Tailwind, entao vai aqui. */
    @media (min-width: 640px) {
      button[mat-flat-button] {
        min-width: 10rem;
      }
    }
  `,
})
export class PessoaCadastroPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly pessoaService = inject(PessoaService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly formDirective = viewChild(FormGroupDirective);

  protected readonly sexoOpcoes = SEXO_OPCOES;
  protected readonly escolaridadeOpcoes = ESCOLARIDADE_OPCOES;
  protected readonly rendaFamiliarOpcoes = RENDA_FAMILIAR_OPCOES;

  protected readonly comorbidades = signal<ItemRelacionadoResumo[]>([]);
  protected readonly categorias = signal<ItemRelacionadoResumo[]>([]);
  protected readonly carregando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/)]],
    cpf: ['', [Validators.required, cpfValido]],
    dataNascimento: ['', [Validators.required, dataNascimentoValida]],
    sexo: [null as SexoCodigo | null],
    telefone: ['', [Validators.required, telefoneValido]],
    email: ['', [Validators.email]],
    escolaridade: [null as EscolaridadeCodigo | null],
    profissao: [''],
    rendaFamiliar: [null as RendaFamiliarCodigo | null],
    comorbidades: [[] as number[]],
    categorias: [[] as number[]],
    descricao: [''],
  });

  constructor() {
    this.carregarOpcoes();
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
        this.notificacaoService.sucesso('Pessoa cadastrada com sucesso!');
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

  /*
   * resetForm() zera tambem o estado "submitted" do formulario.
   * Usar apenas form.reset() deixaria os campos obrigatorios vazios
   * marcados em vermelho logo apos um cadastro bem-sucedido.
   */
  private limparFormulario(): void {
    this.formDirective()?.resetForm();
  }

  protected aplicarMascaraCpf(): void {
    const digitos = somenteDigitos(this.form.controls.cpf.value).slice(0, 11);
    let formatado = digitos;

    if (digitos.length > 9) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
    } else if (digitos.length > 6) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
    } else if (digitos.length > 3) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
    }

    this.form.controls.cpf.setValue(formatado, { emitEvent: false });
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

  private montarRequisicao(): PessoaRequisicao {
    const valores = this.form.getRawValue();

    return {
      nome: valores.nome.trim(),
      cpf: somenteDigitos(valores.cpf),
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
    };
  }

  private tratarErro(erro: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = erro.error as ErroApi | undefined;

    corpo?.erros?.forEach(({ campo, mensagem }) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });

    if (erro.status === 409) {
      this.form.controls.cpf.setErrors({
        backend: corpo?.detail ?? 'Já existe uma pessoa cadastrada com este CPF.',
      });
    }

    this.form.markAllAsTouched();
  }
}

function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

function cpfValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(String(control.value ?? ''));

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
