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

// Formato aceito por POST /vehicles e PUT /vehicles/{id} — espelha
// VehicleRequestDTO no backend. Não importa nada de components/VehicleForm
// (services/api não conhece React/formulário); VehicleFormValues só precisa
// ter esse mesmo formato, o que o TypeScript já garante estruturalmente.
export interface VehicleInput {
  marca: string;
  modelo: string;
  tipoCombustivel: FuelType;
  cor: string;
  ano?: number;
  chassi?: string;
  valor?: number;
  corInterna?: string;
}

export const vehiclesApi = {
  listar: () => httpClient.get<Vehicle[]>('/vehicles'),
  buscarPorId: (vehicleId: number) => httpClient.get<Vehicle>(`/vehicles/${vehicleId}`),
  criar: (data: VehicleInput) => httpClient.post<Vehicle>('/vehicles', data),
  atualizar: (vehicleId: number, data: VehicleInput) =>
    httpClient.put<Vehicle>(`/vehicles/${vehicleId}`, data),
  associarDealer: (vehicleId: number, dealerId: number | null) =>
    httpClient.patch<Vehicle>(`/vehicles/${vehicleId}/dealer`, { dealerId }),
  excluir: (vehicleId: number) => httpClient.delete<void>(`/vehicles/${vehicleId}`),
};
