import { useMutation, useQueryClient } from '@tanstack/react-query';
import { dealersApi } from '../services/api/dealers';

export function useDeleteDealer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (dealerId: number) => dealersApi.excluir(dealerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
