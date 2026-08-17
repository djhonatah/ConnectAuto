import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useDeleteVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (vehicleId: number) => vehiclesApi.excluir(vehicleId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
