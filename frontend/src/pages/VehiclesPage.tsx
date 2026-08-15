import { Link } from 'react-router-dom';
import { useVehicles } from '../hooks/useVehicles';
import { StatusMessage } from '../components/StatusMessage';
import { FUEL_LABELS } from '../services/api/vehicles';
import './VehiclesPage.css';

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function VehiclesPage() {
  const { data: vehicles, isLoading, isError, error, refetch, isFetching } = useVehicles();

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando veículos…</StatusMessage>;
  }

  if (isError) {
    return (
      <StatusMessage
        kind="error"
        action={
          <button type="button" className="status-message__retry" onClick={() => refetch()}>
            Tentar novamente
          </button>
        }
      >
        Não foi possível carregar os veículos: {error.message}
      </StatusMessage>
    );
  }

  return (
    <section className="vehicles-page">
      <header className="vehicles-page__header">
        <h1>Veículos</h1>
        <div className="vehicles-page__header-actions">
          <span className="vehicles-page__count">
            {vehicles?.length ?? 0}{' '}
            {vehicles?.length === 1 ? 'veículo cadastrado' : 'veículos cadastrados'}
            {isFetching && ' · atualizando…'}
          </span>
          <Link to="/veiculos/novo" className="vehicles-page__new-link">
            + Novo veículo
          </Link>
        </div>
      </header>

      {vehicles?.length ? (
        <div className="vehicles-page__table-wrap">
          <table className="vehicles-page__table">
            <thead>
              <tr>
                <th>Veículo</th>
                <th>Ano</th>
                <th>Cor</th>
                <th>Combustível</th>
                <th>Chassi</th>
                <th>Valor</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.map((vehicle) => (
                <tr key={vehicle.id}>
                  <td>
                    <strong>{vehicle.marca}</strong> {vehicle.modelo}
                  </td>
                  <td>{vehicle.ano}</td>
                  <td>{vehicle.cor}</td>
                  <td>{FUEL_LABELS[vehicle.tipoCombustivel]}</td>
                  <td className="vehicles-page__chassi">{vehicle.chassi}</td>
                  <td>{currencyFormatter.format(vehicle.valor)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>Nenhum veículo cadastrado.</p>
      )}
    </section>
  );
}
