import { Link } from 'react-router-dom';
import { useVehicles } from '../hooks/useVehicles';
import './HomePage.css';

export function HomePage() {
  const { data: vehicles, isLoading } = useVehicles();
  const total = vehicles?.length ?? 0;

  return (
    <section className="home-page">
      <div className="home-page__intro">
        <span className="home-page__status">
          <span className="home-page__status-dot" aria-hidden="true" />
          Sistema online
        </span>
        <h1>ConnectAuto</h1>
        <p className="home-page__lead">
          Cadastre, edite e acompanhe o estoque de veículos das concessionárias em um só lugar.
        </p>
        <div className="home-page__actions">
          <Link to="/veiculos" className="btn btn-primary">
            Ver veículos
          </Link>
          <Link to="/veiculos/novo" className="btn btn-ghost">
            Cadastrar veículo
          </Link>
        </div>
      </div>

      <div className="home-page__tile home-page__tile--stat">
        <span className="home-page__tile-label">Veículos cadastrados</span>
        <strong className="home-page__stat">{isLoading ? '···' : total}</strong>
        <span className="home-page__tile-foot">no estoque atual</span>
      </div>

      <Link to="/sobre" className="home-page__tile home-page__tile--link">
        <span className="home-page__tile-label">Sobre o ConnectAuto</span>
        <p>Como a plataforma organiza o estoque entre concessionárias.</p>
        <span className="home-page__tile-cta">Saiba mais →</span>
      </Link>
    </section>
  );
}
