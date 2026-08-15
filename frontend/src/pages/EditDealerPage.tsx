import { Link, useNavigate, useParams } from 'react-router-dom';
import { DealerForm, type DealerFormValues } from '../components/DealerForm';
import { StatusMessage } from '../components/StatusMessage';
import { useDealer } from '../hooks/useDealer';
import { useUpdateDealer } from '../hooks/useUpdateDealer';
import './VehicleFormPage.css';

export function EditDealerPage() {
  const { id } = useParams<{ id: string }>();
  const dealerId = Number(id);
  const navigate = useNavigate();

  const { data: dealer, isLoading, isError, error, refetch } = useDealer(dealerId);
  const updateDealer = useUpdateDealer();

  async function handleSubmit(values: DealerFormValues) {
    try {
      await updateDealer.mutateAsync({ id: dealerId, data: values });
      navigate('/concessionarias', {
        state: { successMessage: 'Concessionária atualizada com sucesso.' },
      });
    } catch {
      // Erro tratado via updateDealer.isError/error, exibido abaixo.
    }
  }

  if (!Number.isFinite(dealerId)) {
    return <StatusMessage kind="error">Concessionária inválida.</StatusMessage>;
  }

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando concessionária…</StatusMessage>;
  }

  if (isError || !dealer) {
    return (
      <StatusMessage
        kind="error"
        action={
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => refetch()}>
            Tentar novamente
          </button>
        }
      >
        Não foi possível carregar a concessionária: {error?.message ?? 'não encontrada'}
      </StatusMessage>
    );
  }

  return (
    <section className="new-vehicle-page">
      <header className="new-vehicle-page__header">
        <div>
          <h1>Editar concessionária</h1>
          <p className="new-vehicle-page__subtitle">{dealer.razaoSocial}</p>
        </div>
        <Link to="/concessionarias" className="new-vehicle-page__back">
          ← Voltar para a listagem
        </Link>
      </header>

      {updateDealer.isError && (
        <p className="new-vehicle-page__error" role="alert">
          Não foi possível salvar as alterações: {updateDealer.error.message}
        </p>
      )}

      <DealerForm
        defaultValues={{
          razaoSocial: dealer.razaoSocial,
          cnpj: dealer.cnpj,
          endereco: dealer.endereco,
        }}
        onSubmit={handleSubmit}
        submitLabel="Salvar alterações"
      />
    </section>
  );
}
