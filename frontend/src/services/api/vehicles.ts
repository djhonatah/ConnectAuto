import { httpClient } from './httpClient';

export type FuelType = 'GASOLINA' | 'ETANOL' | 'FLEX' | 'DIESEL' | 'ELETRICO' | 'HIBRIDO';

export const FUEL_LABELS: Record<FuelType, string> = {
  GASOLINA: 'Gasolina',
  ETANOL: 'Etanol',
  FLEX: 'Flex',
  DIESEL: 'Diesel',
  ELETRICO: 'Elétrico',
  HIBRIDO: 'Híbrido',
};

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
