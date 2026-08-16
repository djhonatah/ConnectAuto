import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useDealers } from '../hooks/useDealers';
import { useDeleteDealer } from '../hooks/useDeleteDealer';
import { useAppSearch } from '../hooks/useAppSearch';
import { StatusMessage } from '../components/StatusMessage';
import { ConfirmDialog } from '../components/ConfirmDialog';
import type { Dealer } from '../services/api/dealers';
import './DealersPage.css';

function formatCnpj(cnpj: string): string {
  const digits = cnpj.replace(/\D/g, '');
  if (digits.length !== 14) return cnpj;
  return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
}

function matchesQuery(dealer: Dealer, query: string): boolean {
  const normalized = query.trim().toLowerCase();
  if (!normalized) return true;
  const digitsOnly = normalized.replace(/\D/g, '');
  return (
    dealer.razaoSocial.toLowerCase().includes(normalized) ||
    dealer.endereco.cidade.toLowerCase().includes(normalized) ||
    (digitsOnly.length > 0 && dealer.cnpj.includes(digitsOnly))
  );
}

export function DealersPage() {
  const { data: dealers, isLoading, isError, error, refetch, isFetching } = useDealers();
  const deleteDealer = useDeleteDealer();
  const location = useLocation();
  const navigate = useNavigate();
  const searchQuery = useAppSearch();
  const filteredDealers = dealers?.filter((dealer) => matchesQuery(dealer, searchQuery));

  const [successMessage, setSuccessMessage] = useState<string | null>(
    (location.state as { successMessage?: string } | null)?.successMessage ?? null,
  );
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [dealerToDelete, setDealerToDelete] = useState<Dealer | null>(null);

  useEffect(() => {
    if (location.state) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.state, location.pathname, navigate]);

  function askDelete(dealer: Dealer) {
    setDeleteErrorMessage(null);
    setDealerToDelete(dealer);
  }

  async function confirmDelete() {
    if (!dealerToDelete) return;
    try {
      await deleteDealer.mutateAsync(dealerToDelete.id);
      setSuccessMessage(`${dealerToDelete.razaoSocial} excluída com sucesso.`);
      setDealerToDelete(null);
    } catch (err) {
      setDeleteErrorMessage(
        err instanceof Error ? err.message : 'Não foi possível excluir a concessionária.',
      );
    }
  }

  if (isLoading) {
    return <StatusMessage kind="loading">Carregando concessionárias…</StatusMessage>;
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
        Não foi possível carregar as concessionárias: {error.message}
      </StatusMessage>
    );
  }

  return (
    <section className="dealers-page">
      <header className="dealers-page__header">
        <h1>Concessionárias</h1>
        <div className="dealers-page__header-actions">
          <span className="dealers-page__count">
            {searchQuery
              ? `${filteredDealers?.length ?? 0} de ${dealers?.length ?? 0} concessionárias`
              : `${dealers?.length ?? 0} ${dealers?.length === 1 ? 'concessionária cadastrada' : 'concessionárias cadastradas'}`}
            {isFetching && ' · atualizando…'}
          </span>
          <Link to="/concessionarias/novo" className="btn btn-primary btn-sm">
            + Nova concessionária
          </Link>
        </div>
      </header>

      {successMessage && (
        <p className="dealers-page__success" role="status">
          {successMessage}
          <button
            type="button"
            className="dealers-page__success-dismiss"
            aria-label="Fechar aviso"
            onClick={() => setSuccessMessage(null)}
          >
            ×
          </button>
        </p>
      )}

      {deleteErrorMessage && (
        <p className="dealers-page__error" role="alert">
          {deleteErrorMessage}
          <button
            type="button"
            className="dealers-page__success-dismiss"
            aria-label="Fechar aviso"
            onClick={() => setDeleteErrorMessage(null)}
          >
            ×
          </button>
        </p>
      )}

      {filteredDealers?.length ? (
        <div className="dealers-page__table-wrap">
          <table className="dealers-page__table">
            <thead>
              <tr>
                <th>Razão social</th>
                <th>CNPJ</th>
                <th>Endereço</th>
                <th>Cidade/UF</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filteredDealers.map((dealer) => (
                <tr key={dealer.id}>
                  <td>
                    <strong>{dealer.razaoSocial}</strong>
                  </td>
                  <td>
                    <span className="plate-chip">{formatCnpj(dealer.cnpj)}</span>
                  </td>
                  <td>
                    {dealer.endereco.logradouro}, {dealer.endereco.bairro}
                  </td>
                  <td>
                    {dealer.endereco.cidade}/{dealer.endereco.estado}
                  </td>
                  <td className="dealers-page__row-actions">
                    <Link
                      to={`/concessionarias/${dealer.id}/veiculos`}
                      className="dealers-page__edit-link"
                    >
                      Ver veículos
                    </Link>
                    <Link
                      to={`/concessionarias/${dealer.id}/editar`}
                      className="dealers-page__edit-link"
                    >
                      Editar
                    </Link>
                    <button
                      type="button"
                      className="dealers-page__delete-link"
                      onClick={() => askDelete(dealer)}
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
        <p>
          {searchQuery
            ? 'Nenhuma concessionária encontrada para essa busca.'
            : 'Nenhuma concessionária cadastrada.'}
        </p>
      )}

      <ConfirmDialog
        open={dealerToDelete !== null}
        title="Excluir concessionária?"
        description={
          dealerToDelete
            ? `Isso vai remover ${dealerToDelete.razaoSocial} (${formatCnpj(dealerToDelete.cnpj)}) permanentemente. Essa ação não pode ser desfeita.`
            : undefined
        }
        confirmLabel="Excluir"
        isConfirming={deleteDealer.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setDealerToDelete(null)}
      />
    </section>
  );
}
