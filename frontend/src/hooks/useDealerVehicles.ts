import { useQuery } from '@tanstack/react-query';
import { dealersApi } from '../services/api/dealers';

export function useDealerVehicles(dealerId: number) {
  return useQuery({
    queryKey: ['dealers', dealerId, 'vehicles'],
    queryFn: () => dealersApi.listarVeiculos(dealerId),
    enabled: Number.isFinite(dealerId),
  });
}
