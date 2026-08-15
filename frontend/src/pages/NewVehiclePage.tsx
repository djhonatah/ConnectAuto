import { useState } from 'react';
import { Link } from 'react-router-dom';
import { VehicleForm, type VehicleFormValues } from '../components/VehicleForm';
import './NewVehiclePage.css';

export function NewVehiclePage() {
  const [lastSaved, setLastSaved] = useState<VehicleFormValues | null>(null);

  // TODO(feature 2 da issue #25): trocar por uma mutation TanStack Query que
  // chama POST /vehicles (vehiclesApi.criar) e invalida a query 'vehicles'
  // para a listagem refletir o veículo recém-criado. Por enquanto, esta
  // feature só garante que o formulário valida e não deixa passar dados
  // inválidos.
  function handleSubmit(values: VehicleFormValues) {
    setLastSaved(values);
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <h1>Novo veículo</h1>
        <Link to="/veiculos">← Voltar para a listagem</Link>
      </header>

      <VehicleForm onSubmit={handleSubmit} submitLabel="Cadastrar veículo" />

      {lastSaved && (
        <div className="new-vehicle-page__preview">
          <p>
            Formulário válido — ainda não é enviado para a API (isso é feito na próxima feature).
            Dados que seriam salvos:
          </p>
          <pre>{JSON.stringify(lastSaved, null, 2)}</pre>
        </div>
      )}
    </section>
  );
}
