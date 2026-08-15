import { useQuery } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useVehicles() {
  return useQuery({
    queryKey: ['vehicles'],
    queryFn: vehiclesApi.listar,
  });
}
