import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useAssociateDealer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ vehicleId, dealerId }: { vehicleId: number; dealerId: number | null }) =>
      vehiclesApi.associarDealer(vehicleId, dealerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
