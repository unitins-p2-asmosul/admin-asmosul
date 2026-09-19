import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UfCodigo } from '../models/pessoa.model';

export interface EnderecoCep {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: UfCodigo;
}

@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);

  buscar(cep: string): Observable<EnderecoCep> {
    return this.http.get<EnderecoCep>(`cep/${cep.replace(/\D/g, '')}`);
  }
}
