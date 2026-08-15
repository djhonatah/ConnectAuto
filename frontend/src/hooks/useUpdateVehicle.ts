import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi, type VehicleInput } from '../services/api/vehicles';

export function useUpdateVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: VehicleInput }) =>
      vehiclesApi.atualizar(id, data),
    onSuccess: () => {
      // Invalida tanto a listagem quanto o cache do veículo individual, para
      // que qualquer tela que dependa desses dados busque a versão nova.
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
    },
  });
}
