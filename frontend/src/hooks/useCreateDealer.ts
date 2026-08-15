import { useMutation, useQueryClient } from '@tanstack/react-query';
import { dealersApi, type DealerInput } from '../services/api/dealers';

export function useCreateDealer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DealerInput) => dealersApi.criar(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
