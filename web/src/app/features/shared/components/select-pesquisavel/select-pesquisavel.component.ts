import {
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ControlValueAccessor, FormControl, NgControl, ReactiveFormsModule } from '@angular/forms';
import {
  MatAutocompleteModule,
  MatAutocompleteSelectedEvent,
  MatAutocompleteTrigger,
} from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounce, distinctUntilChanged, timer } from 'rxjs';

export interface OpcaoSelectPesquisavel<T = unknown> {
  valor: T;
  rotulo: string;
  subtexto?: string;
  desabilitado?: boolean;
}

function normalizarTexto(texto: string): string {
  return (texto || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

function contemTexto(rotulo: string, subtexto: string | undefined, termo: string): boolean {
  if (!termo) return true;
  const termoNorm = normalizarTexto(termo);
  const rotuloNorm = normalizarTexto(rotulo);
  const subtextoNorm = normalizarTexto(subtexto || '');

  if (rotuloNorm.includes(termoNorm) || subtextoNorm.includes(termoNorm)) {
    return true;
  }

  // Verifica se há correspondência numérica (ex: busca por CPF com ou sem pontuação)
  const termoNumeros = termo.replace(/\D/g, '');
  if (termoNumeros.length >= 3) {
    const rotuloNumeros = rotulo.replace(/\D/g, '');
    const subtextoNumeros = (subtexto || '').replace(/\D/g, '');
    if (rotuloNumeros.includes(termoNumeros) || subtextoNumeros.includes(termoNumeros)) {
      return true;
    }
  }

  return false;
}

@Component({
  selector: 'app-select-pesquisavel',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './select-pesquisavel.component.html',
  styleUrls: ['./select-pesquisavel.component.css'],
})
export class SelectPesquisavelComponent<T = unknown> implements ControlValueAccessor, OnInit {
  protected readonly ngControl = inject(NgControl, { optional: true, self: true });
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChild('inputElement') protected inputRef?: ElementRef<HTMLInputElement>;

  readonly opcoes = input<OpcaoSelectPesquisavel<T>[]>([]);
  readonly placeholder = input<string>('Selecione uma opção');
  readonly carregando = input<boolean>(false);
  readonly permitirLimpar = input<boolean>(true);
  readonly mensagemVazio = input<string>('Nenhum resultado encontrado');
  readonly desabilitado = input<boolean>(false);
  readonly mensagemErro = input<string | null>(null);
  readonly debounceMs = input<number>(300);

  readonly aoPesquisar = output<string>();
  readonly aoSelecionar = output<OpcaoSelectPesquisavel<T> | null>();
  readonly aoLimpar = output<void>();

  protected readonly valorSelecionado = signal<T | null>(null);
  protected readonly termoBusca = signal<string>('');
  protected readonly emFoco = signal<boolean>(false);
  protected readonly desabilitadoPeloFormulario = signal<boolean>(false);

  protected readonly buscaControl = new FormControl<string>('');

  protected readonly desabilitadoEfetivo = computed(
    () => this.desabilitado() || this.desabilitadoPeloFormulario(),
  );

  protected readonly temValor = computed(() => {
    const val = this.valorSelecionado();
    return val !== null && val !== undefined && (val as unknown) !== '' && (val as unknown) !== 0;
  });

  protected readonly opcaoSelecionada = computed(() => {
    const val = this.valorSelecionado();
    if (val === null || val === undefined || (val as unknown) === '' || (val as unknown) === 0) {
      return null;
    }
    return this.opcoes().find((o) => o.valor === val) ?? null;
  });

  protected readonly opcoesFiltradas = computed(() => {
    const todas = this.opcoes();
    const termo = this.termoBusca().trim();
    if (!termo) {
      return todas;
    }
    return todas.filter((opcao) => contemTexto(opcao.rotulo, opcao.subtexto, termo));
  });

  protected readonly tocadoInterno = signal<boolean>(false);

  protected get erroExibido(): string | null {
    const custom = this.mensagemErro();
    if (custom) return custom;

    const ctrl = this.ngControl?.control;
    if (!ctrl || !ctrl.invalid) return null;

    const foiTocado = ctrl.touched || ctrl.dirty || this.tocadoInterno();
    if (!foiTocado) return null;

    if (ctrl.hasError('required') || ctrl.hasError('min')) {
      return 'Campo obrigatório.';
    }
    if (ctrl.hasError('backend')) {
      return ctrl.getError('backend');
    }
    return 'Valor inválido.';
  }

  protected readonly errorStateMatcher: ErrorStateMatcher = {
    isErrorState: () => !!this.erroExibido,
  };

  private onChange: (valor: T | null) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor() {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }

    this.buscaControl.valueChanges
      .pipe(
        debounce(() => timer(this.debounceMs())),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((valor) => {
        const termo = typeof valor === 'string' ? valor : '';
        this.termoBusca.set(termo);
        this.aoPesquisar.emit(termo);
      });

    effect(() => {
      const opcao = this.opcaoSelecionada();
      const rotulo = opcao ? opcao.rotulo : '';
      if (!this.emFoco() && this.buscaControl.value !== rotulo) {
        this.buscaControl.setValue(rotulo, { emitEvent: false });
      }
    });

    effect(() => {
      const isDisabled = this.desabilitadoEfetivo();
      if (isDisabled && this.buscaControl.enabled) {
        this.buscaControl.disable({ emitEvent: false });
      } else if (!isDisabled && this.buscaControl.disabled) {
        this.buscaControl.enable({ emitEvent: false });
      }
    });
  }

  ngOnInit(): void {
    this.ngControl?.statusChanges?.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.cdr.markForCheck();
    });
  }

  // --- ControlValueAccessor ---

  writeValue(valor: T | null): void {
    this.valorSelecionado.set(valor);
    const opcao = this.opcoes().find((o) => o.valor === valor);
    const rotulo = opcao ? opcao.rotulo : '';
    this.buscaControl.setValue(rotulo, { emitEvent: false });
  }

  registerOnChange(fn: (valor: T | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.desabilitadoPeloFormulario.set(isDisabled);
  }

  // --- Handlers & Helpers ---

  protected displayFn = (val: unknown): string => {
    if (val === null || val === undefined) return '';
    if (typeof val === 'string') return val;
    const opcao = this.opcoes().find((o) => o.valor === val);
    return opcao ? opcao.rotulo : '';
  };

  protected onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const valor = event.option.value as T;
    const opcao = this.opcoes().find((o) => o.valor === valor) ?? null;
    this.valorSelecionado.set(valor);
    const rotulo = opcao ? opcao.rotulo : '';
    this.buscaControl.setValue(rotulo, { emitEvent: false });
    this.termoBusca.set('');
    this.onChange(valor);
    this.onTouched();
    this.aoSelecionar.emit(opcao);
  }

  protected onFocus(event: FocusEvent): void {
    this.emFoco.set(true);
    const target = event.target as HTMLInputElement;
    if (target && typeof target.select === 'function') {
      target.select();
    }
  }

  protected onBlur(): void {
    this.emFoco.set(false);
    this.onTouched();

    const valorDigitado = (this.buscaControl.value || '').trim();
    const valorAtual = this.valorSelecionado();

    if (!valorDigitado) {
      if (this.permitirLimpar() && valorAtual !== null) {
        this.limpar();
      } else {
        this.restaurarRotuloSelecionado();
      }
      return;
    }

    const opcaoAtual = this.opcaoSelecionada();
    if (opcaoAtual && normalizarTexto(opcaoAtual.rotulo) === normalizarTexto(valorDigitado)) {
      return;
    }

    const opcaoExata = this.opcoes().find(
      (o) => normalizarTexto(o.rotulo) === normalizarTexto(valorDigitado),
    );
    if (opcaoExata) {
      this.selecionarOpcaoDiretamente(opcaoExata);
      return;
    }

    this.restaurarRotuloSelecionado();
  }

  protected limpar(event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    this.valorSelecionado.set(null);
    this.buscaControl.setValue('', { emitEvent: false });
    this.termoBusca.set('');
    this.onChange(null);
    this.onTouched();
    this.aoLimpar.emit();
    this.aoPesquisar.emit('');
  }

  protected alternarPainel(trigger: MatAutocompleteTrigger, event: MouseEvent): void {
    event.stopPropagation();
    if (this.desabilitadoEfetivo()) return;
    if (trigger.panelOpen) {
      trigger.closePanel();
    } else {
      trigger.openPanel();
    }
  }

  focar(): void {
    this.inputRef?.nativeElement.focus();
  }

  private selecionarOpcaoDiretamente(opcao: OpcaoSelectPesquisavel<T>): void {
    this.valorSelecionado.set(opcao.valor);
    this.buscaControl.setValue(opcao.rotulo, { emitEvent: false });
    this.termoBusca.set('');
    this.onChange(opcao.valor);
    this.onTouched();
    this.aoSelecionar.emit(opcao);
  }

  private restaurarRotuloSelecionado(): void {
    const opcao = this.opcaoSelecionada();
    const rotulo = opcao ? opcao.rotulo : '';
    this.buscaControl.setValue(rotulo, { emitEvent: false });
    this.termoBusca.set('');
  }
}
