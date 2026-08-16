import { Link } from 'react-router-dom';
import { useVehicles } from '../hooks/useVehicles';
import { useDealers } from '../hooks/useDealers';
import { StatusMessage } from '../components/StatusMessage';
import { FUEL_LABELS, type FuelType, type Vehicle } from '../services/api/vehicles';
import type { Dealer } from '../services/api/dealers';
import './HomePage.css';

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const compactCurrencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  notation: 'compact',
  maximumFractionDigits: 1,
});

const COLOR_SWATCHES: Record<string, string> = {
  branco: '#EDEDEF',
  preto: '#0A0A0B',
  prata: '#C9CDD3',
  cinza: '#8B92A0',
  'cinza grafite': '#3D4450',
  vermelho: '#E31C3D',
  azul: '#3B5BA9',
  amarelo: '#E8C547',
  verde: '#3E8E5A',
  bege: '#C9B79C',
  marrom: '#6B4A34',
  laranja: '#E08B3B',
};

function swatchFor(cor: string): string {
  return COLOR_SWATCHES[cor.trim().toLowerCase()] ?? '#6E6E73';
}

const FUEL_GROUPS: { label: string; fuels: FuelType[]; color: string; chipClass: string }[] = [
  { label: 'Flex', fuels: ['FLEX'], color: 'var(--accent)', chipClass: 'fuel-flex' },
  { label: 'Elétrico', fuels: ['ELETRICO'], color: 'var(--success)', chipClass: 'fuel-eletrico' },
  { label: 'Híbrido', fuels: ['HIBRIDO'], color: '#8B92A0', chipClass: 'fuel-hibrido' },
  {
    label: 'Diesel/Gasolina/Etanol',
    fuels: ['DIESEL', 'GASOLINA', 'ETANOL'],
    color: '#3A3A3D',
    chipClass: 'fuel-outros',
  },
];

function chipClassFor(fuel: FuelType): string {
  return FUEL_GROUPS.find((group) => group.fuels.includes(fuel))?.chipClass ?? 'fuel-outros';
}

export function HomePage() {
  const vehiclesQuery = useVehicles();
  const dealersQuery = useDealers();

  if (vehiclesQuery.isLoading || dealersQuery.isLoading) {
    return <StatusMessage kind="loading">Carregando painel…</StatusMessage>;
  }

  if (vehiclesQuery.isError || dealersQuery.isError) {
    return (
      <StatusMessage kind="error">
        Não foi possível carregar o painel:{' '}
        {vehiclesQuery.error?.message ?? dealersQuery.error?.message}
      </StatusMessage>
    );
  }

  const vehicles = vehiclesQuery.data ?? [];
  const dealers = dealersQuery.data ?? [];

  const dealerById = new Map<number, Dealer>(dealers.map((dealer) => [dealer.id, dealer]));
  const countsByDealer = new Map<number, number>();
  for (const vehicle of vehicles) {
    if (vehicle.dealerId == null) continue;
    countsByDealer.set(vehicle.dealerId, (countsByDealer.get(vehicle.dealerId) ?? 0) + 1);
  }

  const electrifiedCount = vehicles.filter((v) =>
    (['ELETRICO', 'HIBRIDO'] as FuelType[]).includes(v.tipoCombustivel),
  ).length;
  const totalValue = vehicles.reduce((sum, v) => sum + (v.valor ?? 0), 0);
  const activeDealers = dealers.filter((dealer) => (countsByDealer.get(dealer.id) ?? 0) > 0).length;

  const topDealers = dealers
    .map((dealer) => ({ dealer, count: countsByDealer.get(dealer.id) ?? 0 }))
    .filter((entry) => entry.count > 0)
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);
  const maxDealerCount = Math.max(1, ...topDealers.map((entry) => entry.count));

  let cursor = 0;
  const fuelSegments = FUEL_GROUPS.map((group) => {
    const count = vehicles.filter((v) => group.fuels.includes(v.tipoCombustivel)).length;
    const pct = vehicles.length ? count / vehicles.length : 0;
    const start = cursor;
    cursor += pct * 360;
    return { ...group, count, pct, start, end: cursor };
  });
  const donutGradient = fuelSegments.map((s) => `${s.color} ${s.start}deg ${s.end}deg`).join(', ');

  const recentVehicles: Vehicle[] = [...vehicles].sort((a, b) => b.id - a.id).slice(0, 5);

  return (
    <section className="dashboard">
      <div className="dashboard__head">
        <div>
          <h1>Visão geral</h1>
          <p>Painel de controle da rede ConnectAuto.</p>
        </div>
        <Link to="/veiculos/novo" className="btn btn-primary">
          + Novo veículo
        </Link>
      </div>

      <div className="dashboard__metrics">
        <div className="metric-card">
          <div className="metric-card__icon metric-card__icon--accent">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 13l1.5-5a2 2 0 0 1 2-1.4h11a2 2 0 0 1 2 1.4L21 13" />
              <path d="M3 13h18v5a1 1 0 0 1-1 1h-1.5a1 1 0 0 1-1-1v-1h-11v1a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z" />
            </svg>
          </div>
          <div className="metric-card__value">{vehicles.length}</div>
          <div className="metric-card__label">Veículos cadastrados</div>
        </div>

        <div className="metric-card">
          <div className="metric-card__icon metric-card__icon--success">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 21h18M5 21V7l7-4 7 4v14M9 21v-6h6v6" />
            </svg>
          </div>
          <div className="metric-card__value">{activeDealers}</div>
          <div className="metric-card__label">Concessionárias com estoque</div>
        </div>

        <div className="metric-card">
          <div className="metric-card__icon metric-card__icon--neutral">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M13 2L3 14h7l-1 8 10-12h-7z" />
            </svg>
          </div>
          <div className="metric-card__value">{electrifiedCount}</div>
          <div className="metric-card__label">Elétricos / híbridos</div>
        </div>

        <div className="metric-card">
          <div className="metric-card__icon metric-card__icon--neutral">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
          </div>
          <div className="metric-card__value" title={currencyFormatter.format(totalValue)}>
            {compactCurrencyFormatter.format(totalValue)}
          </div>
          <div className="metric-card__label">Valor total em estoque</div>
        </div>
      </div>

      <div className="dashboard__grid">
        <div className="panel">
          <div className="panel__head">
            <h3>Veículos por concessionária</h3>
            <span className="panel__tag">Top {topDealers.length || 0}</span>
          </div>
          {topDealers.length ? (
            <div className="dashboard__bars">
              {topDealers.map(({ dealer, count }) => (
                <div key={dealer.id} className="dashboard__bar-col">
                  <div className="dashboard__bar-track">
                    <div
                      className="dashboard__bar-fill"
                      style={{ height: `${(count / maxDealerCount) * 100}%` }}
                      title={`${count} veículo${count === 1 ? '' : 's'}`}
                    />
                  </div>
                  <span>{dealer.endereco.cidade}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="dashboard__empty">Nenhum veículo associado a uma concessionária ainda.</p>
          )}
        </div>

        <div className="panel">
          <div className="panel__head">
            <h3>Por combustível</h3>
            <span className="panel__tag">{vehicles.length} total</span>
          </div>
          {vehicles.length ? (
            <div className="dashboard__donut-wrap">
              <div
                className="dashboard__donut"
                style={{ background: `conic-gradient(${donutGradient})` }}
              />
              <div className="dashboard__legend">
                {fuelSegments
                  .filter((segment) => segment.count > 0)
                  .map((segment) => (
                    <div key={segment.label} className="dashboard__legend-item">
                      <span
                        className="dashboard__legend-dot"
                        style={{ background: segment.color }}
                      />
                      {segment.label} · {Math.round(segment.pct * 100)}%
                    </div>
                  ))}
              </div>
            </div>
          ) : (
            <p className="dashboard__empty">Nenhum veículo cadastrado ainda.</p>
          )}
        </div>
      </div>

      <div className="panel__head dashboard__table-head">
        <h3>Últimos veículos cadastrados</h3>
      </div>

      {recentVehicles.length ? (
        <div className="dashboard__table-wrap">
          <table className="dashboard__table">
            <thead>
              <tr>
                <th>Veículo</th>
                <th>Combustível</th>
                <th>Concessionária</th>
                <th>Cor</th>
                <th>Valor</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {recentVehicles.map((vehicle) => (
                <tr key={vehicle.id}>
                  <td>
                    <div className="dashboard__car-cell">
                      <span
                        className="dashboard__car-swatch"
                        style={{ background: swatchFor(vehicle.cor) }}
                      />
                      <div>
                        <div className="dashboard__car-model">{vehicle.modelo}</div>
                        <div className="dashboard__car-brand">{vehicle.marca}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={`fuel-chip ${chipClassFor(vehicle.tipoCombustivel)}`}>
                      {FUEL_LABELS[vehicle.tipoCombustivel]}
                    </span>
                  </td>
                  <td className="dashboard__dealer-cell">
                    {vehicle.dealerId != null
                      ? (dealerById.get(vehicle.dealerId)?.razaoSocial ?? '—')
                      : 'Sem concessionária'}
                  </td>
                  <td>{vehicle.cor}</td>
                  <td className="dashboard__value-cell">
                    {vehicle.valor != null ? currencyFormatter.format(vehicle.valor) : '—'}
                  </td>
                  <td>
                    <Link to={`/veiculos/${vehicle.id}/editar`} className="dashboard__row-action">
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
