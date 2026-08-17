import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi, type VehicleInput } from '../services/api/vehicles';

export function useUpdateVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: VehicleInput }) =>
      vehiclesApi.atualizar(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
