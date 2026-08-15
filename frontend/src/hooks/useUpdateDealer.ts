import { useMutation, useQueryClient } from '@tanstack/react-query';
import { dealersApi, type DealerInput } from '../services/api/dealers';

export function useUpdateDealer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: DealerInput }) => dealersApi.atualizar(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
