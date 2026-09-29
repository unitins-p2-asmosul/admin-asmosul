import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'telefone',
  standalone: true,
})
export class TelefonePipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    const digitos = (valor ?? '').replace(/\D/g, '');

    if (digitos.length === 10) {
      return digitos.replace(
        /^(\d{2})(\d{4})(\d{4})$/,
        '($1) $2-$3',
      );
    }

    if (digitos.length === 11) {
      return digitos.replace(
        /^(\d{2})(\d{5})(\d{4})$/,
        '($1) $2-$3',
      );
    }

    return valor?.trim() || '-';
  }
}