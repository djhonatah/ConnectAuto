import { Link, NavLink } from 'react-router-dom';
import './Header.css';

export function Header() {
  return (
    <header className="app-header">
      <Link to="/" className="brand">
        ConnectAuto
      </Link>
      <nav>
        <NavLink to="/" end>
          Início
        </NavLink>
        <NavLink to="/veiculos">Veículos</NavLink>
        <NavLink to="/sobre">Sobre</NavLink>
      </nav>
    </header>
  );
}
