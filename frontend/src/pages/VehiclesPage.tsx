import { useVehicles } from '../hooks/useVehicles';
import { StatusMessage } from '../components/StatusMessage';

export function VehiclesPage() {
  const { data: vehicles, isLoading, isError, error } = useVehicles();

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando veículos…</StatusMessage>;
  }

  if (isError) {
    return (
      <StatusMessage kind="error">
        Não foi possível carregar os veículos: {error.message}
      </StatusMessage>
    );
  }

  return (
    <section>
      <h1>Veículos</h1>
      {vehicles?.length ? (
        <ul>
          {vehicles.map((vehicle) => (
            <li key={vehicle.id}>
              {vehicle.marca} {vehicle.modelo} ({vehicle.ano})
            </li>
          ))}
        </ul>
      ) : (
        <p>Nenhum veículo cadastrado.</p>
      )}
    </section>
  );
}
