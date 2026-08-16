import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { VehicleForm, type VehicleFormValues } from '../components/VehicleForm';
import { StatusMessage } from '../components/StatusMessage';
import { useVehicle } from '../hooks/useVehicle';
import { useUpdateVehicle } from '../hooks/useUpdateVehicle';
import './VehicleFormPage.css';

export function EditVehiclePage() {
  const { id } = useParams<{ id: string }>();
  const vehicleId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();
  const backTo = (location.state as { from?: string } | null)?.from ?? '/veiculos';

  const { data: vehicle, isLoading, isError, error, refetch } = useVehicle(vehicleId);
  const updateVehicle = useUpdateVehicle();

  async function handleSubmit(values: VehicleFormValues) {
    try {
      await updateVehicle.mutateAsync({ id: vehicleId, data: values });
      navigate(backTo, { state: { successMessage: 'Veículo atualizado com sucesso.' } });
    } catch {
      // Erro tratado via updateVehicle.isError/error, exibido abaixo.
    }
  }

  if (!Number.isFinite(vehicleId)) {
    return <StatusMessage kind="error">Veículo inválido.</StatusMessage>;
  }

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando veículo…</StatusMessage>;
  }

  if (isError || !vehicle) {
    return (
      <StatusMessage
        kind="error"
        action={
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => refetch()}>
            Tentar novamente
          </button>
        }
      >
        Não foi possível carregar o veículo: {error?.message ?? 'não encontrado'}
      </StatusMessage>
    );
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <div>
          <h1>Editar veículo</h1>
          <p className="new-vehicle-page__subtitle">
            {vehicle.marca} {vehicle.modelo}
          </p>
        </div>
        <Link to={backTo} className="new-vehicle-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      {updateVehicle.isError && (
        <p className="new-vehicle-page__error" role="alert">
          Não foi possível salvar as alterações: {updateVehicle.error.message}
        </p>
      )}

      <VehicleForm
        defaultValues={{
          marca: vehicle.marca,
          modelo: vehicle.modelo,
          tipoCombustivel: vehicle.tipoCombustivel,
          cor: vehicle.cor,
          ano: vehicle.ano,
          chassi: vehicle.chassi,
          valor: vehicle.valor,
          corInterna: vehicle.corInterna,
          dealerId: vehicle.dealerId ?? undefined,
        }}
        onSubmit={handleSubmit}
        submitLabel="Salvar alterações"
      />
    </section>
  );
}
