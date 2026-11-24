import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cpfCnpj',
  standalone: true,
})
export class CpfCnpjPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    const alfanumerico = (valor ?? '').toUpperCase().replace(/[^A-Z0-9]/g, '');

    if (/^\d{11}$/.test(alfanumerico)) {
      return alfanumerico.replace(
        /^(\d{3})(\d{3})(\d{3})(\d{2})$/,
        '$1.$2.$3-$4',
      );
    }

    if (alfanumerico.length === 14) {
      return alfanumerico.replace(
        /^([A-Z0-9]{2})([A-Z0-9]{3})([A-Z0-9]{3})([A-Z0-9]{4})([A-Z0-9]{2})$/,
        '$1.$2.$3/$4-$5',
      );
    }

    return valor?.trim() || '-';
  }
}