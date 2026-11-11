import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ControlValueAccessor,
  FormControl,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import {
  MatAutocompleteModule,
  MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';

export interface SelectPesquisavelOpcao<T = number> {
  valor: T;
  rotulo: string;
  textoBusca?: string;
}

@Component({
  selector: 'app-select-pesquisavel',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: SelectPesquisavelComponent,
      multi: true,
    },
  ],
  template: `
    <mat-form-field appearance="outline" class="w-full">
      <input
        matInput
        [id]="id() ? id() + '-input' : ''"
        [errorStateMatcher]="errorStateMatcher"
        [formControl]="textoControl"
        [matAutocomplete]="auto"
        [placeholder]="placeholder()"
        [attr.aria-label]="ariaLabel()"
        [disabled]="desabilitado()"
        autocomplete="off"
        (blur)="marcarComoTocado()"
      />
      @if (textoControl.value || valorAtual() !== null) {
        <button
          mat-icon-button
          matSuffix
          type="button"
          aria-label="Limpar seleção"
          [disabled]="desabilitado()"
          (click)="limpar($event)"
        >
          <mat-icon>close</mat-icon>
        </button>
      }
      <mat-autocomplete
        #auto="matAutocomplete"
        [displayWith]="exibirOpcao"
        (optionSelected)="selecionar($event)"
      >
        @if (carregando()) {
          <mat-option disabled>
            <mat-spinner diameter="20" />
            <span class="ml-2">Carregando...</span>
          </mat-option>
        } @else if (opcoesFiltradas().length === 0) {
          <mat-option disabled>Nenhum resultado encontrado</mat-option>
        } @else {
          @for (opcao of opcoesFiltradas(); track opcao.valor) {
            <mat-option [value]="opcao">{{ opcao.rotulo }}</mat-option>
          }
        }
      </mat-autocomplete>
      @if (mensagemErro()) {
        <mat-error>{{ mensagemErro() }}</mat-error>
      }
    </mat-form-field>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectPesquisavelComponent<T = number> implements ControlValueAccessor {
  readonly id = input('');
  readonly ariaLabel = input('Pesquisar opções');
  readonly placeholder = input('Digite para pesquisar');
  readonly opcoes = input<readonly SelectPesquisavelOpcao<T>[]>([]);
  readonly carregando = input(false);
  readonly mensagemErro = input('');
  readonly pesquisar = output<string>();

  protected readonly textoControl = new FormControl<SelectPesquisavelOpcao<T> | string | null>('');
  protected readonly valorAtual = signal<T | null>(null);
  protected readonly desabilitado = signal(false);
  private readonly textoBusca = signal('');
  private readonly destroyRef = inject(DestroyRef);

  protected readonly opcoesFiltradas = computed(() => {
    const consulta = this.textoBusca().trim().toLocaleLowerCase();
    const opcoes = this.opcoes();
    if (!consulta) return opcoes;

    return opcoes.filter((opcao) =>
      `${opcao.rotulo} ${opcao.textoBusca ?? ''}`.toLocaleLowerCase().includes(consulta),
    );
  });

  private aoAlterar: (valor: T | null) => void = () => {};
  private aoTocar: () => void = () => {};
  protected readonly errorStateMatcher: ErrorStateMatcher = {
    isErrorState: () => !!this.mensagemErro(),
  };

  constructor() {
    this.textoControl.valueChanges
      .pipe(
        map((valor) => (typeof valor === 'string' ? valor : '')),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((texto) => {
        this.textoBusca.set(texto);
        if (typeof this.textoControl.value === 'string') {
          this.valorAtual.set(null);
          this.aoAlterar(null);
        }
      });

    this.textoControl.valueChanges
      .pipe(
        map((valor) => (typeof valor === 'string' ? valor.trim() : null)),
        distinctUntilChanged(),
        debounceTime(300),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((texto) => {
        if (texto !== null) this.pesquisar.emit(texto);
      });

    effect(() => {
      const valor = this.valorAtual();
      if (valor === null) {
        if (typeof this.textoControl.value !== 'string') {
          this.textoControl.setValue('', { emitEvent: false });
        }
        return;
      }

      const opcao = this.opcoes().find((candidata) => Object.is(candidata.valor, valor));
      if (opcao) this.textoControl.setValue(opcao, { emitEvent: false });
    });
  }

  protected readonly exibirOpcao = (valor: SelectPesquisavelOpcao<T> | string | null): string =>
    typeof valor === 'string' ? valor : (valor?.rotulo ?? '');

  protected selecionar(evento: MatAutocompleteSelectedEvent): void {
    const opcao = evento.option.value as SelectPesquisavelOpcao<T>;
    this.valorAtual.set(opcao.valor);
    this.textoControl.setValue(opcao, { emitEvent: false });
    this.aoAlterar(opcao.valor);
    this.aoTocar();
  }

  protected limpar(evento: MouseEvent): void {
    evento.stopPropagation();
    this.valorAtual.set(null);
    this.textoBusca.set('');
    this.textoControl.setValue('', { emitEvent: false });
    this.aoAlterar(null);
    this.aoTocar();
    this.pesquisar.emit('');
  }

  protected marcarComoTocado(): void {
    this.aoTocar();
  }

  writeValue(valor: T | null): void {
    this.valorAtual.set(valor ?? null);
    if (valor === null) this.textoControl.setValue('', { emitEvent: false });
  }

  registerOnChange(fn: (valor: T | null) => void): void {
    this.aoAlterar = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.aoTocar = fn;
  }

  setDisabledState(desabilitado: boolean): void {
    this.desabilitado.set(desabilitado);
    if (desabilitado) this.textoControl.disable({ emitEvent: false });
    else this.textoControl.enable({ emitEvent: false });
  }
}
