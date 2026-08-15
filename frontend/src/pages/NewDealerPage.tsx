import { Link } from 'react-router-dom';
import { DealerForm, type DealerFormValues } from '../components/DealerForm';
import './VehicleFormPage.css';

export function NewDealerPage() {
  function handleSubmit(values: DealerFormValues) {
    console.log('dealer válido:', values);
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <h1>Nova concessionária</h1>
        <Link to="/concessionarias" className="new-vehicle-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      <DealerForm onSubmit={handleSubmit} submitLabel="Cadastrar concessionária" />
    </section>
  );
}
