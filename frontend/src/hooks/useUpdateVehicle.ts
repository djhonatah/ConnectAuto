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
      // Também invalida ['dealers'] (que cobre ['dealers', id, 'vehicles']),
      // já que a tela de veículos de uma concessionária mostra esses dados.
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
