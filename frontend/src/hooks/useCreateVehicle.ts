import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi, type VehicleInput } from '../services/api/vehicles';

export function useCreateVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: VehicleInput) => vehiclesApi.criar(data),
    onSuccess: () => {
      // Invalida o cache da listagem, forçando um refetch — é assim que a
      // tela de veículos passa a mostrar o item recém-criado.
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
    },
  });
}
