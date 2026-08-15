import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
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
  const location = useLocation();
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState<string | null>(
    (location.state as { successMessage?: string } | null)?.successMessage ?? null,
  );

  useEffect(() => {
    // O "if" protege contra reexecução: depois do primeiro navigate() aqui
    // embaixo, location.state vira null, então esse bloco não roda de novo
    // mesmo que o efeito seja reavaliado.
    if (location.state) {
      // Limpa o state da navegação para o aviso não reaparecer se o usuário
      // atualizar a página ou voltar por aqui de novo.
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.state, location.pathname, navigate]);

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

      {successMessage && (
        <p className="vehicles-page__success" role="status">
          {successMessage}
          <button
            type="button"
            className="vehicles-page__success-dismiss"
            aria-label="Fechar aviso"
            onClick={() => setSuccessMessage(null)}
          >
            ×
          </button>
        </p>
      )}

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
                  <td className="vehicles-page__chassi">{vehicle.chassi}</td>
                  <td>{currencyFormatter.format(vehicle.valor)}</td>
                  <td>
                    <Link
                      to={`/veiculos/${vehicle.id}/editar`}
                      className="vehicles-page__edit-link"
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
        <p>Nenhum veículo cadastrado.</p>
      )}
    </section>
  );
}
