import { useQuery } from '@tanstack/react-query';
import { dealersApi } from '../services/api/dealers';

export function useDealer(dealerId: number) {
  return useQuery({
    queryKey: ['dealers', dealerId],
    queryFn: () => dealersApi.buscarPorId(dealerId),
    enabled: Number.isFinite(dealerId),
  });
}
