import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useDeleteVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (vehicleId: number) => vehiclesApi.excluir(vehicleId),
    onSuccess: () => {
      // Também invalida ['dealers'], que cobre a listagem de veículos de uma
      // concessionária específica (['dealers', id, 'vehicles']).
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
