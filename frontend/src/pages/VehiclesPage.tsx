import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useVehicles } from '../hooks/useVehicles';
import { useDeleteVehicle } from '../hooks/useDeleteVehicle';
import { StatusMessage } from '../components/StatusMessage';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { FUEL_LABELS, type Vehicle } from '../services/api/vehicles';
import './VehiclesPage.css';

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function VehiclesPage() {
  const { data: vehicles, isLoading, isError, error, refetch, isFetching } = useVehicles();
  const deleteVehicle = useDeleteVehicle();
  const location = useLocation();
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState<string | null>(
    (location.state as { successMessage?: string } | null)?.successMessage ?? null,
  );
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [vehicleToDelete, setVehicleToDelete] = useState<Vehicle | null>(null);

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

  function askDelete(vehicle: Vehicle) {
    setDeleteErrorMessage(null);
    setVehicleToDelete(vehicle);
  }

  async function confirmDelete() {
    if (!vehicleToDelete) return;
    try {
      await deleteVehicle.mutateAsync(vehicleToDelete.id);
      setSuccessMessage(`${vehicleToDelete.marca} ${vehicleToDelete.modelo} excluído com sucesso.`);
      setVehicleToDelete(null);
    } catch (err) {
      setDeleteErrorMessage(
        err instanceof Error ? err.message : 'Não foi possível excluir o veículo.',
      );
    }
  }

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando veículos…</StatusMessage>;
  }

  if (isError) {
    return (
      <StatusMessage
        kind="error"
        action={
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => refetch()}>
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
          <Link to="/veiculos/novo" className="btn btn-primary btn-sm">
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

      {deleteErrorMessage && (
        <p className="vehicles-page__error" role="alert">
          {deleteErrorMessage}
          <button
            type="button"
            className="vehicles-page__success-dismiss"
            aria-label="Fechar aviso"
            onClick={() => setDeleteErrorMessage(null)}
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
                  <td>
                    {vehicle.chassi ? (
                      <span className="plate-chip">{vehicle.chassi}</span>
                    ) : (
                      <span className="plate-chip plate-chip--empty">sem chassi</span>
                    )}
                  </td>
                  <td className="vehicles-page__value">
                    {currencyFormatter.format(vehicle.valor)}
                  </td>
                  <td className="vehicles-page__row-actions">
                    <Link
                      to={`/veiculos/${vehicle.id}/editar`}
                      className="vehicles-page__edit-link"
                    >
                      Editar
                    </Link>
                    <button
                      type="button"
                      className="vehicles-page__delete-link"
                      onClick={() => askDelete(vehicle)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>Nenhum veículo cadastrado.</p>
      )}

      <ConfirmDialog
        open={vehicleToDelete !== null}
        title="Excluir veículo?"
        description={
          vehicleToDelete
            ? `Isso vai remover ${vehicleToDelete.marca} ${vehicleToDelete.modelo} (${vehicleToDelete.chassi || 'sem chassi'}) permanentemente. Essa ação não pode ser desfeita.`
            : undefined
        }
        confirmLabel="Excluir"
        isConfirming={deleteVehicle.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setVehicleToDelete(null)}
      />
    </section>
  );
}
