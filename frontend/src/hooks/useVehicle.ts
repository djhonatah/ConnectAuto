import { useQuery } from '@tanstack/react-query';
import { vehiclesApi } from '../services/api/vehicles';

export function useVehicle(vehicleId: number) {
  return useQuery({
    queryKey: ['vehicles', vehicleId],
    queryFn: () => vehiclesApi.buscarPorId(vehicleId),
    enabled: Number.isFinite(vehicleId),
  });
}
