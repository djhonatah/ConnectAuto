import { Link } from 'react-router-dom';
import { useDealers } from '../hooks/useDealers';
import { StatusMessage } from '../components/StatusMessage';
import './DealersPage.css';

function formatCnpj(cnpj: string): string {
  const digits = cnpj.replace(/\D/g, '');
  if (digits.length !== 14) return cnpj;
  return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
}

export function DealersPage() {
  const { data: dealers, isLoading, isError, error, refetch, isFetching } = useDealers();

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
            {dealers?.length ?? 0}{' '}
            {dealers?.length === 1 ? 'concessionária cadastrada' : 'concessionárias cadastradas'}
            {isFetching && ' · atualizando…'}
          </span>
          <Link to="/concessionarias/novo" className="btn btn-primary btn-sm">
            + Nova concessionária
          </Link>
        </div>
      </header>

      {dealers?.length ? (
        <div className="dealers-page__table-wrap">
          <table className="dealers-page__table">
            <thead>
              <tr>
                <th>Razão social</th>
                <th>CNPJ</th>
                <th>Endereço</th>
                <th>Cidade/UF</th>
              </tr>
            </thead>
            <tbody>
              {dealers.map((dealer) => (
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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>Nenhuma concessionária cadastrada.</p>
      )}
    </section>
  );
}
