import { useMutation, useQueryClient } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useDeleteVehicle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (vehicleId: number) => vehiclesApi.excluir(vehicleId),
    onSuccess: () => {
      // Invalida a listagem para o item excluído sumir da tela sem precisar
      // de reload manual — é o que a issue #26 pede em "atualizar a listagem
      // após exclusão". O diálogo de confirmação e o botão que disparam essa
      // mutation entram na próxima feature.
      // Também invalida ['dealers'], que cobre a listagem de veículos de uma
      // concessionária específica (['dealers', id, 'vehicles']).
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      queryClient.invalidateQueries({ queryKey: ['dealers'] });
    },
  });
}
