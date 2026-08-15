import { httpClient } from './httpClient';

export type FuelType = 'GASOLINA' | 'ETANOL' | 'FLEX' | 'DIESEL' | 'ELETRICO' | 'HIBRIDO';

export interface Vehicle {
  id: number;
  marca: string;
  modelo: string;
  tipoCombustivel: FuelType;
  cor: string;
  ano: number;
  chassi: string;
  valor: number;
  corInterna: string;
  dealerId: number | null;
}

export const vehiclesApi = {
  listar: () => httpClient.get<Vehicle[]>('/vehicles'),
  buscarPorId: (vehicleId: number) => httpClient.get<Vehicle>(`/vehicles/${vehicleId}`),
};
