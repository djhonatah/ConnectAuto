import { useQuery } from '@tanstack/react-query';
import { viaCepApi } from '../services/api/viaCep';

export function useCepLookup(cep: string) {
  return useQuery({
    queryKey: ['cep', cep],
    queryFn: () => viaCepApi.buscar(cep),
    enabled: cep.length === 8,
    staleTime: Infinity,
  });
}
