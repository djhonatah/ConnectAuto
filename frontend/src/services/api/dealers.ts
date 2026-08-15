import { httpClient } from './httpClient';
import type { Vehicle } from './vehicles';

export interface Endereco {
  logradouro: string;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
}

export interface Dealer {
  id: number;
  razaoSocial: string;
  cnpj: string;
  endereco: Endereco;
}

export interface DealerInput {
  razaoSocial: string;
  cnpj: string;
  endereco: Endereco;
}

export const dealersApi = {
  listar: () => httpClient.get<Dealer[]>('/dealer'),
  buscarPorId: (dealerId: number) => httpClient.get<Dealer>(`/dealer/${dealerId}`),
  listarVeiculos: (dealerId: number) => httpClient.get<Vehicle[]>(`/dealer/${dealerId}/vehicles`),
  criar: (data: DealerInput) => httpClient.post<Dealer>('/dealer', data),
  atualizar: (dealerId: number, data: DealerInput) =>
    httpClient.put<Dealer>(`/dealer/${dealerId}`, data),
};
