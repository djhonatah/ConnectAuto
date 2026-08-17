import { Link, useNavigate } from 'react-router-dom';
import { VehicleForm, type VehicleFormValues } from '../components/VehicleForm';
import { useCreateVehicle } from '../hooks/useCreateVehicle';
import './VehicleFormPage.css';

export function NewVehiclePage() {
  const navigate = useNavigate();
  const createVehicle = useCreateVehicle();

  async function handleSubmit(values: VehicleFormValues) {
    try {
      await createVehicle.mutateAsync(values);
      navigate('/veiculos', { state: { successMessage: 'Veículo cadastrado com sucesso.' } });
    } catch {
    }
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <h1>Novo veículo</h1>
        <Link to="/veiculos" className="new-vehicle-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      {createVehicle.isError && (
        <p className="new-vehicle-page__error" role="alert">
          Não foi possível cadastrar o veículo: {createVehicle.error.message}
        </p>
      )}

      <VehicleForm onSubmit={handleSubmit} submitLabel="Cadastrar veículo" />
    </section>
  );
}
