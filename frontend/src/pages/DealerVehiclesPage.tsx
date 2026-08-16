import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useDealer } from '../hooks/useDealer';
import { useDealerVehicles } from '../hooks/useDealerVehicles';
import { StatusMessage } from '../components/StatusMessage';
import { FUEL_LABELS } from '../services/api/vehicles';
import './DealerVehiclesPage.css';

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function DealerVehiclesPage() {
  const { id } = useParams<{ id: string }>();
  const dealerId = Number(id);
  const location = useLocation();
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState<string | null>(
    (location.state as { successMessage?: string } | null)?.successMessage ?? null,
  );

  useEffect(() => {
    if (location.state) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.state, location.pathname, navigate]);

  const { data: dealer, isLoading: isLoadingDealer, isError: isDealerError } = useDealer(dealerId);
  const {
    data: vehicles,
    isLoading: isLoadingVehicles,
    isError: isVehiclesError,
    error: vehiclesError,
    refetch,
  } = useDealerVehicles(dealerId);

  if (!Number.isFinite(dealerId)) {
    return <StatusMessage kind="error">Concessionária inválida.</StatusMessage>;
  }

  if (isLoadingDealer || isLoadingVehicles) {
    return <StatusMessage kind="loading">Carregando veículos…</StatusMessage>;
  }

  if (isDealerError || !dealer) {
    return <StatusMessage kind="error">Não foi possível carregar a concessionária.</StatusMessage>;
  }

  if (isVehiclesError) {
    return (
      <StatusMessage
        kind="error"
        action={
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => refetch()}>
            Tentar novamente
          </button>
        }
      >
        Não foi possível carregar os veículos: {vehiclesError.message}
      </StatusMessage>
    );
  }

  return (
    <section className="dealer-vehicles-page">
      <header className="dealer-vehicles-page__header">
        <div>
          <h1>Veículos da concessionária</h1>
          <p className="dealer-vehicles-page__subtitle">{dealer.razaoSocial}</p>
        </div>
        <Link to="/concessionarias" className="dealer-vehicles-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      {successMessage && (
        <p className="dealer-vehicles-page__success" role="status">
          {successMessage}
          <button
            type="button"
            className="dealer-vehicles-page__success-dismiss"
            aria-label="Fechar aviso"
            onClick={() => setSuccessMessage(null)}
          >
            ×
          </button>
        </p>
      )}

      {vehicles?.length ? (
        <div className="dealer-vehicles-page__table-wrap">
          <table className="dealer-vehicles-page__table">
            <thead>
              <tr>
                <th>Veículo</th>
                <th>Ano</th>
                <th>Cor</th>
                <th>Combustível</th>
                <th>Chassi</th>
                <th>Valor</th>
                <th></th>
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
                  <td>
                    {vehicle.chassi ? (
                      <span className="plate-chip">{vehicle.chassi}</span>
                    ) : (
                      <span className="plate-chip plate-chip--empty">sem chassi</span>
                    )}
                  </td>
                  <td className="dealer-vehicles-page__value">
                    {currencyFormatter.format(vehicle.valor)}
                  </td>
                  <td>
                    <Link
                      to={`/veiculos/${vehicle.id}/editar`}
                      state={{ from: location.pathname }}
                      className="dealer-vehicles-page__edit-link"
                    >
                      Editar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>Nenhum veículo associado a esta concessionária.</p>
      )}
    </section>
  );
}
