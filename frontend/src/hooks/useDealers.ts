import { useQuery } from '@tanstack/react-query';
import { dealersApi } from '../services/api/dealers';

export function useDealers() {
  return useQuery({
    queryKey: ['dealers'],
    queryFn: dealersApi.listar,
  });
}
