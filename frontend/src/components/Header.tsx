import { Link, NavLink } from 'react-router-dom';
import './Header.css';

const NAV_ITEMS = [
  { to: '/', label: 'Início', end: true },
  { to: '/veiculos', label: 'Veículos', end: false },
  { to: '/sobre', label: 'Sobre', end: false },
];

export function Header() {
  return (
    <header className="app-header">
      <Link to="/" className="app-header__brand" aria-label="ConnectAuto, ir para o início">
        <svg className="app-header__mark" viewBox="0 0 28 28" aria-hidden="true">
          <polygon
            points="14,2 24.39,8 24.39,20 14,26 3.61,20 3.61,8"
            fill="none"
            stroke="var(--accent)"
            strokeWidth="1.6"
            strokeLinejoin="round"
          />
          <path
            d="M8 18.2 12.6 10.6a1 1 0 0 1 1.7 0L18.6 17"
            stroke="var(--accent-strong)"
            strokeWidth="1.6"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
          />
          <circle cx="20.4" cy="18.6" r="1.5" fill="var(--accent-strong)" />
        </svg>
        <span>
          Connect<strong>Auto</strong>
        </span>
      </Link>
      <nav aria-label="Navegação principal">
        {NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.label}
          </NavLink>
        ))}
      </nav>
    </header>
  );
}
