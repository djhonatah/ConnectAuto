import { Link } from 'react-router-dom';
import './NotFoundPage.css';

export function NotFoundPage() {
  return (
    <section className="not-found-page">
      <span className="plate-chip not-found-page__plate">404</span>
      <h1>Página não encontrada</h1>
      <p>O endereço não corresponde a nenhuma tela do ConnectAuto.</p>
      <Link to="/" className="btn btn-primary">
        Voltar para o início
      </Link>
    </section>
  );
}
