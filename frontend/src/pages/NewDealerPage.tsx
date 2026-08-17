import { Link, useNavigate } from 'react-router-dom';
import { DealerForm, type DealerFormValues } from '../components/DealerForm';
import { useCreateDealer } from '../hooks/useCreateDealer';
import './VehicleFormPage.css';

export function NewDealerPage() {
  const navigate = useNavigate();
  const createDealer = useCreateDealer();

  async function handleSubmit(values: DealerFormValues) {
    try {
      await createDealer.mutateAsync(values);
      navigate('/concessionarias', {
        state: { successMessage: 'Concessionária cadastrada com sucesso.' },
      });
    } catch {
    }
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <h1>Nova concessionária</h1>
        <Link to="/concessionarias" className="new-vehicle-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      {createDealer.isError && (
        <p className="new-vehicle-page__error" role="alert">
          Não foi possível cadastrar a concessionária: {createDealer.error.message}
        </p>
      )}

      <DealerForm onSubmit={handleSubmit} submitLabel="Cadastrar concessionária" />
    </section>
  );
}
