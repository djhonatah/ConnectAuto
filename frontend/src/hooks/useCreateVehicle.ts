import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi, type VehicleInput } from '../services/api/vehicles';

export function useCreateVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: VehicleInput) => vehiclesApi.criar(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
    },
  });
}
